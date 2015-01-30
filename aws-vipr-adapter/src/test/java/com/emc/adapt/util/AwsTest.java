package com.emc.adapt.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringInputStream;
import com.emc.atmos.util.RandomInputStream;
import com.emc.vipr.services.lib.ViprConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AwsTest {
    private static AmazonS3Client s3;
    private Map<String, Set<String>> bucketsAndKeys = new TreeMap<String, Set<String>>();

    @Test
    public void testCrudBuckets() throws Exception {
        String bucket = "test-bucket-aws";

        s3.createBucket(bucket);

        Assert.assertTrue("created bucket does not exist", s3.doesBucketExist(bucket));

        boolean bucketFound = false;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket)) bucketFound = true;
        }
        Assert.assertTrue("bucket not found in listBuckets", bucketFound);

        s3.deleteBucket(bucket);

        Assert.assertFalse("bucket still exists after delete", s3.doesBucketExist(bucket));

        bucketFound = false;
        buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket)) bucketFound = true;
        }
        Assert.assertFalse("bucket found in listBuckets after delete",
                bucketFound);
    }

    @Test
    public void testDeleteNonEmptyBucket() throws Exception {
        String bucket = "test-nonempty-bucket";
        String content = "Hello World";
        String key = "testKey";

        s3.createBucket(bucket);
        s3.putObject(bucket, key, new StringInputStream(content), null);
        createdKeys(bucket).add(key);

        try {
            s3.deleteBucket(bucket);
            Assert.fail("deleting non-empty bucket should fail");
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 409)
                throw e;
        }
    }

    @Test
    public void testCrudKeys() throws Exception {
        String bucket = "test-bucket-aws";
        String content = "Hello World";
        String key = "testKey";

        s3.createBucket(bucket);
        createdKeys(bucket); // make sure we clean up the bucket

        s3.putObject(bucket, key, new StringInputStream(content), null);

        S3Object object = s3.getObject(bucket, key);
        String readContent = new Scanner(object.getObjectContent(), "UTF-8").useDelimiter("\\A").next();
        Assert.assertEquals("content mismatch", content, readContent);

        String newContent = "Hello Waldo!";
        s3.putObject(bucket, key, new StringInputStream(newContent), null);

        object = s3.getObject(bucket, key);
        readContent = new Scanner(object.getObjectContent(), "UTF-8").useDelimiter("\\A").next();
        Assert.assertEquals("updated content mismatch", newContent, readContent);

        s3.deleteObject(bucket, key);

        try {
            s3.getObject(bucket, key);
            Assert.fail("object still exists after delete");
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) throw e;
        }
    }

    @Test
    public void testRequestExpiration() throws Exception {
        String bucket = "test-bucket-aws";
        String content = "Hello World";
        String key = "testKey";
        String cacheControl = "nocache";
        String disposition = "attachment; filename=foo.txt";
        String contentEncoding = "raw";
        String contentLanguage = "en-US";
        String contentType = "test/plain";
        String expires = "0";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);

        s3.createBucket(bucket);
        createdKeys(bucket); // make sure we clean up the bucket

        // test PUT request
        URL url = s3.generatePresignedUrl(bucket, key, cal.getTime(), HttpMethod.PUT);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("PUT");
        con.connect();
        copyStream(new StringInputStream(content), con.getOutputStream());
        copyStream(con.getInputStream(), new ByteArrayOutputStream());
        con.disconnect();
        Assert.assertEquals("PUT response code wrong", 200, con.getResponseCode());
        createdKeys(bucket).add(key);

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setCacheControl(cacheControl);
        overrides.setContentDisposition(disposition);
        overrides.setContentEncoding(contentEncoding);
        overrides.setContentLanguage(contentLanguage);
        overrides.setContentType(contentType);
        overrides.setExpires(expires);

        GeneratePresignedUrlRequest gRequest = new GeneratePresignedUrlRequest(bucket, key);
        gRequest.setMethod(HttpMethod.GET);
        gRequest.setExpiration(cal.getTime());
        gRequest.setResponseHeaders(overrides);

        // test GET request
        url = s3.generatePresignedUrl(gRequest);
        con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(false);
        con.setDoInput(true);
        con.connect();

        // verify headers
        Assert.assertEquals("cache-control header mismatch", cacheControl, con.getHeaderField("Cache-Control"));
        Assert.assertEquals("content-disposition header mismatch",
                disposition,
                con.getHeaderField("Content-Disposition"));
        Assert.assertEquals("content-encoding header mismatch", contentEncoding, con.getContentEncoding());
        Assert.assertEquals("content-language header mismatch",
                contentLanguage,
                con.getHeaderField("Content-Language"));
        Assert.assertEquals("content-type header mismatch", contentType, con.getContentType());
        Assert.assertEquals("expires header mismatch", expires, Long.toString(con.getExpiration()));

        // verify content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(con.getInputStream(), baos);
        con.disconnect();
        Assert.assertEquals("content mismatch", content, baos.toString());
    }

    @Test
    public void testResponseHeaderOverride() throws Exception {
        DateFormat rfc822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        String bucket = "test-bucket";
        String content = "Hello World";
        String key = "testKey";
        String cacheControl = "nocache";
        String disposition = "attachment; filename=foo.txt";
        String contentEncoding = "raw";
        String contentLanguage = "en-US";
        String contentType = "test/plain";
        String expires = rfc822.format(new Date());

        s3.createBucket(bucket);
        createdKeys(bucket); // make sure we clean up the bucket

        s3.putObject(bucket, key, new StringInputStream(content), null);
        createdKeys(bucket).add(key);

        GetObjectRequest request = new GetObjectRequest(bucket, key);
        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setCacheControl(cacheControl);
        overrides.setContentDisposition(disposition);
        overrides.setContentEncoding(contentEncoding);
        overrides.setContentLanguage(contentLanguage);
        overrides.setContentType(contentType);
        overrides.setExpires(expires);
        request.setResponseHeaders(overrides);
        S3Object object = s3.getObject(request);
        ObjectMetadata metadata = object.getObjectMetadata();
        Assert.assertEquals("cache-control header mismatch", cacheControl, metadata.getCacheControl());
        Assert.assertEquals("content-disposition header mismatch", disposition, metadata.getContentDisposition());
        Assert.assertEquals("content-encoding header mismatch", contentEncoding, metadata.getContentEncoding());
        Assert.assertEquals("content-type header mismatch", contentType, metadata.getContentType());
        Assert.assertEquals("expires header mismatch", expires, rfc822.format(metadata.getHttpExpiresDate()));
        String readContent = new Scanner(object.getObjectContent(), "UTF-8").useDelimiter("\\A").next();
        Assert.assertEquals("content mismatch", content, readContent);
    }

    @Test
    public void testMultipartUpload() throws Exception {
        String bucket = "multipart-bucket";
        String key = "multipartKey";

        // write large file (must be a file to support parallel uploads)
        File tmpFile = File.createTempFile("random", "bin");
        tmpFile.deleteOnExit();
        int objectSize = 100 * 1024 * 1024; // 100M

        copyStream(new RandomInputStream(objectSize), new FileOutputStream(tmpFile));

        Assert.assertEquals("tmp file is not the right size", objectSize, tmpFile.length());

        s3.createBucket(bucket);
        createdKeys(bucket);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(50));
        TransferManager tm = new TransferManager(s3, executor);

        PutObjectRequest request = new PutObjectRequest(bucket, key, tmpFile);
        request.setMetadata(new ObjectMetadata());
        request.getMetadata().addUserMetadata("selector", "one");

        Upload upload = tm.upload(request);
        createdKeys(bucket).add(key);

        upload.waitForCompletion();

        S3Object object = s3.getObject(bucket, key);

        int size = copyStream(object.getObjectContent(), null);
        Assert.assertEquals("Wrong object size", objectSize, size);
    }

    @Test
    public void testUpdateMetadata() throws Exception {
        String bucket = "meta-bucket";
        String key = "metaKey";
        String content = "test metadata update";

        s3.createBucket(bucket);
        createdKeys(bucket);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("key1", "value1");
        s3.putObject(bucket, key, new StringInputStream(content), metadata);
        createdKeys(bucket).add(key);

        // verify metadata
        S3Object object = s3.getObject(bucket, key);
        metadata = object.getObjectMetadata();
        Assert.assertEquals("value1", metadata.getUserMetadata().get("key1"));

        // update (add) metadata - only way is to copy
        metadata.addUserMetadata("key2", "value2");
        CopyObjectRequest request = new CopyObjectRequest(bucket, key, bucket, key);
        request.setNewObjectMetadata(metadata);
        s3.copyObject(request);

        // verify metadata (both keys)
        object = s3.getObject(bucket, key);
        metadata = object.getObjectMetadata();
        Assert.assertEquals("value1", metadata.getUserMetadata().get("key1"));
        Assert.assertEquals("value2", metadata.getUserMetadata().get("key2"));

        // test for bug 28668
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(object.getObjectContent(), baos);
        Assert.assertEquals(content, baos.toString("UTF-8"));
    }

    @Test
    public void testMultipartList() throws Exception {
        String bucket = "multipart-list-bucket";
        String key = "multipartListKey";

        s3.createBucket(bucket);
        createdKeys(bucket);

        // initiate multipart
        InitiateMultipartUploadResult result;
        result = s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucket, key));

        int partSize = 100 * 1024;

        // send part 1
        UploadPartRequest request = new UploadPartRequest();
        request.withBucketName(bucket).withKey(key)
                .withUploadId(result.getUploadId()).withPartNumber(1)
                .withInputStream(new RandomInputStream(partSize))
                .withPartSize(partSize);
        String etag1 = s3.uploadPart(request).getETag();

        // send part 2
        request.withBucketName(bucket).withKey(key)
                .withUploadId(result.getUploadId()).withPartNumber(2)
                .withInputStream(new RandomInputStream(partSize))
                .withPartSize(partSize);
        String etag2 = s3.uploadPart(request).getETag();

        // list parts
        PartListing listing = s3.listParts(new ListPartsRequest(bucket, key,
                result.getUploadId()).withMaxParts(1000));
        Assert.assertEquals("Wrong number of parts", 2, listing.getParts().size());

        // complete multipart
        List<PartETag> etags = Arrays.asList(new PartETag(1, etag1), new PartETag(2, etag2));
        s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucket, key, result.getUploadId(), etags));

        createdKeys(bucket).add(key);

        s3.getObject(bucket, key);
    }

    @After
    public void after() {
        for (String bucket : bucketsAndKeys.keySet()) {
            for (String key : createdKeys(bucket)) {
                s3.deleteObject(bucket, key);
            }
            s3.deleteBucket(bucket);
        }
    }

    private synchronized Set<String> createdKeys(String bucket) {
        Set<String> keys = bucketsAndKeys.get(bucket);
        if (keys == null) {
            keys = new TreeSet<String>();
            bucketsAndKeys.put(bucket, keys);
        }
        return keys;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        Properties props = ViprConfig.getProperties();
        String endpoint = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ENDPOINT);
        String accessKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ACCESS_KEY_ID);
        String secret = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_SECRET_KEY);
        String proxyHost = props.getProperty(ViprConfig.PROP_PROXY_HOST);
        String proxyPortStr = props.getProperty(ViprConfig.PROP_PROXY_PORT);
        int proxyPort = proxyPortStr == null ? 0 : Integer.parseInt(proxyPortStr);

        s3 = createClient(new URI(endpoint), proxyHost, proxyPort, accessKey, secret);
    }

    private static AmazonS3Client createClient(URI endpoint, String proxyHost, int proxyPort,
                                               String uid, String secret) {
        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.valueOf(endpoint.getScheme().toUpperCase()));

        if (proxyHost != null) {
            config.setProxyHost(proxyHost);
            config.setProxyPort(proxyPort);
        }

        AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(uid, secret), config);
        client.setEndpoint(endpoint.toString());

        S3ClientOptions options = new S3ClientOptions();
        options.setPathStyleAccess(true);
        client.setS3ClientOptions(options);
        return client;
    }

    private int copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[100 * 1024];
        int total = 0, read = is.read(buffer);
        while (read >= 0) {
            if (os != null)
                os.write(buffer, 0, read);
            total += read;
            read = is.read(buffer);
        }
        is.close();
        if (os != null)
            os.close();
        return total;
    }
}
