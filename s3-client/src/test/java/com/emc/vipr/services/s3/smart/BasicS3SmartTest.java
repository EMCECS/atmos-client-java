/*
 * Copyright 2014 EMC Corporation. All Rights Reserved.
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
package com.emc.vipr.services.s3.smart;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.emc.vipr.services.s3.BasicS3Test;
import com.emc.vipr.services.s3.S3ClientFactory;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

public class BasicS3SmartTest extends BasicS3Test {
    @Before
    public void setUp() throws Exception {
        vipr = S3ClientFactory.getSmartS3Client(false);
        Assume.assumeTrue("Could not configure S3 connection", vipr != null);
        try {
            vipr.createBucket(TEST_BUCKET);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 409) {
                // Ignore; bucket exists;
            } else {
                throw e;
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(vipr.getLoadBalancerStats());
    }
}
