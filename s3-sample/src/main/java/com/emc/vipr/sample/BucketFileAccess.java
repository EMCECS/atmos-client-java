package com.emc.vipr.sample;

import java.util.*;
import java.util.concurrent.TimeoutException;

import com.amazonaws.util.StringInputStream;
import com.emc.vipr.services.s3.ViPRS3Client;
import com.emc.vipr.services.s3.model.*;
import com.emc.vipr.services.s3.model.Object;

/**
 * EMC ViPR data services includes an option to export S3 buckets and Swift
 * containers over NFS.  The set of objects exported in this manor is fixed and new
 * objects added via the REST protocol will not be visible in existing exports.
 * Conversely, you cannot create or delete objects in the exported mount points,
 * although when mounted read-write, you can modify existing objects.  While
 * objects are available over NFS, they are inaccessible via REST and vice versa.
 * <p/>
 * The set of objects to be exported is defined by start-token and end-token parameters, which
 * represent specific points in time for the bucket (bucket versions). These
 * tokens are returned from any call ("get" or "set") and one token may be used in subsequent
 * "set" calls with the following outcome:
 * <p/>
 * when setting mode to readOnly or readWrite and a token is included,
 * only enable access for objects *newer* than the token.
 * <p/>
 * when setting mode to disabled and a token is included, only disable
 * access for objects *older* than the token.
 * <p/>
 * note that the tokens (bucket versions) are inclusive, so it is possible that start-token = end-token (this will
 * likely include only one object).
 * <p/>
 * Leaving out the token in a request signifies that all objects in the bucket should be
 * affected by the operation.
 * <p/>
 * Using the "set" call to enable access for new objects and disable access for
 * old objects, you can effectively establish a sliding window (over time) of
 * objects available via NFS for a given bucket.
 */
public class BucketFileAccess {
    private ViPRS3Client s3;

    public BucketFileAccess() {
        this.s3 = S3ClientFactory.getS3Client();
    }

    public void runSample() throws Exception {
        String bucketName = "temp.vipr-fileaccess";
        String key1 = "test1.txt";
        String key2 = "test2.txt";
        String key3 = "test3.txt";
        String key4 = "test4.txt";
        String key5 = "test5.txt";
        String key6 = "test6.txt";
        String content = "Hello World!";
        String clientHost = "10.10.10.10"; // change to a real client to test
        String clientUid = "501"; // change to your client uid to test
        long fileAccessDuration = 20; // seconds

        try {
            // create some objects
            s3.createBucket(bucketName);
            s3.putObject(bucketName, key1, new StringInputStream(content), null);
            s3.putObject(bucketName, key2, new StringInputStream(content), null);
            s3.putObject(bucketName, key3, new StringInputStream(content), null);
            s3.putObject(bucketName, key4, new StringInputStream(content), null);

            SetBucketFileAccessModeRequest request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.readWrite);
            request.setDuration(fileAccessDuration);
            request.setHostList(Arrays.asList(clientHost));
            request.setUid(clientUid);

            // switch to NFS access for current objects the bucket
            SampleUtils.log("Enabling NFS access (workflow A)");
            BucketFileAccessModeResult result = s3.setBucketFileAccessMode(request);

            // the token is the bucket version. it represents a point in time. objects older than this token are now
            // exported via NFS. any objects created after this point in time will not be accessible via NFS, but *will*
            // be accessible via REST.
            String tokenA = result.getEndToken();
            SampleUtils.log("Token: %s", tokenA);

            // wait until complete (change is asynchronous)
            SampleUtils.log("Waiting for bucket mode to change...");
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.readWrite, 30000);
            SampleUtils.log("Change complete; bucket is now in NFS read-write mode");

            // now the bucket should be accessible via NFS, let's get the details
            GetFileAccessRequest fileAccessRequest = new GetFileAccessRequest();
            fileAccessRequest.setBucketName(bucketName);
            GetFileAccessResult fileAccessResult = s3.getFileAccess(fileAccessRequest);

            // here are your mount points and objects
            Map<String, List<com.emc.vipr.services.s3.model.Object>> mountMap
                    = new HashMap<String, List<com.emc.vipr.services.s3.model.Object>>();
            for (com.emc.vipr.services.s3.model.Object object : fileAccessResult.getObjects()) {
                List<com.emc.vipr.services.s3.model.Object> objects = mountMap.get(object.getDeviceExport());
                if (objects == null) {
                    objects = new ArrayList<com.emc.vipr.services.s3.model.Object>();
                    mountMap.put(object.getDeviceExport(), objects);
                }
                objects.add(object);
            }

            SampleUtils.log("NFS mount points and objects (workflow A):");
            for (String export : mountMap.keySet()) {
                SampleUtils.log("> %s", export);
                for (Object object : mountMap.get(export)) {
                    SampleUtils.log("> > %s => %s", object.getName(), object.getRelativePath());
                }
            }

            // create more objects (part of a new workflow in the bucket)
            s3.putObject(bucketName, key5, new StringInputStream(content), null);
            s3.putObject(bucketName, key6, new StringInputStream(content), null);

            // now, switch to NFS access for the new objects using the end-token returned from the last call.
            // when enabling NFS access, all objects *newer* than the token are included in the NFS export.
            SampleUtils.log("Enabling NFS access (workflow B)");
            request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.readWrite);
            request.setDuration(fileAccessDuration);
            request.setHostList(Arrays.asList(clientHost));
            request.setUid(clientUid);
            request.setToken(tokenA);
            s3.setBucketFileAccessMode(request);

            String tokenB = result.getEndToken();
            SampleUtils.log("End-token for workflow B: %s", tokenB);

            // wait until complete (change is asynchronous)
            SampleUtils.log("Waiting for bucket mode to change...");
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.readWrite, 30000);
            SampleUtils.log("Change complete; bucket is now in NFS read-write mode");

            // now the bucket should be accessible via NFS, let's get the details
            fileAccessRequest = new GetFileAccessRequest();
            fileAccessRequest.setBucketName(bucketName);
            fileAccessResult = s3.getFileAccess(fileAccessRequest);

            // here are your mount points and objects
            mountMap = new HashMap<String, List<com.emc.vipr.services.s3.model.Object>>();
            for (com.emc.vipr.services.s3.model.Object object : fileAccessResult.getObjects()) {
                List<com.emc.vipr.services.s3.model.Object> objects = mountMap.get(object.getDeviceExport());
                if (objects == null) {
                    objects = new ArrayList<com.emc.vipr.services.s3.model.Object>();
                    mountMap.put(object.getDeviceExport(), objects);
                }
                objects.add(object);
            }

            SampleUtils.log("NFS mount points and objects (workflow B):");
            for (String export : mountMap.keySet()) {
                SampleUtils.log("> %s", export);
                for (Object object : mountMap.get(export)) {
                    SampleUtils.log("> > %s => %s", object.getName(), object.getRelativePath());
                }
            }

            // NFS access for a bucket is enabled only for objects between a starting and ending bucket version
            // (start-token and end-token). at this point in the sample, there are two simultaneous sequential NFS
            // "workflows" for the same bucket. you can have as many of these workflows as you want so long as they are
            // sequential and are therefore disabled in the same order they were enabled (there is only one sliding
            // window of access).

            // now let's assume the first workflow is complete.
            // switch mode back to disabled for those objects by passing end-token from first workflow.
            // this makes all objects in first workflow available via REST again.
            // when disabling NFS access, all objects *older* than the token are excluded from the NFS export.
            SampleUtils.log("Work complete, changing file access mode back to disabled (workflow A):");
            request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.disabled);
            request.setToken(tokenA);
            s3.setBucketFileAccessMode(request);

            // wait until complete
            SampleUtils.log("Waiting for bucket mode to change...");
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.disabled, 30000);
            SampleUtils.log("Change complete; bucket file access is now disabled");

            // now the second workflow is complete.
            // switch mode back to disabled for these objects by passing end-token from second workflow.
            // NOTE: setting mode to disabled *without* specifying a token will disable NFS access for *all* objects in
            // the bucket.
            SampleUtils.log("Work complete, changing file access mode back to disabled (workflow B):");
            request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.disabled);
            request.setToken(tokenB);
            s3.setBucketFileAccessMode(request);

            // wait until complete
            SampleUtils.log("Waiting for bucket mode to change...");
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.disabled, 30000);
            SampleUtils.log("Change complete; bucket file access is now disabled");
        } finally {
            SampleUtils.cleanBucket(s3, bucketName);
        }

    }

    public static void main(String[] args) throws Exception {
        BucketFileAccess instance = new BucketFileAccess();
        instance.runSample();
    }

    /**
     * waits until the target access mode is completely transitioned on the specified bucket.
     *
     * @param bucketName bucket name
     * @param targetMode target access mode to wait for (readOnly, readWrite, or disabled)
     * @param timeout    after the specified number of milliseconds, this method will throw a TimeoutException
     * @throws InterruptedException if interrupted while sleeping between GET intervals
     * @throws TimeoutException     if the specified timeout is reached before transition is complete
     */
    protected void waitForTransition(String bucketName, ViPRConstants.FileAccessMode targetMode, int timeout)
            throws InterruptedException, TimeoutException {
        long start = System.currentTimeMillis(), interval = 500;
        while (true) {
            // GET the current access mode
            BucketFileAccessModeResult result = s3.getBucketFileAccessMode(bucketName);
            if (targetMode == result.getAccessMode()) return; // transition is complete

            if (targetMode.isTransitionState())
                throw new IllegalArgumentException("Invalid target mode: " + targetMode);

            if (!result.getAccessMode().isTransitionState() || !result.getAccessMode().transitionsToTarget(targetMode))
                throw new RuntimeException(String.format("Bucket %s in mode %s will never get to mode %s",
                        bucketName, result.getAccessMode(), targetMode));

            // if we've reached our timeout
            long runTime = System.currentTimeMillis() - start;
            if (runTime >= timeout)
                throw new TimeoutException(String.format("Access mode transition for %s took longer than %dms",
                        bucketName, timeout));

            // transitioning; wait and query again
            long timeLeft = timeout - runTime;
            Thread.sleep(Math.min(timeLeft, interval));
        }
    }
}
