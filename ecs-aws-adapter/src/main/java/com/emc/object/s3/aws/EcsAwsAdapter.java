/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.object.s3.aws;

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
import com.amazonaws.services.s3.model.Grant;
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
import com.emc.object.Method;
import com.emc.object.Range;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.bean.PutObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;
import com.emc.object.s3.request.*;

import java.io.*;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Amazon S3 adapter for AWS.
 * Provides an interface for accessing the Amazon S3 web service.
 *
 * Amazon S3 provides storage for the Internet,
 * and is designed to make web-scale computing easier for developers.
 *
 * The Amazon S3 Java SDK provides a simple interface that can be
 * used to store and retrieve any amount of data, at any time,
 * from anywhere on the web. It gives any developer access to the same
 * highly scalable, reliable, secure, fast, inexpensive infrastructure
 * that Amazon uses to run its own global network of web sites.
 * The service aims to maximize benefits of scale and to pass those
 * benefits on to developers.
 *
 * For more information about Amazon S3, please see
 * <a href="http://aws.amazon.com/s3">
 * http://aws.amazon.com/s3</a>
 **/
public class EcsAwsAdapter implements AmazonS3 {

    protected S3Client client;
    protected S3Config config;

    /**
     * Constructor for AWS adapter.
     *
     * @param config S3Config object containing endpoint and access
     *               credentials for client.
     */
    public EcsAwsAdapter(S3Config config) {
        this.config = config;
        this.client = new S3JerseyClient(this.config);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setEndpoint(String endpoints) {
        throw new UnsupportedOperationException("endpoint[s] can only be set in the constructor of this adapter");
    }

    /**
     *{@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setRegion(Region region) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setS3ClientOptions(S3ClientOptions clientOptions) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void changeObjectStorageClass(String bucketName, String key, StorageClass newStorageClass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setObjectRedirectLocation(String bucketName, String key, String newRedirectLocation) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not contain object encoding type details.
     * </p>
     */
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

        for(com.emc.object.s3.bean.S3Object obj : lor.getObjects()) {
            S3ObjectSummary os = new S3ObjectSummary();
            os.setBucketName(lor.getBucketName());
            os.setKey(obj.getKey());
            os.setETag(obj.geteTag());
            os.setLastModified(obj.getLastModified());
            os.setSize(obj.getSize());
            os.setStorageClass(obj.getStorageClass().toString());
            os.setOwner(new Owner(obj.getOwner().getDisplayName(), obj.getOwner().getId()));
            ol.getObjectSummaries().add(os);
        }

        return ol;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not include object encoding type details.
     * </p>
     */
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

        for(com.emc.object.s3.bean.S3Object obj : lor.getObjects()) {
            S3ObjectSummary os = new S3ObjectSummary();
            os.setBucketName(lor.getBucketName());
            os.setKey(obj.getKey());
            os.setETag(obj.geteTag());
            os.setLastModified(obj.getLastModified());
            os.setSize(obj.getSize());
            os.setStorageClass(obj.getStorageClass().toString());
            os.setOwner(new Owner(obj.getOwner().getDisplayName(), obj.getOwner().getId()));
            ol.getObjectSummaries().add(os);
        }

        return ol;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not include object encoding type details.
     * </p>
     */
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

        for(com.emc.object.s3.bean.S3Object obj : lor.getObjects()) {
            S3ObjectSummary os = new S3ObjectSummary();
            os.setBucketName(lor.getBucketName());
            os.setKey(obj.getKey());
            os.setETag(obj.geteTag());
            os.setLastModified(obj.getLastModified());
            os.setSize(obj.getSize());
            os.setStorageClass(obj.getStorageClass().toString());
            os.setOwner(new Owner(obj.getOwner().getDisplayName(), obj.getOwner().getId()));
            ol.getObjectSummaries().add(os);
        }

        return ol;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not include object encoding type details.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public VersionListing listNextBatchOfVersions(VersionListing previousVersionListing) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not include object encoding type details.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Return object will not include object encoding type details.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public Owner getS3AccountOwner() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesBucketExist(String bucketName) throws AmazonClientException {
        return client.bucketExists(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bucket> listBuckets() throws AmazonClientException {
        List<com.emc.object.s3.bean.Bucket> clientList = client.listBuckets().getBuckets();
        List<Bucket> retList = new ArrayList<Bucket>();
        for (com.emc.object.s3.bean.Bucket bucket : clientList) {
            Bucket newBucket = new Bucket(bucket.getName());
            newBucket.setCreationDate(bucket.getCreationDate());
            retList.add(newBucket);
        }
        return retList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest) throws AmazonClientException {
        return listBuckets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBucketLocation(String bucketName) throws AmazonClientException {
        return client.getBucketLocation(bucketName).getRegion().getConstraint();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) throws AmazonClientException {
        return getBucketLocation(getBucketLocationRequest.getBucketName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest) throws AmazonClientException {
        client.createBucket(createBucketRequest.getBucketName());
        return new Bucket(createBucketRequest.getBucketName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bucket createBucket(String bucketName) throws AmazonClientException {
        client.createBucket(bucketName);
        return new Bucket(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bucket createBucket(String bucketName, com.amazonaws.services.s3.model.Region region) throws AmazonClientException {
        return createBucket(new CreateBucketRequest(bucketName, region.toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bucket createBucket(String bucketName, String region) throws AmazonClientException {
        return createBucket(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlList getObjectAcl(String bucketName, String key) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList acl = client.getBucketAcl(bucketName);
        AccessControlList retAcl = new AccessControlList();

        retAcl.setOwner(new Owner(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (com.emc.object.s3.bean.Grant g : acl.getGrants()) {
            Grantee grantee = (Grantee)g.getGrantee();
            com.amazonaws.services.s3.model.Grant newGrant =
                    new com.amazonaws.services.s3.model.Grant(grantee, Permission.parsePermission(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        return retAcl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlList getObjectAcl(String bucketName, String key, String versionId) throws AmazonClientException {
        return getObjectAcl(bucketName, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectAcl(String bucketName, String key, AccessControlList acl) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList retAcl = new com.emc.object.s3.bean.AccessControlList();

        retAcl.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (Grant g : acl.getGrants()) {
            Grantee grantee = g.getGrantee();
            com.emc.object.s3.bean.Grant newGrant =
                    new com.emc.object.s3.bean.Grant((AbstractGrantee)grantee, com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        client.setObjectAcl(bucketName, key, retAcl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectAcl(String bucketName, String key, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, AccessControlList acl) throws AmazonClientException {
        setObjectAcl(bucketName, key, acl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public AccessControlList getBucketAcl(String bucketName) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList acl = client.getBucketAcl(bucketName);
        AccessControlList retAcl = new AccessControlList();

        retAcl.setOwner(new Owner(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (com.emc.object.s3.bean.Grant g : acl.getGrants()) {
            Grantee grantee = (Grantee)g.getGrantee();
            com.amazonaws.services.s3.model.Grant newGrant =
                    new com.amazonaws.services.s3.model.Grant(grantee, Permission.parsePermission(g.getPermission().toString()));
            retAcl.getGrants().add(newGrant);
        }

        return retAcl;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws AmazonClientException {
        setBucketAcl(setBucketAclRequest.getBucketName(), setBucketAclRequest.getAcl());
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest) throws AmazonClientException {
        return getBucketAcl(getBucketAclRequest.getBucketName());
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setBucketAcl(String bucketName, AccessControlList acl) throws AmazonClientException {
        com.emc.object.s3.bean.AccessControlList aclReq = new com.emc.object.s3.bean.AccessControlList();

        aclReq.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
        for (Grant g : acl.getGrants()) {
            com.emc.object.s3.bean.Grant newGrant =
                    new com.emc.object.s3.bean.Grant((AbstractGrantee)g.getGrantee(), com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
            aclReq.getGrants().add(newGrant);
        }

        client.setBucketAcl(bucketName, aclReq);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl) throws AmazonClientException {
        client.setBucketAcl(bucketName, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Object metadata will not include certain values, restore expiration time detail
     * and SSE information.
     * </p>
     */
    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String key) throws AmazonClientException {
        S3ObjectMetadata md = client.getObjectMetadata(bucketName, key);

        ObjectMetadata ret = new ObjectMetadata();
        ret.setExpirationTime(md.getExpirationDate());
        ret.setExpirationTimeRuleId(md.getExpirationRuleId());
        ret.setLastModified(md.getLastModified());
        ret.setContentMD5(md.getContentMd5());
        ret.setContentType(md.getContentType());
        ret.setContentLength(md.getContentLength());
        ret.setCacheControl(md.getCacheControl());
        ret.setContentDisposition(md.getContentDisposition());
        ret.setContentEncoding(md.getContentEncoding());
        ret.setHttpExpiresDate(md.getHttpExpires());
        ret.setUserMetadata(md.getUserMetadata());

        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Object metadata will not include certain values, restore expiration time detail
     * and SSE information.
     * </p>
     */
    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest) throws AmazonClientException {
        return getObjectMetadata(getObjectMetadataRequest.getBucketName(), getObjectMetadataRequest.getKey());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Object metadata will not include certain values, restore expiration time detail
     * and SSE information.
     * </p>
     */
    @Override
    public S3Object getObject(String bucketName, String key) throws AmazonClientException {
        GetObjectResult gores = client.getObject(new com.emc.object.s3.request.GetObjectRequest(bucketName, key), InputStream.class);
        S3Object obj = new S3Object();
        obj.setObjectContent((InputStream) gores.getObject());
        S3ObjectMetadata oldMd = gores.getObjectMetadata();
        if (oldMd != null) {
            ObjectMetadata md = new ObjectMetadata();
            md.setLastModified(oldMd.getLastModified());
            md.setContentType(oldMd.getContentType());
            md.setContentLength(oldMd.getContentLength());
            md.setCacheControl(oldMd.getCacheControl());
            md.setContentDisposition(oldMd.getContentDisposition());
            md.setContentEncoding(oldMd.getContentEncoding());
            md.setExpirationTime(oldMd.getExpirationDate());
            md.setExpirationTimeRuleId(oldMd.getExpirationRuleId());
            md.setContentMD5(oldMd.getContentMd5());
            md.setHttpExpiresDate(oldMd.getHttpExpires());
            md.setUserMetadata(oldMd.getUserMetadata());
            obj.setObjectMetadata(md);
        }
        return obj;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Object metadata will not include certain values, restore expiration time detail
     * and SSE information.
     * </p>
     */
    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest) throws AmazonClientException {
        com.emc.object.s3.request.GetObjectRequest gor =
                new com.emc.object.s3.request.GetObjectRequest(getObjectRequest.getBucketName(), getObjectRequest.getKey());
        gor.setVersionId(getObjectRequest.getVersionId());
        if(getObjectRequest.getRange() != null) {
            gor.setRange(new Range(getObjectRequest.getRange()[0], getObjectRequest.getRange()[0] + getObjectRequest.getRange().length));
        }
        gor.setIfUnmodifiedSince(getObjectRequest.getUnmodifiedSinceConstraint());
        gor.setIfMatch(getObjectRequest.getMatchingETagConstraints().toString());
        gor.setIfModifiedSince(getObjectRequest.getModifiedSinceConstraint());
        gor.setIfNoneMatch(getObjectRequest.getNonmatchingETagConstraints().toString());

        GetObjectResult gores = client.getObject(gor, InputStream.class);

        S3Object obj = new S3Object();
        obj.setObjectContent((InputStream) gores.getObject());
        S3ObjectMetadata oldMd = gores.getObjectMetadata();
        if (oldMd != null) {
            ObjectMetadata md = new ObjectMetadata();
            md.setLastModified(oldMd.getLastModified());
            md.setContentType(oldMd.getContentType());
            md.setContentLength(oldMd.getContentLength());
            md.setCacheControl(oldMd.getCacheControl());
            md.setContentDisposition(oldMd.getContentDisposition());
            md.setContentEncoding(oldMd.getContentEncoding());
            md.setExpirationTime(oldMd.getExpirationDate());
            md.setExpirationTimeRuleId(oldMd.getExpirationRuleId());
            md.setContentMD5(oldMd.getContentMd5());
            md.setHttpExpiresDate(oldMd.getHttpExpires());
            md.setUserMetadata(oldMd.getUserMetadata());
            obj.setObjectMetadata(md);
        }
        return obj;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Object metadata will not include certain values, restore expiration time detail
     * and SSE information.
     * </p>
     */
    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File destinationFile) throws AmazonClientException {
        try {
            if (destinationFile.exists()) {
                FileOutputStream fos = new FileOutputStream(destinationFile.getAbsolutePath());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(getObject(getObjectRequest));
            }
            else {
                FileOutputStream fos = new FileOutputStream(new File(destinationFile.getAbsolutePath()));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(getObject(getObjectRequest));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return getObjectMetadata(getObjectRequest.getBucketName(), getObjectRequest.getKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest) throws AmazonClientException {
        client.deleteBucket(deleteBucketRequest.getBucketName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucket(String bucketName) throws AmazonClientException {
        client.deleteBucket(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public com.amazonaws.services.s3.model.PutObjectResult putObject(PutObjectRequest putObjectRequest) throws AmazonClientException {
        try {
            return putObject(putObjectRequest.getBucketName(), putObjectRequest.getKey(), putObjectRequest.getFile());
        }
        catch (Exception e) {
            return putObject(putObjectRequest.getBucketName(), putObjectRequest.getKey(), putObjectRequest.getInputStream(), putObjectRequest.getMetadata());
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public com.amazonaws.services.s3.model.PutObjectResult putObject(String bucketName, String key, InputStream input, ObjectMetadata metadata) throws AmazonClientException {

        com.emc.object.s3.request.PutObjectRequest por = new com.emc.object.s3.request.PutObjectRequest(bucketName, key, input);

        if (metadata != null) {
            S3ObjectMetadata md = new S3ObjectMetadata();
            md.setContentType(metadata.getContentType());
            md.setContentLength(metadata.getContentLength());
            md.setContentEncoding(metadata.getContentEncoding());
            md.setCacheControl(metadata.getCacheControl());
            md.setContentMd5(metadata.getContentMD5());
            md.setHttpExpires(metadata.getHttpExpiresDate());
            md.setExpirationDate(metadata.getExpirationTime());
            md.setContentDisposition(metadata.getContentDisposition());
            md.setUserMetadata(metadata.getUserMetadata());
            md.setExpirationRuleId(metadata.getExpirationTimeRuleId());
            por.setObjectMetadata(md);
        }

        PutObjectResult pores = client.putObject(por);

        com.amazonaws.services.s3.model.PutObjectResult ret = new com.amazonaws.services.s3.model.PutObjectResult();
        ret.setVersionId(pores.getVersionId());
        ret.setExpirationTime(pores.getExpirationDate());
        ret.setExpirationTimeRuleId(pores.getExpirationRuleId());

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyObjectResult copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws AmazonClientException {
        return copyObject(new CopyObjectRequest(sourceBucketName, sourceKey, destinationBucketName, destinationKey));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws AmazonClientException {
        com.emc.object.s3.request.PutObjectRequest req = new com.emc.object.s3.request.PutObjectRequest(copyObjectRequest.getDestinationBucketName(),
                copyObjectRequest.getDestinationKey(), getObject(copyObjectRequest.getSourceBucketName(), copyObjectRequest.getSourceKey()).getObjectContent());
        ObjectMetadata oldMd = copyObjectRequest.getNewObjectMetadata();
        if (oldMd != null) {
            S3ObjectMetadata md = new S3ObjectMetadata();
            md.setLastModified(oldMd.getLastModified());
            md.setContentType(oldMd.getContentType());
            md.setContentLength(oldMd.getContentLength());
            md.setCacheControl(oldMd.getCacheControl());
            md.setContentDisposition(oldMd.getContentDisposition());
            md.setContentEncoding(oldMd.getContentEncoding());
            md.setExpirationDate(oldMd.getExpirationTime());
            md.setExpirationRuleId(oldMd.getExpirationTimeRuleId());
            md.setContentMd5(oldMd.getContentMD5());
            md.setHttpExpires(oldMd.getHttpExpiresDate());
            md.setUserMetadata(oldMd.getUserMetadata());
            req.setObjectMetadata(md);
        }
        PutObjectResult res = client.putObject(req);

        CopyObjectResult cor = new CopyObjectResult();
        cor.setExpirationTime(res.getExpirationDate());
        cor.setExpirationTimeRuleId(res.getExpirationRuleId());
        cor.setVersionId(res.getVersionId());

        return cor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) throws AmazonClientException {
        com.emc.object.s3.request.CopyPartRequest req = new com.emc.object.s3.request.CopyPartRequest(
                copyPartRequest.getSourceBucketName(), copyPartRequest.getSourceKey(), copyPartRequest.getDestinationBucketName(),
                copyPartRequest.getDestinationBucketName(), copyPartRequest.getUploadId(), copyPartRequest.getPartNumber());
        req.setObjectMetadata(client.getObjectMetadata(copyPartRequest.getSourceBucketName(), copyPartRequest.getSourceKey()));
        com.emc.object.s3.bean.CopyPartResult res = client.copyPart(req);

        CopyPartResult ret = new CopyPartResult();
        ret.setETag(res.getETag());
        ret.setLastModifiedDate(res.getLastModified());
        ret.setPartNumber(res.getPartNumber());
        ret.setVersionId(res.getVersionId());

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteObject(String bucketName, String key) throws AmazonClientException {
        client.deleteObject(bucketName, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest) throws AmazonClientException {
        client.deleteObject(deleteObjectRequest.getBucketName(), deleteObjectRequest.getKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws AmazonClientException {
        com.emc.object.s3.request.DeleteObjectsRequest dor = new com.emc.object.s3.request.DeleteObjectsRequest(deleteObjectsRequest.getBucketName());
        com.emc.object.s3.bean.DeleteObjectsResult dObjs = client.deleteObjects(dor);
        List<DeleteObjectsResult.DeletedObject> dObjList = new ArrayList<DeleteObjectsResult.DeletedObject>();
        for (AbstractDeleteResult adr : dObjs.getResults()) {
            DeleteObjectsResult.DeletedObject dObj = new DeleteObjectsResult.DeletedObject();
            dObj.setKey(adr.getKey());
            dObj.setVersionId(adr.getVersionId());
            dObjList.add(new DeleteObjectsResult.DeletedObject());
        }
        return new DeleteObjectsResult(dObjList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVersion(String bucketName, String key, String versionId) throws AmazonClientException {
        client.deleteVersion(bucketName, key, versionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest) throws AmazonClientException {
        client.deleteVersion(deleteVersionRequest.getBucketName(), deleteVersionRequest.getKey(), deleteVersionRequest.getVersionId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketLoggingConfiguration(SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(String bucketName) throws AmazonClientException {
        return new BucketVersioningConfiguration(client.getBucketVersioning(bucketName).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBucketVersioningConfiguration(SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest) throws AmazonClientException {
        VersioningConfiguration vc = new VersioningConfiguration();
        if(setBucketVersioningConfigurationRequest.getVersioningConfiguration().getStatus().equalsIgnoreCase("Enabled")) {
            vc.setStatus(VersioningConfiguration.Status.Enabled);
        }
        else vc.setStatus(VersioningConfiguration.Status.Suspended);
        client.setBucketVersioning(setBucketVersioningConfigurationRequest.getBucketName(), vc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(String bucketName) {
        LifecycleConfiguration lc = client.getBucketLifecycle(bucketName);
        List<BucketLifecycleConfiguration.Rule> rules = new ArrayList<BucketLifecycleConfiguration.Rule>();

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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBucketLifecycleConfiguration(SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest) {
        setBucketLifecycleConfiguration(setBucketLifecycleConfigurationRequest.getBucketName(), setBucketLifecycleConfigurationRequest.getLifecycleConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucketLifecycleConfiguration(String bucketName) {
        client.deleteBucketLifecycle(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucketLifecycleConfiguration(DeleteBucketLifecycleConfigurationRequest deleteBucketLifecycleConfigurationRequest) {

    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBucketCrossOriginConfiguration(String bucketName, BucketCrossOriginConfiguration bucketCrossOriginConfiguration) {
        CorsConfiguration cc = new CorsConfiguration();

        for (CORSRule cr : bucketCrossOriginConfiguration.getRules()) {
            CorsRule ncr = new CorsRule().withId(cr.getId()).withMaxAgeSeconds(cr.getMaxAgeSeconds());
            ncr.setAllowedHeaders(cr.getAllowedHeaders());
            ncr.setAllowedOrigins(cr.getAllowedOrigins());
            ncr.setExposeHeaders(cr.getExposedHeaders());
            List<CorsMethod> cml = new ArrayList<CorsMethod>();
            for (CORSRule.AllowedMethods am : cr.getAllowedMethods()) {
                cml.add(CorsMethod.valueOf(am.toString()));
            }
            ncr.setAllowedMethods(cml);
            cc.getCorsRules().add(ncr);
        }

        client.setBucketCors(bucketName, cc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBucketCrossOriginConfiguration(SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest) {
        setBucketCrossOriginConfiguration(setBucketCrossOriginConfigurationRequest.getBucketName(), setBucketCrossOriginConfigurationRequest.getCrossOriginConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucketCrossOriginConfiguration(String bucketName) {
        client.deleteBucketCors(bucketName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBucketCrossOriginConfiguration(DeleteBucketCrossOriginConfigurationRequest deleteBucketCrossOriginConfigurationRequest) {
        client.deleteBucketCors(deleteBucketCrossOriginConfigurationRequest.getBucketName());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketTaggingConfiguration(String bucketName, BucketTaggingConfiguration bucketTaggingConfiguration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketTaggingConfiguration(SetBucketTaggingConfigurationRequest setBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketTaggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketTaggingConfiguration(DeleteBucketTaggingConfigurationRequest deleteBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketNotificationConfiguration(SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketNotificationConfiguration(String bucketName, BucketNotificationConfiguration bucketNotificationConfiguration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(GetBucketWebsiteConfigurationRequest getBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketWebsiteConfiguration(String bucketName, BucketWebsiteConfiguration configuration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketWebsiteConfiguration(SetBucketWebsiteConfigurationRequest setBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketWebsiteConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketWebsiteConfiguration(DeleteBucketWebsiteConfigurationRequest deleteBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketPolicy getBucketPolicy(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketPolicy(String bucketName, String policyText) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void setBucketPolicy(SetBucketPolicyRequest setBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketPolicy(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration) throws UnsupportedOperationException {
        return client.getPresignedUrl(bucketName, key, expiration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method) throws UnsupportedOperationException {
        PresignedUrlRequest req = new PresignedUrlRequest(Method.valueOf(method.name()), bucketName, key, expiration);
        return client.getPresignedUrl(req);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest) throws AmazonClientException {
        return generatePresignedUrl(generatePresignedUrlRequest.getBucketName(), generatePresignedUrlRequest.getKey(),
                generatePresignedUrlRequest.getExpiration(), generatePresignedUrlRequest.getMethod());
    }

    /**
     * {@inheritDoc}
     */
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
            smd.setUserMetadata(omd.getUserMetadata());
            req.setObjectMetadata(smd);
        }

        if(request.getAccessControlList() != null) {
            AccessControlList acl = request.getAccessControlList();
            com.emc.object.s3.bean.AccessControlList reqAcl = new com.emc.object.s3.bean.AccessControlList();
            reqAcl.setOwner(new CanonicalUser(acl.getOwner().getId(), acl.getOwner().getDisplayName()));
            for (com.amazonaws.services.s3.model.Grant g : acl.getGrants()) {
                AbstractGrantee grantee = (AbstractGrantee)g.getGrantee();
                com.emc.object.s3.bean.Grant newGrant =
                        new com.emc.object.s3.bean.Grant(grantee, com.emc.object.s3.bean.Permission.valueOf(g.getPermission().toString()));
                reqAcl.getGrants().add(newGrant);
            }
        }
        else if (request.getCannedACL() != null) {
            req.setCannedAcl(CannedAcl.fromHeaderValue(request.getCannedACL().toString()));
        }

        com.emc.object.s3.bean.InitiateMultipartUploadResult imur = client.initiateMultipartUpload(req);

        InitiateMultipartUploadResult ret = new InitiateMultipartUploadResult();
        ret.setBucketName(imur.getBucketName());
        ret.setKey(imur.getKey());
        ret.setUploadId(imur.getUploadId());
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadPartResult uploadPart(UploadPartRequest request) throws AmazonClientException {

        MultipartPart mp;
        if (request.getInputStream() != null) {
            mp = client.uploadPart(new com.emc.object.s3.request.UploadPartRequest(
                    request.getBucketName(), request.getKey(), request.getUploadId(),
                    request.getPartNumber(), request.getInputStream()).withContentLength(request.getPartSize()));
        }
        else {
            mp = client.uploadPart(new com.emc.object.s3.request.UploadPartRequest(
                    request.getBucketName(), request.getKey(), request.getUploadId(),
                    request.getPartNumber(), request.getFile()).withContentLength(request.getPartSize()));
        }

        UploadPartResult ret = new UploadPartResult();
        ret.setETag(mp.getETag());
        ret.setPartNumber(mp.getPartNumber());
        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public PartListing listParts(ListPartsRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) throws AmazonClientException {
        client.abortMultipartUpload(new com.emc.object.s3.request.AbortMultipartUploadRequest(
                request.getBucketName(), request.getKey(), request.getUploadId()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) throws AmazonClientException {
        com.emc.object.s3.request.CompleteMultipartUploadRequest req = new com.emc.object.s3.request.CompleteMultipartUploadRequest(
                request.getBucketName(), request.getKey(), request.getUploadId());
        List<MultipartPart> mpList = new ArrayList<MultipartPart>();
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
     * {@inheritDoc}
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

        List<MultipartUpload> mpuList = new ArrayList<MultipartUpload>();
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

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public S3ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void restoreObject(RestoreObjectRequest copyGlacierObjectRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void restoreObject(String bucketName, String key, int expirationInDays) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void enableRequesterPays(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public void disableRequesterPays(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not supported by smart client.</b>
     * </p>
     */
    @Override
    public boolean isRequesterPaysEnabled(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}