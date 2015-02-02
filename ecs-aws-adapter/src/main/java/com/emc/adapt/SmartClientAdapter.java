package com.emc.adapt;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.bean.PutObjectResult;
import com.emc.object.s3.jersey.S3JerseyClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.regions.Region;

/**
 * Amazon S3 adapter for ECS smart client.
 */
public class SmartClientAdapter implements AmazonS3 {

    protected S3Client client;
    protected S3Config config;

    /**
     * Constructor for AWS adapter.
     *
     * @param config S3Config object containing endpoint and access
     *               credentials for client.
     */
    public SmartClientAdapter(S3Config config) {
        this.config = config;
        this.client = new S3JerseyClient(this.config);
    }

    @Override
    public void setEndpoint(String endpoints) {
        throw new UnsupportedOperationException("endpoint[s] can only be set in the constructor of this adapter");
    }

    /**
     * Sets AWS region for communications. Not supported by smart client.
     *
     * @param region Region this client will communicate with.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setRegion(Region region) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Override current S3 Client options for this client. Not supported by smart client.
     *
     * @param clientOptions S3 client options to use.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setS3ClientOptions(S3ClientOptions clientOptions) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Changes S3 storage class for the specified object. Not supported by smart client.
     *
     * @param bucketName Bucket containing the target object.
     * @param key Key of object within specified bucket.
     * @param newStorageClass New storage class for specified object.
     * @throws UnsupportedOperationException
     */
    @Override
    public void changeObjectStorageClass(String bucketName, String key, StorageClass newStorageClass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Changes redirect location for the specified object. Not supported by smart client.
     *
     * @param bucketName Bucket containing the target object.
     * @param key Key of object within specified bucket.
     * @param newRedirectLocation New redirect location for the specified object.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setObjectRedirectLocation(String bucketName, String key, String newRedirectLocation) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns list of summary information about objects in specified bucket.
     *
     * @param bucketName Name of bucket to list.
     * @return Listing of objects in the specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

        return ol;
    }

    /**
     * Returns list of summary information about objects in specified bucket.
     *
     * @param bucketName Name of bucket to list.
     * @param prefix Optional; restricts response to keys beginning with this prefix.
     * @return Listing of objects in the specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

        return ol;
    }

    /**
     * Returns list of summary information about objects in specified bucket.
     *
     * @param listObjectsRequest Request object containing all options for listing objects.
     * @return Listing of objects in the specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

        return ol;
    }

    /**
     * Provides easy way to continue truncated Objectlisting with next result page.
     * Not supported by smart client.
     *
     * @param previousObjectListing Previous truncated ObjectListing.
     * @return Next set of ObjectListing results following previousObjectListing.
     * @throws java.lang.UnsupportedOperationException
     */
    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a list of summary information about the versions in the specified bucket.
     *
     * @param bucketName Name of bucket for which versions are to be listed.
     * @param prefix Optional; restricts response to keys beginning with this prefix.
     * @return Listing of versions inthe specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Provides easy way to continue truncated VersionListing with next result page.
     * Not supported by smart client.
     *
     * @param previousVersionListing Previous truncated VersionListing
     * @return Next set of VersionListing results following previous VersionListing
     * @throws UnsupportedOperationException
     */
    @Override
    public VersionListing listNextBatchOfVersions(VersionListing previousVersionListing) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a list of summary information about the versions in the specified bucket.
     *
     * @param bucketName Name of bucket for which versions are to be listed.
     * @param prefix Optional; restricts response to keys beginning with this prefix.
     * @param keyMarker Optional; indicates where in version list to begin returning results.
     *                  Without versionIDMarker, results beging immediately after this key's
     *                  last version.
     * @param versionIdMarker Optional; indicates where in version list to begin returning results.
     *                        Key marker must also be specified. Listing results begins immediately
     *                        following version with specified key and version ID.
     * @param delimiter Optional; causes keys containing same string between prefix and first occurence
     *                  of delimiter to be condensed into single result element.
     * @param maxResults Optional; indicates maximum number of results in the response.
     * @return Listing of versions inthe specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Returns a list of summary information about the versions in the specified bucket.
     *
     * @param listVersionsRequest Request object containing all options for version listing.
     * @return Listing of versions inthe specified bucket, along with associated information
     * including common prefixes, original request parameters, etc.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Gets the current owner of the AWS account used by the sender of the request.
     * Not supported by smart client.
     *
     * @return Account of the authenticated sender.
     * @throws UnsupportedOperationException
     */
    @Override
    public Owner getS3AccountOwner() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the specified bucket already exists.
     *
     * @param bucketName Name of the bucket to check.
     * @return True if the specified bucket exists.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public boolean doesBucketExist(String bucketName) throws AmazonClientException {
        return client.bucketExists(bucketName);
    }

    /**
     * Returns a list of all buckets accessible to the requester.
     *
     * @return List of all buckets accessible to the requester.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * Returns a list of all buckets currently accessible to the requester.
     *
     * @param listBucketsRequest Request object containing all options related to bucket listing.
     * @return List of all bucekts currently accessible to the requester.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest) throws AmazonClientException {
        return listBuckets();
    }

    /**
     * Gets the geographical location where the bucket is stored.
     *
     * @param bucketName Name of bucket to look up.
     * @return Location where the specified bucket is stored.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public String getBucketLocation(String bucketName) throws AmazonClientException {
        return client.getBucketLocation(bucketName).getRegion().getConstraint();

    }

    /**
     * Gets the geographical location where the bucket is stored.
     *
     * @param getBucketLocationRequest Request object containing name of bucket to look up.
     * @return Location where the specified bucket is stored.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public String getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) throws AmazonClientException {
        return getBucketLocation(getBucketLocationRequest.getBucketName());
    }

    /**
     * Creates a new bucket in the default region.
     *
     * @param createBucketRequest Request object containing all options for creating a bucket.
     * @return New bucket with requested name.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest) throws AmazonClientException {
        client.createBucket(createBucketRequest.getBucketName());
        return new Bucket(createBucketRequest.getBucketName());
    }

    /**
     * Creates a new bucket in the default region.
     *
     * @param bucketName Name of the bucket to create.
     * @return New bucket with requested name.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public Bucket createBucket(String bucketName) throws AmazonClientException {
        client.createBucket(bucketName);
        return new Bucket(bucketName);
    }

    /**
     * Creates a new bucket in the specified region.
     *
     * @param bucketName Name of the bucket to create.
     * @param region Region in which to create bucket.
     * @return New bucket with requested name in specified region.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public Bucket createBucket(String bucketName, com.amazonaws.services.s3.model.Region region) throws AmazonClientException {
        return createBucket(new CreateBucketRequest(bucketName, region.toString()));
    }

    /**
     * Creates a new bucket in the specified region.
     *
     * @param bucketName Name of the bucket to create.
     * @param region Region in which to create bucket.
     * @return New bucket with requested name in specified region.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public Bucket createBucket(String bucketName, String region) throws AmazonClientException {
        return createBucket(bucketName);
    }

    /**
     * Gets the AccessControlList for the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose ACL is being retrieved.
     * @return The AccessControlList for the specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Gets the AccessControlList for the specified version of the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose ACL is being retrieved.
     * @param versionId Version ID of object version for which to retrieve ACL.
     * @return The AccessControlList for the specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public AccessControlList getObjectAcl(String bucketName, String key, String versionId) throws AmazonClientException {
        return getObjectAcl(bucketName, key);
    }

    /**
     * Sets the access control list for the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose access control list is being set.
     * @param acl New AccessControlList for specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Sets the canned access control list for the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose canned access control list is being set.
     * @param acl New CannedAccessControlList for specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void setObjectAcl(String bucketName, String key, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     * Sets the access control list for the specified version of the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose access control list is being set.
     * @param versionId Version ID of object version for which to retrieve ACL.
     * @param acl New AccessControlList for specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, AccessControlList acl) throws AmazonClientException {
        setObjectAcl(bucketName, key, acl);
    }

    /**
     * Sets the canned access control list for the specified version of the specified object.
     *
     * @param bucketName Name of bucket containing the target object.
     * @param key Name of object whose canned access control list is being set.
     * @param versionId Version ID of object version for which to retrieve ACL.
     * @param acl New CannedAccessControlList for specified object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, CannedAccessControlList acl) throws AmazonClientException {
        client.setObjectAcl(bucketName, key, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     * Gets the AccessControlList for the specified bucket.
     *
     * @param bucketName Bucket for which to retrieve the AccessControlList.
     * @return AccessControlList for the specified bucket.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Sets the access control list for the specified bucket.
     *
     * @param setBucketAclRequest Request object containing the bucket to modify and
     *                            new AccessControlList to set.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws AmazonClientException {
        setBucketAcl(setBucketAclRequest.getBucketName(), setBucketAclRequest.getAcl());
    }

    /**
     * Gets the AccessControlList for the specified bucket.
     *
     * @param getBucketAclRequest Request object containing the name of the bucket for which to
     *                            retrieve the AccessControlList
     * @return AccesssControlList for the specified bucket.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest) throws AmazonClientException {
        return getBucketAcl(getBucketAclRequest.getBucketName());
    }

    /**
     * Sets the access control list for the specified bucket.
     *
     * @param bucketName Bucket to modify.
     * @param acl New access control list for the specified bucket.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Sets the canned access control list for the specified bucket.
     *
     * @param bucketName Bucket to modify.
     * @param acl New canned access control list for the specified bucket.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl) throws AmazonClientException {
        client.setBucketAcl(bucketName, CannedAcl.fromHeaderValue(acl.toString()));
    }

    /**
     * Retrieves object metadata for the specified object without fetching the object itself.
     *
     * @param bucketName Name of the bucket containing the target object.
     * @param key Key of the object for which to retrieve metadata.
     * @return Appropriate metadata for specified object. Will not include certain fields, including
     * UserMetaData, ServerSideEncryption, Header, and OngoingRestore objects.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

        return ret;
    }

    /**
     * Retrieves object metadata for the specified object without fetching the object itself.
     *
     * @param getObjectMetadataRequest Request object specifying bucket name, key, and version ID
     *                                 of object for which to retrieve metadata.
     * @return Appropriate metadata for specified object. Will not include certain fields, including
     * UserMetaData, ServerSideEncryption, Header, and OngoingRestore objects.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest) throws AmazonClientException {
        return getObjectMetadata(getObjectMetadataRequest.getBucketName(), getObjectMetadataRequest.getKey());
    }

    /**
     * Gets the object stored under the specified bucket and key.
     *
     * @param bucketName Name of the bucket containing the target object.
     * @param key Key under which target object is stored.
     * @return Object stored under the specified bucket and key.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public S3Object getObject(String bucketName, String key) throws AmazonClientException {
        return client.readObject(bucketName, key, S3Object.class);
    }

    /**
     * Gets the object stored under the specified bucket and key.
     *
     * @param getObjectRequest Request object containing options for object retrieval.
     * @return Object stored under the specified bucket and key.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest) throws AmazonClientException {
        return getObject(getObjectRequest.getBucketName(), getObjectRequest.getKey());
    }

    /**
     * Gets object metadata for the specified object and saves object contents to specified file.
     *
     * @param getObjectRequest Request object containing options for object retrieval.
     * @param destinationFile File wherein to save object content.
     * @return Appropriate metadata for specified object. Will not include certain fields, including
     * UserMetaData, ServerSideEncryption, Header, and OngoingRestore objects.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File destinationFile) throws AmazonClientException {
        try {
            FileOutputStream fos = new FileOutputStream(destinationFile.getAbsolutePath());
            ObjectOutputStream oos =new ObjectOutputStream(fos);
            oos.writeObject(getObject(getObjectRequest));
        } catch (Exception e) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(destinationFile.getAbsolutePath()));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(getObject(getObjectRequest));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return getObjectMetadata(getObjectRequest.getBucketName(), getObjectRequest.getKey());
    }

    /**
     * Deletes the specified bucket.
     *
     * @param deleteBucketRequest Request object containing all options for bucket deletion.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest) throws AmazonClientException {
        client.deleteBucket(deleteBucketRequest.getBucketName());
    }

    /**
     * Deletes the specified bucket.
     *
     * @param bucketName Name of the bucket to delete.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteBucket(String bucketName) throws AmazonClientException {
        client.deleteBucket(bucketName);
    }

    /**
     * Uploads a new object to the specified bucket.
     *
     * @param putObjectRequest Request object containing all options for object upload.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Uploads a new object to the specified bucket.
     *
     * @param bucketName Name of existing bucket to which to write object.
     * @param key Key under which to store file.
     * @param file File containing data to be uploaded.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Uploads a new object to the specified bucket.
     *
     * @param bucketName Name of existing bucket to which to write object.
     * @param key Key under which to store data.
     * @param input Input stream containing data to be uploaded.
     * @param metadata Additional metadata to inform data handling
     *                 (e.g. custom user metadata, content type specification, etc.)
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * Copies a source object to a new destination.
     *
     * @param sourceBucketName Name of bucket containing source object.
     * @param sourceKey Key under which source object is stored.
     * @param destinationBucketName Name of bucket to which to copy object.
     * @param destinationKey Key under which to store copied object.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * Copies a source object to a new destination.
     *
     * @param copyObjectRequest Request object containing all options for copying object.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws AmazonClientException {
        return copyObject(copyObjectRequest.getSourceBucketName(), copyObjectRequest.getSourceKey(),
                copyObjectRequest.getDestinationBucketName(), copyObjectRequest.getDestinationKey());
    }

    /**
     * Copies a cource object to a part of a multipart upload.
     *
     * @param copyPartRequest Request object containing all options for copying object.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * Deletes the specified object from the specified bucket.
     *
     * @param bucketName Bucket from which to delete object.
     * @param key Key of object to be deleted.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteObject(String bucketName, String key) throws AmazonClientException {
        client.deleteObject(bucketName, key);
    }

    /**
     * Deletes the specified object from the specified bucket.
     *
     * @param deleteObjectRequest Request object containing all options for object deletion.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest) throws AmazonClientException {
        client.deleteObject(deleteObjectRequest.getBucketName(), deleteObjectRequest.getKey());
    }

    /**
     * Deletes multiple object from the specified bucket.
     *
     * @param deleteObjectsRequest Request object contiaining all options for object deletion.
     * @return Result object containing all information returned by client while handling request.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * Deletes specified version of specified object from specified bucket.
     *
     * @param bucketName Bucket from which to delete object.
     * @param key Key of object to be deleted.
     * @param versionId Object version to be deleted.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteVersion(String bucketName, String key, String versionId) throws AmazonClientException {
        client.deleteVersion(bucketName, key, versionId);
    }

    /**
     * Deletes specified version of specified object from specified bucket.
     *
     * @param deleteVersionRequest Request object containing all options for bucket deletion.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest) throws AmazonClientException {
        client.deleteVersion(deleteVersionRequest.getBucketName(), deleteVersionRequest.getKey(), deleteVersionRequest.getVersionId());
    }

    /**
     * Gets logging configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve logging configuration.
     * @return Bucket logging configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets logging configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketLoggingConfigurationRequest Request object containing all options for
     *                                             setting bucket logging configuration.
     * @return Bucket logging configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketLoggingConfiguration(SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets versioning configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve versioning configuration.
     * @return Bucket versioning configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(String bucketName) throws AmazonClientException {
        return new BucketVersioningConfiguration(client.getBucketVersioning(bucketName).toString());
    }

    /**
     * Sets versioning configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketVersioningConfigurationRequest Request object containing all options for
     *                                                setting bucket versioning configuration.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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
     * Gets lifecycle configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve lifecycle configuration.
     * @return Bucket lifecycle configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
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

    /**
     * Sets lifecycle configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to set lifecycle configuration.
     * @param bucketLifecycleConfiguration New bucket lifecylce configuration for target bucket.
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
     * Sets lifecycle configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketLifecycleConfigurationRequest Request object containing name of target bucket and
     *                                                 new bucket lifecycle configuration.
     */
    @Override
    public void setBucketLifecycleConfiguration(SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest) {
        setBucketLifecycleConfiguration(setBucketLifecycleConfigurationRequest.getBucketName(), setBucketLifecycleConfigurationRequest.getLifecycleConfiguration());
    }

    /**
     * Deletes lifecycle configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to delete lifecycle configuration.
     */
    @Override
    public void deleteBucketLifecycleConfiguration(String bucketName) {
        client.deleteBucketLifecycle(bucketName);
    }

    /**
     * Deletes lifecycle configuraton for the specified bucket.
     * Not supported by smart client.
     *
     * @param deleteBucketLifecycleConfigurationRequest Request object containing name of bucket
     *                                                  for which to delete lifecycle configuration.
     */
    @Override
    public void deleteBucketLifecycleConfiguration(DeleteBucketLifecycleConfigurationRequest deleteBucketLifecycleConfigurationRequest) {

    }

    /**
     * Gets cross origin configuration for the specified bucket.
     *
     * @param bucketName Bucket for which to retreive cross origin information.
     * @return Cross origin configuration for the specified bucket.
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
     * Sets cross origin configuration for specified bucket.
     *
     * @param bucketName Name of bucket for which to set cross origin configuration.
     * @param bucketCrossOriginConfiguration New cross origin configuration for the specified bucket.
     */
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

    /**
     * Sets cross origin configuration for specified bucket.
     *
     * @param setBucketCrossOriginConfigurationRequest Request object containing name of target bucket and
     *                                                 new cross origin configuration.
     */
    @Override
    public void setBucketCrossOriginConfiguration(SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest) {
        setBucketCrossOriginConfiguration(setBucketCrossOriginConfigurationRequest.getBucketName(), setBucketCrossOriginConfigurationRequest.getCrossOriginConfiguration());
    }

    /**
     * Deletes cross origin configuration for specified bucket.
     *
     * @param bucketName Name of bucket for which to delete cross origin configuration.
     */
    @Override
    public void deleteBucketCrossOriginConfiguration(String bucketName) {
        client.deleteBucketCors(bucketName);
    }

    /**
     * Deletes cross origin configuration for specified bucket.
     *
     * @param deleteBucketCrossOriginConfigurationRequest Request object contanining name of bucket for
     *                                                    which to delete cross origin configuration.
     */
    @Override
    public void deleteBucketCrossOriginConfiguration(DeleteBucketCrossOriginConfigurationRequest deleteBucketCrossOriginConfigurationRequest) {
        client.deleteBucketCors(deleteBucketCrossOriginConfigurationRequest.getBucketName());
    }

    /**
     * Gets bucket tagging configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve bucket tagging configuration.
     * @return Bucket tagging configuration of the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket tagging configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to set bucket tagging configuration.
     * @param bucketTaggingConfiguration New bucket tagging configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketTaggingConfiguration(String bucketName, BucketTaggingConfiguration bucketTaggingConfiguration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket tagging configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketTaggingConfigurationRequest Request object containing name of target bucket and
     *                                             new bucket tagging configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketTaggingConfiguration(SetBucketTaggingConfigurationRequest setBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket tagging configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to delete bucket tagging configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketTaggingConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket tagging configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param deleteBucketTaggingConfigurationRequest Request object containing name of bucket for which to
     *                                                delete tagging configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketTaggingConfiguration(DeleteBucketTaggingConfigurationRequest deleteBucketTaggingConfigurationRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets bucket notification configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve bucket notification configuration.
     * @return Bucket notification configuration of the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket notification configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketNotificationConfigurationRequest Request object containing name of target bucket and
     *                                                  new bucket notification configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketNotificationConfiguration(SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket notification configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to set bucket notification configuration.
     * @param bucketNotificationConfiguration New bucket notification configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketNotificationConfiguration(String bucketName, BucketNotificationConfiguration bucketNotificationConfiguration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve bucket website configuration.
     * @return Bucket website configuration for the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param getBucketWebsiteConfigurationRequest Request object containing all information on bucket
     *                                             for which to retrieve website configuration.
     * @return Bucket website configuration for the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(GetBucketWebsiteConfigurationRequest getBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to set bucket website configuration.
     * @param configuration New website configuration for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketWebsiteConfiguration(String bucketName, BucketWebsiteConfiguration configuration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketWebsiteConfigurationRequest Request containing name of bucket for which to set
     *                                             bucket website configuration and new website configuration
     *                                             object for specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketWebsiteConfiguration(SetBucketWebsiteConfigurationRequest setBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to delete bucket website configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketWebsiteConfiguration(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket website configuration for the specified bucket.
     * Not supported by smart client.
     *
     * @param deleteBucketWebsiteConfigurationRequest Request object specifying name of bucket
     *                                                for which to delete bucket website configuration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketWebsiteConfiguration(DeleteBucketWebsiteConfigurationRequest deleteBucketWebsiteConfigurationRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to retrieve policy.
     * @return Bucket policy for the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketPolicy getBucketPolicy(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param getBucketPolicyRequest Request object containing all options for bucket policy retrieval.
     * @return Bucket policy for the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to set policy.
     * @param policyText Policy to apply to the specified bucket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketPolicy(String bucketName, String policyText) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets bucket policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param setBucketPolicyRequest Request object containing details of bucket
     *                               and policy to apply.
     * @throws UnsupportedOperationException
     */
    @Override
    public void setBucketPolicy(SetBucketPolicyRequest setBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket for which to delete policy.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketPolicy(String bucketName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes bucket policy for the specified bucket.
     * Not supported by smart client.
     *
     * @param deleteBucketPolicyRequest Request object containing all options for bucket policy deletion.
     * @throws UnsupportedOperationException
     */
    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a pre-signed url for accessing the specified resource.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket containing target object.
     * @param key Key under which target object is stored.
     * @param expiration Time at which pre-signed URL will expire.
     * @return Pre-signed URL which expires at the specified time and can be
     * used publicly to retrieve the specified object.
     * @throws UnsupportedOperationException
     */
    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a pre-signed url for accessing the specified resource.
     * Not supported by smart client.
     *
     * @param bucketName Name of bucket containing target object.
     * @param key Key under which target object is stored.
     * @param expiration Time at which pre-signed URL will expire.
     * @param method HTTP method verb to be used.
     * @return Pre-signed URL which expires at the specified time and can be
     * used publicly to retrieve the specified object.
     * @throws UnsupportedOperationException
     */
    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a pre-signed url for accessing the specified resource.
     * Not supported by smart client.
     *
     * @param generatePresignedUrlRequest Request object containing all options for generating
     *                                    pre-signed URL for a specified resource.
     * @return Pre-signed URL which expires at the specified time and can be
     * used publicly to retrieve the specified object.
     * @throws UnsupportedOperationException
     */
    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Initiates a multipart upload and returns a result object containing an Upload ID.
     * Upload ID associates all parts in the specific upload and is used in UploadPart resuests.
     *
     * @param request Request object specifying all options for initiating this multipart upload.
     * @return InitiateMultipartUploadResult object for this operation.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

        req.setCannedAcl(CannedAcl.fromHeaderValue(request.getCannedACL().toString()));
        com.emc.object.s3.bean.InitiateMultipartUploadResult imur = client.initiateMultipartUpload(req);

        InitiateMultipartUploadResult ret = new InitiateMultipartUploadResult();
        ret.setBucketName(imur.getBucketName());
        ret.setKey(imur.getKey());
        ret.setUploadId(imur.getUploadId());
        return ret;
    }

    /**
     * Uploads a part in a multipart upload. Multipart upload must first be initiated before
     * any individual part may be uploaded.
     *
     * @param request Request object specifying all options for part upload.
     * @return Result object containing part number and ETag of uploaded part.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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

    /**
     * List parts that have been uploaded for a specific multipart upload.
     *
     * @param request Request object specifying all options for uploading part.
     * @return PartListing object for this operation.
     * @throws UnsupportedOperationException
     */
    @Override
    public PartListing listParts(ListPartsRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Aborts a multipart upload. After this operation, no additional parts may be uploaded using
     * the specified upload ID.
     *
     * @param request Request object specifying all options for aborting this multipart upload.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) throws AmazonClientException {
        client.abortMultipartUpload(new com.emc.object.s3.request.AbortMultipartUploadRequest(
                request.getBucketName(), request.getKey(), request.getUploadId()));

    }

    /**
     * Completes a multipart upload by assembling constituent parts.
     *
     * @param request Resuest object specifying all options for multipart upload conpletion.
     * @return Result object containing ETag for completed object.
     * @throws AmazonClientException If errors are encountered in client while handling the request.
     */
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
     * @throws AmazonClientException If errors are encountered in client while handling the request.
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

    /**
     * Gets additional metadata for a pervious successful request.
     * Not supported by smart client.
     *
     * @param request Originally executed request.
     * @return Response metadata for the specified request.
     * @throws UnsupportedOperationException
     */
    @Override
    public S3ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Temporarily restores an object transitioned to Glacier back into S3.
     * Not supported by smart client.
     *
     * @param copyGlacierObjectRequest Request object specifying all options for object restoration.
     * @throws UnsupportedOperationException
     */
    @Override
    public void restoreObject(RestoreObjectRequest copyGlacierObjectRequest) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Temporarily restores an object transitioned to Glacier back into S3.
     * Not supported by smart client.
     *
     * @param bucketName Name of an existing bucket.
     * @param key Key under which to store target object.
     * @param expirationInDays Number of days after which object will expire.
     * @throws UnsupportedOperationException
     */
    @Override
    public void restoreObject(String bucketName, String key, int expirationInDays) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableRequesterPays(String bucketName) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disableRequesterPays(String bucketName) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequesterPaysEnabled(String bucketName) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }
}