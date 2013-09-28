/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.vipr.services.s3;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;
import com.emc.vipr.services.s3.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/*
 * Test the ViPR-specific file access feature for S3
 */
public class ViPRS3FileAccessTest {
    private static Log log = LogFactory.getLog(ViPRS3FileAccessTest.class);

    private ViPRS3Client s3;

    @Before
    public void setUp() throws Exception {
        s3 = S3ClientFactory.getS3Client();
    }

    protected void createBucket(String bucketName) {
        try {
            s3.createBucket(bucketName);
        } catch(AmazonS3Exception e) {
            if(e.getStatusCode() == 409) {
                // Ignore; bucket exists;
            } else {
                throw e;
            }
        }
    }

    // TODO: uncomment when CQ 608494 is fixed
    //@Test
    public void testBasicReadOnly() throws Exception {
        String bucketName = "test.vipr-basic-read-only";
        String key = "basic-read-only.txt";
        String content = "Hello read-only!";

        try {
            createBucket(bucketName);
            StringInputStream ss = new StringInputStream(content);
            ObjectMetadata om = new ObjectMetadata();
            om.setContentLength(ss.available());
            s3.putObject(bucketName, key, ss, om);

            SetBucketFileAccessModeRequest request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.readOnly);
            request.setDuration(300); // seconds
            request.setHostList(Arrays.asList("10.6.143.99", "10.6.143.100")); // client IP(s)
            request.setUid("501"); // client's OS UID

            // change mode to read-only
            BucketFileAccessModeResult result = s3.setBucketFileAccessMode(request);

            assertNotNull("set access-mode result is null", result);
            assertTrue("wrong access mode", request.getAccessMode() == result.getAccessMode()
                    || result.getAccessMode().transitionsToTarget(request.getAccessMode()));
            assertTrue("wrong duration", request.getDuration() - result.getDuration() < 5);
            assertArrayEquals("wrong host list", request.getHostList().toArray(), result.getHostList().toArray());
            assertEquals("wrong user", request.getUid(), result.getUid());

            // wait until complete (change is asynchronous)
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.readOnly, 60000);

            // verify mode change
            BucketFileAccessModeResult result2 = s3.getBucketFileAccessMode(bucketName);

            assertEquals("wrong access mode", request.getAccessMode(), result2.getAccessMode());
            assertTrue("wrong duration", request.getDuration() > result2.getDuration());
            assertArrayEquals("wrong host list", request.getHostList().toArray(), result2.getHostList().toArray());
            assertEquals("wrong user", request.getUid(), result2.getUid());

            // get NFS details
            GetFileAccessRequest fileAccessRequest = new GetFileAccessRequest();
            fileAccessRequest.setBucketName(bucketName);
            GetFileAccessResult fileAccessResult = s3.getFileAccess(fileAccessRequest);

            // verify NFS details
            assertNotNull("fileaccess result is null", fileAccessResult);
            assertNotNull("mounts is null", fileAccessResult.getMountPoints());
            assertTrue("no mounts", fileAccessResult.getMountPoints().size() > 0);
            assertNotNull("objects is null", fileAccessResult.getObjects());
            assertEquals("wrong number of objects", 1, fileAccessResult.getObjects().size());

            // change mode back to disabled
            request = new SetBucketFileAccessModeRequest();
            request.setBucketName(bucketName);
            request.setAccessMode(ViPRConstants.FileAccessMode.disabled);
            s3.setBucketFileAccessMode(request);

            // wait until complete
            waitForTransition(bucketName, ViPRConstants.FileAccessMode.disabled, 30000);

            // verify mode change
            fileAccessRequest = new GetFileAccessRequest();
            fileAccessRequest.setBucketName(bucketName);
            try {
                s3.getFileAccess(fileAccessRequest);
                fail("GET fileaccess should fail when access mode is disabled");
            } catch (AmazonS3Exception e) {
                if (!"FileAccessNotAllowed".equals(e.getErrorCode())) throw e;
            }

        } finally {
            cleanBucket(bucketName);
        }
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
    protected void waitForTransition(String bucketName, ViPRConstants.FileAccessMode targetMode, int timeout) throws InterruptedException, TimeoutException {
        long start = System.currentTimeMillis(), interval = 500;
        while (true) {
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

    protected void cleanBucket(String bucketName) {
        try {
            for (S3ObjectSummary summary : s3.listObjects(bucketName).getObjectSummaries()) {
                s3.deleteObject(bucketName, summary.getKey());
            }
            s3.deleteBucket(bucketName);
        } catch (Exception e) {
            // don't cause tests to fail
            log.warn(String.format("Could not clean up bucket %s", bucketName), e);
        }
    }
}
