package com.emc.object.s3;

import com.emc.object.Range;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.request.*;

public interface S3Client {
    ListDataNode listDataNodes();

    /**
     * Lists the buckets owned by the configured identity
     */
    ListBucketsResult listBuckets();

    ListBucketsResult listBuckets(ListBucketsRequest request);

    boolean bucketExists(String bucketName);

    void createBucket(String bucketName);

    void createBucket(CreateBucketRequest request);

    void deleteBucket(String bucketName);

    void setBucketAcl(String bucketName, AccessControlList acl);

    void setBucketAcl(String bucketName, CannedAcl cannedAcl);

    void setBucketAcl(SetBucketAclRequest request);

    AccessControlList getBucketAcl(String bucketName);

    void setBucketCors(String bucketName, CorsConfiguration corsConfiguration);

    CorsConfiguration getBucketCors(String bucketName);

    void deleteBucketCors(String bucketName);

    void setBucketLifecycle(String bucketName, LifecycleConfiguration lifecycleConfiguration);

    LifecycleConfiguration getBucketLifecycle(String bucketName);

    void deleteBucketLifecycle(String bucketName);

    LocationConstraint getBucketLocation(String bucketName);

    void setBucketVersioning(String bucketName, VersioningConfiguration versioningConfiguration);

    VersioningConfiguration getBucketVersioning(String bucketName);

    ListMultipartUploadsResult listMultipartUploads(String bucketName);

    ListMultipartUploadsResult listMultipartUploads(ListMultipartUploadsRequest request);

    ListObjectsResult listObjects(String bucketName);

    ListObjectsResult listObjects(String bucketName, String prefix);

    ListObjectsResult listObjects(ListObjectsRequest request);

    ListVersionsResult listVersions(String bucketName, String prefix);

    ListVersionsResult listVersions(ListVersionsRequest request);

    /**
     * Will fail if object exists
     */
    void createObject(String bucketName, String key, Object content, String contentType);

    /**
     * Will fail if object does not exist
     */
    void updateObject(String bucketName, String key, Range range, Object content);

    void putObject(PutObjectRequest request);

    <T> T readObject(String bucketName, String key, Class<T> objectType);

    void deleteObject(String bucketName, String key);
}
