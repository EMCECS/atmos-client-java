package com.emc.adapt;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SetBucketAclRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.bean.Grant;
import com.emc.object.s3.bean.PutObjectResult;
import com.emc.rest.smart.SmartClientFactory;

/**
 * Amazon S3 adapter for ECS smart client.
 */
public class SmartClientAdapter implements AmazonS3 {

    protected S3Config config;
    protected S3Client client;

    /**
     * Constructor for AWS adapter.
     * @param config S3Config object to configure S3Client.
     */
    public SmartClientAdapter(S3Config config) {
        this.config = config;
        this.client = (S3Client)SmartClientFactory.createSmartClient(config.toSmartConfig());
    }

    /**
     * Adds designated endpoint to the list of endpoints.
     * @param endpoint The endpoint to be added to the list of communicable endpoints
     *                 for this client.
     */
    @Override
    public void setEndpoint(String endpoint) {
        try {
            config.getEndpoints().add(new URI(endpoint));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRegion(Region region) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setS3ClientOptions(S3ClientOptions clientOptions) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void changeObjectStorageClass(String bucketName, String key, StorageClass newStorageClass) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObjectRedirectLocation(String bucketName, String key, String newRedirectLocation) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    public ObjectListing listObjects(String bucketName) throws AmazonClientException {
        ListObjectsResult lor = client.listObjects(bucketName);
        ObjectListing ol = new ObjectListing();

        ol.setBucketName(bucketName);
        ol.setCommonPrefixes(lor.getCommonPrefixes());
        ol.setDelimiter(lor.getDelimiter());
        ol.setMarker(lor.getMarker());
        ol.setMaxKeys(lor.getMaxKeys());
        ol.setNextMarker(lor.getNextMarker());
        ol.setPrefix(lor.getPrefix());
        ol.setTruncated(lor.getTruncated());

        return ol;
    }

    @Override
    public ObjectListing listObjects(String bucketName, String prefix) throws AmazonClientException {
        ListObjectsResult lor = client.listObjects(bucketName, prefix);
        ObjectListing ol = new ObjectListing();

        ol.setBucketName(bucketName);
        ol.setCommonPrefixes(lor.getCommonPrefixes());
        ol.setDelimiter(lor.getDelimiter());
        ol.setMarker(lor.getMarker());
        ol.setMaxKeys(lor.getMaxKeys());
        ol.setNextMarker(lor.getNextMarker());
        ol.setPrefix(lor.getPrefix());
        ol.setTruncated(lor.getTruncated());

        return ol;
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest) throws AmazonClientException {
        ListObjectsResult lor = client.listObjects(listObjectsRequest.getBucketName(), listObjectsRequest.getPrefix());
        ObjectListing ol = new ObjectListing();

        ol.setBucketName(lor.getBucketName());
        ol.setCommonPrefixes(lor.getCommonPrefixes());
        ol.setDelimiter(lor.getDelimiter());
        ol.setMarker(lor.getMarker());
        ol.setMaxKeys(lor.getMaxKeys());
        ol.setNextMarker(lor.getNextMarker());
        ol.setPrefix(lor.getPrefix());
        ol.setTruncated(lor.getTruncated());

        return ol;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix) throws AmazonClientException {
        ListVersionsResult lvr = client.listVersions(bucketName, prefix);
        VersionListing vl = new VersionListing();

        vl.setBucketName(lvr.getBucketName());
        vl.setCommonPrefixes(lvr.getCommonPrefixes());
        vl.setDelimiter(lvr.getDelimiter());
        vl.setKeyMarker(lvr.getKeyMarker());
        vl.setMaxKeys(lvr.getMaxKeys());
        vl.setNextKeyMarker(lvr.getNextKeyMarker());
        vl.setNextVersionIdMarker(lvr.getNextVersionIdMarker());
        vl.setPrefix(lvr.getPrefix());
        vl.setTruncated(lvr.getTruncated());
        vl.setVersionIdMarker(lvr.getVersionIdMarker());
        for (AbstractVersion av : lvr.getVersions()) {
            S3VersionSummary vs = new S3VersionSummary();
            vs.setKey(av.getKey());
            vs.setLastModified(av.getLastModified());
            vs.setIsLatest(av.getLatest());
            vs.setOwner(new Owner(av.getOwner().getId(), av.getOwner().getDisplayName()));
            vs.setVersionId(av.getVersionId());
            vl.getVersionSummaries().add(vs);
        }

        return vl;
    }

    @Override
    public VersionListing listNextBatchOfVersions(VersionListing previousVersionListing) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix, String keyMarker, String versionIdMarker, String delimiter, Integer maxResults) throws AmazonClientException {
        com.emc.object.s3.request.ListVersionsRequest lvreq = new com.emc.object.s3.request.ListVersionsRequest(bucketName);
        lvreq.setPrefix(prefix);
        lvreq.setKeyMarker(keyMarker);
        lvreq.setVersionIdMarker(versionIdMarker);
        lvreq.setDelimiter(delimiter);

        ListVersionsResult lvr = client.listVersions(lvreq);
        VersionListing vl = new VersionListing();

        vl.setBucketName(lvr.getBucketName());
        vl.setCommonPrefixes(lvr.getCommonPrefixes());
        vl.setDelimiter(lvr.getDelimiter());
        vl.setKeyMarker(lvr.getKeyMarker());
        vl.setMaxKeys(lvr.getMaxKeys());
        vl.setNextKeyMarker(lvr.getNextKeyMarker());
        vl.setNextVersionIdMarker(lvr.getNextVersionIdMarker());
        vl.setPrefix(lvr.getPrefix());
        vl.setTruncated(lvr.getTruncated());
        vl.setVersionIdMarker(lvr.getVersionIdMarker());
        for (AbstractVersion av : lvr.getVersions()) {
            S3VersionSummary vs = new S3VersionSummary();
            vs.setKey(av.getKey());
            vs.setLastModified(av.getLastModified());
            vs.setIsLatest(av.getLatest());
            vs.setOwner(new Owner(av.getOwner().getId(), av.getOwner().getDisplayName()));
            vs.setVersionId(av.getVersionId());
            vl.getVersionSummaries().add(vs);
        }

        return vl;
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest listVersionsRequest) throws AmazonClientException {
        com.emc.object.s3.request.ListVersionsRequest lvreq =
                new com.emc.object.s3.request.ListVersionsRequest(listVersionsRequest.getBucketName());
        lvreq.setDelimiter(listVersionsRequest.getDelimiter());
        lvreq.setKeyMarker(listVersionsRequest.getKeyMarker());
        lvreq.setMaxKeys(listVersionsRequest.getMaxResults());
        lvreq.setPrefix(listVersionsRequest.getPrefix());
        lvreq.setVersionIdMarker(listVersionsRequest.getVersionIdMarker());

        ListVersionsResult lvr = client.listVersions(lvreq);
        VersionListing vl = new VersionListing();

        vl.setBucketName(lvr.getBucketName());
        vl.setCommonPrefixes(lvr.getCommonPrefixes());
        vl.setDelimiter(lvr.getDelimiter());
        vl.setKeyMarker(lvr.getKeyMarker());
        vl.setMaxKeys(lvr.getMaxKeys());
        vl.setNextKeyMarker(lvr.getNextKeyMarker());
        vl.setNextVersionIdMarker(lvr.getNextVersionIdMarker());
        vl.setPrefix(lvr.getPrefix());
        vl.setTruncated(lvr.getTruncated());
        vl.setVersionIdMarker(lvr.getVersionIdMarker());
        for (AbstractVersion av : lvr.getVersions()) {
            S3VersionSummary vs = new S3VersionSummary();
            vs.setKey(av.getKey());
            vs.setLastModified(av.getLastModified());
            vs.setIsLatest(av.getLatest());
            vs.setOwner(new Owner(av.getOwner().getId(), av.getOwner().getDisplayName()));
            vs.setVersionId(av.getVersionId());
            vl.getVersionSummaries().add(vs);
        }

        return vl;
    }

    @Override
    public Owner getS3AccountOwner() throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesBucketExist(String bucketName) throws AmazonClientException {
        return client.bucketExists(bucketName);
    }

    @Override
    public List<Bucket> listBuckets() throws AmazonClientException {
        List<com.emc.object.s3.bean.Bucket> clientList = client.listBuckets().getBuckets();
        List<Bucket> retList = new ArrayList<>();
        for (com.emc.object.s3.bean.Bucket bucket : clientList) {
            Bucket newBucket = new Bucket();
            newBucket.setName(bucket.getName());
            newBucket.setCreationDate(bucket.getCreationDate());
            retList.add(newBucket);
        }
        return retList;
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest) throws AmazonClientException {
        return listBuckets();
    }

    @Override
    public String getBucketLocation(String bucketName) throws AmazonClientException {
        return client.getBucketLocation(bucketName).getRegion().getConstraint();

    }

    @Override
    public String getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) throws AmazonClientException {
        return getBucketLocation(getBucketLocationRequest.getBucketName());
    }

    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest) throws AmazonClientException {
        client.createBucket(createBucketRequest.getBucketName());
        return new Bucket(createBucketRequest.getBucketName());
    }

    @Override
    public Bucket createBucket(String bucketName) throws AmazonClientException {
        client.createBucket(bucketName);
        return new Bucket(bucketName);
    }

    @Override
    public Bucket createBucket(String bucketName, com.amazonaws.services.s3.model.Region region) throws AmazonClientException {
        return createBucket(new CreateBucketRequest(bucketName, region.toString()));
    }

    @Override
    public Bucket createBucket(String bucketName, String region) throws AmazonClientException {
        return createBucket(bucketName);
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList acl = client.getBucketAcl(bucketName);
        AccessControlList retAcl = new AccessControlList();

        retAcl.setOwner(new Owner(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (Grant g : acl.getGrants()) {
            Grantee grantee = (Grantee)g.getGrantee();
            com.amazonaws.services.s3.model.Grant newGrant =
                    new com.amazonaws.services.s3.model.Grant(grantee, Permission.parsePermission(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        return retAcl;
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key, String versionId) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList acl = client.getBucketAcl(bucketName);
        AccessControlList retAcl = new AccessControlList();

        retAcl.setOwner(new Owner(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (Grant g : acl.getGrants()) {
            Grantee grantee = (Grantee)g.getGrantee();
            com.amazonaws.services.s3.model.Grant newGrant =
                    new com.amazonaws.services.s3.model.Grant(grantee, Permission.parsePermission(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        return retAcl;
    }

    @Override
    public void setObjectAcl(String bucketName, String key, AccessControlList acl) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList retAcl = new com.emc.object.s3.bean.AccessControlList();

        retAcl.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (com.amazonaws.services.s3.model.Grant g : acl.getGrants()) {
            AbstractGrantee grantee = (AbstractGrantee)g.getGrantee();
            Grant newGrant =
                    new Grant(grantee, com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        client.setObjectAcl(bucketName, key, retAcl);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, AccessControlList acl) throws AmazonClientException {
        setObjectAcl(bucketName, key, acl);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    @Override
    public AccessControlList getBucketAcl(String bucketName) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList acl = client.getBucketAcl(bucketName);
        AccessControlList retAcl = new AccessControlList();

        retAcl.setOwner(new Owner(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (Grant g : acl.getGrants()) {
            Grantee grantee = (Grantee)g.getGrantee();
            com.amazonaws.services.s3.model.Grant newGrant =
                    new com.amazonaws.services.s3.model.Grant(grantee, Permission.parsePermission(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        return retAcl;
    }

    @Override
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws AmazonClientException {
        setBucketAcl(setBucketAclRequest.getBucketName(), setBucketAclRequest.getAcl());
    }

    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest) throws AmazonClientException {
        return getBucketAcl(getBucketAclRequest.getBucketName());
    }

    @Override
    public void setBucketAcl(String bucketName, AccessControlList acl) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList aclReq = new com.emc.object.s3.bean.AccessControlList();

        aclReq.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (com.amazonaws.services.s3.model.Grant g : acl.getGrants()) {
            Grant newGrant = new Grant((AbstractGrantee)g.getGrantee(), com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
            aclReq.getGrants().add(newGrant);
        }

        client.setBucketAcl(bucketName, aclReq);
    }

    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl) throws AmazonClientException {
        client.setBucketAcl(bucketName, CannedAcl.fromHeaderValue(acl.toString()));
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String key) throws AmazonClientException {
        S3ObjectMetadata md = client.getObjectMetadata(bucketName, key);

        ObjectMetadata ret = new ObjectMetadata();
        ret.setExpirationTime(md.getExpirationTime());
        ret.setExpirationTimeRuleId(md.getExpirationRuleId());
        ret.setLastModified(md.getLastModified());
        ret.setContentMD5(md.getContentMd5());
        ret.setContentType(md.getContentType());
        ret.setContentLength(md.getContentLength());

        return ret;
    }

    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest) throws AmazonClientException {
        return getObjectMetadata(getObjectMetadataRequest.getBucketName(), getObjectMetadataRequest.getKey());
    }

    @Override
    public S3Object getObject(String bucketName, String key) throws AmazonClientException {
        return client.readObject(bucketName, key, S3Object.class);
    }

    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest) throws AmazonClientException {
        return getObject(getObjectRequest.getBucketName(), getObjectRequest.getKey());
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File destinationFile) throws AmazonClientException {
        return getObjectMetadata(getObjectRequest.getBucketName(), getObjectRequest.getKey());
    }

    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest) throws AmazonClientException {
        client.deleteBucket(deleteBucketRequest.getBucketName());
    }

    @Override
    public void deleteBucket(String bucketName) throws AmazonClientException {
        client.deleteBucket(bucketName);
    }

    @Override
    public com.amazonaws.services.s3.model.PutObjectResult putObject(PutObjectRequest putObjectRequest) throws AmazonClientException {
        try {
            return putObject(putObjectRequest.getBucketName(), putObjectRequest.getKey(), putObjectRequest.getFile());
        }
        catch (Exception e) {
            return putObject(putObjectRequest.getBucketName(), putObjectRequest.getKey(), putObjectRequest.getInputStream(), putObjectRequest.getMetadata());
        }
    }

    @Override
    public com.amazonaws.services.s3.model.PutObjectResult putObject(String bucketName, String key, File file) throws AmazonClientException {

        com.emc.object.s3.request.PutObjectRequest por = new com.emc.object.s3.request.PutObjectRequest(bucketName, key, file);
        PutObjectResult pores = client.putObject(por);

        com.amazonaws.services.s3.model.PutObjectResult ret = new com.amazonaws.services.s3.model.PutObjectResult();
        ret.setVersionId(pores.getVersionId());
        ret.setExpirationTime(pores.getExpirationDate());
        ret.setExpirationTimeRuleId(pores.getExpirationRuleId());

        return ret;
    }

    @Override
    public com.amazonaws.services.s3.model.PutObjectResult putObject(String bucketName, String key, InputStream input, ObjectMetadata metadata) throws AmazonClientException {

        com.emc.object.s3.request.PutObjectRequest por = new com.emc.object.s3.request.PutObjectRequest(bucketName, key, input);
        PutObjectResult pores = client.putObject(por);

        com.amazonaws.services.s3.model.PutObjectResult ret = new com.amazonaws.services.s3.model.PutObjectResult();
        ret.setVersionId(pores.getVersionId());
        ret.setExpirationTime(pores.getExpirationDate());
        ret.setExpirationTimeRuleId(pores.getExpirationRuleId());

        return ret;
    }

    @Override
    public CopyObjectResult copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws AmazonClientException {
        com.emc.object.s3.request.PutObjectRequest req = new com.emc.object.s3.request.PutObjectRequest(destinationBucketName,
                destinationKey, client.readObject(sourceBucketName, sourceKey, S3Object.class));
        PutObjectResult res = client.putObject(req);

        CopyObjectResult cor = new CopyObjectResult();
        cor.setExpirationTime(res.getExpirationDate());
        cor.setExpirationTimeRuleId(res.getExpirationRuleId());
        cor.setVersionId(res.getVersionId());

        return cor;
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws AmazonClientException {
        return copyObject(copyObjectRequest.getSourceBucketName(), copyObjectRequest.getSourceKey(),
                copyObjectRequest.getDestinationBucketName(), copyObjectRequest.getDestinationKey());
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) throws AmazonClientException {
        com.emc.object.s3.request.CopyPartRequest req = new com.emc.object.s3.request.CopyPartRequest(
                copyPartRequest.getSourceBucketName(), copyPartRequest.getSourceKey(), copyPartRequest.getDestinationBucketName(),
                copyPartRequest.getDestinationBucketName(), copyPartRequest.getUploadId(), copyPartRequest.getPartNumber());
        com.emc.object.s3.bean.CopyPartResult res = client.copyPart(req);

        CopyPartResult ret = new CopyPartResult();
        ret.setETag(res.getETag());
        ret.setLastModifiedDate(res.getLastModified());
        ret.setPartNumber(res.getPartNumber());
        ret.setVersionId(res.getVersionId());

        return ret;
    }

    @Override
    public void deleteObject(String bucketName, String key) throws AmazonClientException {
        client.deleteObject(bucketName, key);
    }

    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest) throws AmazonClientException {
        client.deleteObject(deleteObjectRequest.getBucketName(), deleteObjectRequest.getKey());
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws AmazonClientException {
        com.emc.object.s3.request.DeleteObjectsRequest dor = new com.emc.object.s3.request.DeleteObjectsRequest(deleteObjectsRequest.getBucketName());
        com.emc.object.s3.bean.DeleteObjectsResult dObjs = client.deleteObjects(dor);
        List<DeleteObjectsResult.DeletedObject> dObjList = new ArrayList<>();
        for (AbstractDeleteResult adr : dObjs.getResults()) {
            DeleteObjectsResult.DeletedObject dObj = new DeleteObjectsResult.DeletedObject();
            dObj.setKey(adr.getKey());
            dObj.setVersionId(adr.getVersionId());
            dObjList.add(new DeleteObjectsResult.DeletedObject());
        }
        return new DeleteObjectsResult(dObjList);
    }

    @Override
    public void deleteVersion(String bucketName, String key, String versionId) throws AmazonClientException {
        client.deleteVersion(bucketName, key, versionId);
    }

    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest) throws AmazonClientException {
        client.deleteVersion(deleteVersionRequest.getBucketName(), deleteVersionRequest.getKey(), deleteVersionRequest.getVersionId());
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketLoggingConfiguration(SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(String bucketName) throws AmazonClientException {
        return new BucketVersioningConfiguration(client.getBucketVersioning(bucketName).toString());
    }

    @Override
    public void setBucketVersioningConfiguration(SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest) throws AmazonClientException {
        VersioningConfiguration vc = new VersioningConfiguration();
        if(setBucketVersioningConfigurationRequest.getVersioningConfiguration().getStatus().equalsIgnoreCase("Enabled")) {
            vc.setStatus(VersioningConfiguration.Status.Enabled);
        }
        else vc.setStatus(VersioningConfiguration.Status.Suspended);
        client.setBucketVersioning(setBucketVersioningConfigurationRequest.getBucketName(), vc);
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(String bucketName) {
        LifecycleConfiguration lc = client.getBucketLifecycle(bucketName);
        List<BucketLifecycleConfiguration.Rule> rules = new ArrayList<>();

        for (LifecycleRule lr : lc.getRules()) {
            BucketLifecycleConfiguration.Rule rule = new BucketLifecycleConfiguration.Rule().withId(lr.getId());
            rule.setPrefix(lr.getPrefix());
            rule.setExpirationDate(lr.getExpirationDate());
            rule.setExpirationInDays(lr.getExpirationDays());
            if (lr.getStatus() == LifecycleRule.Status.Enabled) rule.setStatus("Enabled");
            else rule.setStatus("Disabled");
            rules.add(rule);
        }

        return new BucketLifecycleConfiguration(rules);
    }

    @Override
    public void setBucketLifecycleConfiguration(String bucketName, BucketLifecycleConfiguration bucketLifecycleConfiguration) {
        LifecycleConfiguration lc = new LifecycleConfiguration();

        for (BucketLifecycleConfiguration.Rule rule : bucketLifecycleConfiguration.getRules()) {
            LifecycleRule.Status st = LifecycleRule.Status.Enabled;
            if(rule.getStatus().equalsIgnoreCase("Disabled")) st = LifecycleRule.Status.Disabled;
            LifecycleRule lr = new LifecycleRule(rule.getId(), rule.getPrefix(), st, rule.getExpirationInDays());
            lc.getRules().add(lr);
        }

        client.setBucketLifecycle(bucketName, lc);
    }

    @Override
    public void setBucketLifecycleConfiguration(SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest) {
        setBucketLifecycleConfiguration(setBucketLifecycleConfigurationRequest.getBucketName(), setBucketLifecycleConfigurationRequest.getLifecycleConfiguration());
    }

    @Override
    public void deleteBucketLifecycleConfiguration(String bucketName) {
        client.deleteBucketLifecycle(bucketName);
    }

    @Override
    public void deleteBucketLifecycleConfiguration(DeleteBucketLifecycleConfigurationRequest deleteBucketLifecycleConfigurationRequest) {
        client.deleteBucketLifecycle(deleteBucketLifecycleConfigurationRequest.getBucketName());
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(String bucketName) {
        CorsConfiguration cc = client.getBucketCors(bucketName);
        BucketCrossOriginConfiguration bcc = new BucketCrossOriginConfiguration();

        for (CorsRule cr : cc.getCorsRules()) {
            CORSRule ncr = new CORSRule().withId(cr.getId()).withMaxAgeSeconds(cr.getMaxAgeSeconds());
            ncr.setAllowedHeaders(cr.getAllowedHeaders());
            ncr.setAllowedOrigins(cr.getAllowedOrigins());
            ncr.setExposedHeaders(cr.getExposeHeaders());
            ncr.setAllowedMethods(CORSRule.AllowedMethods.fromValue(cr.getAllowedMethods().toString()));
            bcc.getRules().add(ncr);
        }

        return bcc;
    }

    @Override
    public void setBucketCrossOriginConfiguration(String bucketName, BucketCrossOriginConfiguration bucketCrossOriginConfiguration) {
        CorsConfiguration cc = new CorsConfiguration();

        for (CORSRule cr : bucketCrossOriginConfiguration.getRules()) {
            CorsRule ncr = new CorsRule().withId(cr.getId()).withMaxAgeSeconds(cr.getMaxAgeSeconds());
            ncr.setAllowedHeaders(cr.getAllowedHeaders());
            ncr.setAllowedOrigins(cr.getAllowedOrigins());
            ncr.setExposeHeaders(cr.getExposedHeaders());
            List<CorsMethod> cml = new ArrayList<>();
            for (CORSRule.AllowedMethods am : cr.getAllowedMethods()) {
                cml.add(CorsMethod.valueOf(am.toString()));
            }
            ncr.setAllowedMethods(cml);
            cc.getCorsRules().add(ncr);
        }

        client.setBucketCors(bucketName, cc);
    }

    @Override
    public void setBucketCrossOriginConfiguration(SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest) {
        setBucketCrossOriginConfiguration(setBucketCrossOriginConfigurationRequest.getBucketName(),
                setBucketCrossOriginConfigurationRequest.getCrossOriginConfiguration());
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(String bucketName) {
        client.deleteBucketCors(bucketName);
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(DeleteBucketCrossOriginConfigurationRequest deleteBucketCrossOriginConfigurationRequest) {
        client.deleteBucketCors(deleteBucketCrossOriginConfigurationRequest.getBucketName());
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String bucketName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketTaggingConfiguration(String bucketName, BucketTaggingConfiguration bucketTaggingConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketTaggingConfiguration(SetBucketTaggingConfigurationRequest setBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketTaggingConfiguration(String bucketName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketTaggingConfiguration(DeleteBucketTaggingConfigurationRequest deleteBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketNotificationConfiguration(SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketNotificationConfiguration(String bucketName, BucketNotificationConfiguration bucketNotificationConfiguration) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(GetBucketWebsiteConfigurationRequest getBucketWebsiteConfigurationRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketWebsiteConfiguration(String bucketName, BucketWebsiteConfiguration configuration) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketWebsiteConfiguration(SetBucketWebsiteConfigurationRequest setBucketWebsiteConfigurationRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketWebsiteConfiguration(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketWebsiteConfiguration(DeleteBucketWebsiteConfigurationRequest deleteBucketWebsiteConfigurationRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketPolicy getBucketPolicy(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyText) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBucketPolicy(SetBucketPolicyRequest setBucketPolicyRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketPolicy(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request) throws AmazonClientException {
        com.emc.object.s3.request.InitiateMultipartUploadRequest req =
                new com.emc.object.s3.request.InitiateMultipartUploadRequest(request.getBucketName(), request.getKey());

        if (request.getObjectMetadata() != null) {
            S3ObjectMetadata smd = new S3ObjectMetadata();
            ObjectMetadata omd =request.getObjectMetadata();
            smd.setContentType(omd.getContentType());
            smd.setContentLength(omd.getContentLength());
            smd.setContentEncoding(omd.getContentEncoding());
            smd.setCacheControl(omd.getCacheControl());
            smd.setContentMd5(omd.getContentMD5());
            smd.setHttpExpires(omd.getHttpExpiresDate());
            smd.setContentDisposition(omd.getContentDisposition());
            req.setObjectMetadata(smd);
        }

        if(request.getAccessControlList() != null) {
            AccessControlList acl = request.getAccessControlList();
            com.emc.object.s3.bean.AccessControlList reqAcl = new com.emc.object.s3.bean.AccessControlList();
            reqAcl.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
            for (com.amazonaws.services.s3.model.Grant g : acl.getGrants()) {
                AbstractGrantee grantee = (AbstractGrantee)g.getGrantee();
                Grant newGrant =
                        new Grant(grantee, com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
                reqAcl.getGrants().add(newGrant);
            }
        }

        req.setCannedAcl(CannedAcl.fromHeaderValue(request.getCannedACL().toString()));
        com.emc.object.s3.bean.InitiateMultipartUploadResult imur = client.initiateMultipartUpload(req);

        InitiateMultipartUploadResult ret = new InitiateMultipartUploadResult();
        ret.setBucketName(imur.getBucketName());
        ret.setKey(imur.getKey());
        ret.setUploadId(imur.getUploadId());
        return ret;
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request) throws AmazonClientException {

        MultipartPart mp;
        if (request.getInputStream() != null) {
            com.emc.object.s3.request.UploadPartRequest req = new com.emc.object.s3.request.UploadPartRequest(
                    request.getBucketName(), request.getKey(), request.getUploadId(), request.getPartNumber(), request.getInputStream());
            mp = client.uploadPart(req);
        }
        else {
            com.emc.object.s3.request.UploadPartRequest req = new com.emc.object.s3.request.UploadPartRequest(
                    request.getBucketName(), request.getKey(), request.getUploadId(), request.getPartNumber(), request.getFile());
            mp = client.uploadPart(req);
        }

        UploadPartResult ret = new UploadPartResult();
        ret.setETag(mp.getETag());
        ret.setPartNumber(mp.getPartNumber());
        return new UploadPartResult();
    }

    @Override
    public PartListing listParts(ListPartsRequest request) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) throws AmazonClientException {
        client.abortMultipartUpload(new com.emc.object.s3.request.AbortMultipartUploadRequest(
                request.getBucketName(), request.getKey(), request.getUploadId()));

    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) throws AmazonClientException {
        com.emc.object.s3.request.CompleteMultipartUploadRequest req = new com.emc.object.s3.request.CompleteMultipartUploadRequest(
                request.getBucketName(), request.getKey(), request.getUploadId());
        List<MultipartPart> mpList = new ArrayList<>();
        for (PartETag tag : request.getPartETags()) {
            mpList.add(new MultipartPart(tag.getPartNumber(), tag.getETag()));
        }

        com.emc.object.s3.bean.CompleteMultipartUploadResult cmur = client.completeMultipartUpload(req);
        CompleteMultipartUploadResult ret = new CompleteMultipartUploadResult();
        ret.setBucketName(cmur.getBucketName());
        ret.setKey(cmur.getKey());
        ret.setETag(cmur.getETag());
        ret.setLocation(cmur.getLocation());

        return ret;
    }

    /**
     * Multipart uploads listing; contains all the information about the
     * ListMultipartUploads method.
     *
     * @param request Multipart upload request.
     * @return Listing of multipart uploads with metadata. List contains empty
     * Upload objects.
     * @throws AmazonClientException
     */
    @Override
    public MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest request) throws AmazonClientException {
        ListMultipartUploadsResult res = client.listMultipartUploads(request.getBucketName());
        MultipartUploadListing mul = new MultipartUploadListing();

        mul.setBucketName(res.getBucketName());
        mul.setCommonPrefixes(res.getCommonPrefixes());
        mul.setDelimiter(res.getDelimiter());
        mul.setKeyMarker(res.getKeyMarker());
        mul.setMaxUploads(res.getMaxUploads());
        mul.setNextKeyMarker(res.getNextKeyMarker());
        mul.setNextUploadIdMarker(res.getUploadIdMarker());
        mul.setPrefix(res.getPrefix());
        mul.setTruncated(res.getTruncated());
        mul.setUploadIdMarker(res.getUploadIdMarker());

        List<MultipartUpload> mpuList = new ArrayList<>();
        for (Upload ul : res.getUploads()) {
            MultipartUpload mpu = new MultipartUpload();
//            mpu.setKey(ul.getKey());
//            mpu.setUploadId(ul.getUploadID());
//            mpu.setOwner(new Owner(ul.getCanonicalUser.getID(), ul.getCanonicalUser.getDisplayName()));
//            mpu.setInitiator(new Owner(ul.getInitiator.getID(), ul.getInitiator.getDisplayName()));
//            mpu.setInitiated(ul.getInitiated());
//            mpu.setStorageClass(ul.getStorageClass());
            mpuList.add(mpu);
        }
        mul.setMultipartUploads(mpuList);

        return mul;
    }

    @Override
    public S3ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreObject(RestoreObjectRequest copyGlacierObjectRequest) throws AmazonServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreObject(String bucketName, String key, int expirationInDays) throws AmazonServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableRequesterPays(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disableRequesterPays(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequesterPaysEnabled(String bucketName) throws AmazonClientException {
        throw new UnsupportedOperationException();
    }

}