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
package com.emc.acdp.api.test;

import com.emc.acdp.api.AcdpAdminConfig;
import com.emc.acdp.api.AcdpMgmtConfig;
import com.emc.util.TestConfig;
import org.apache.log4j.Logger;
import org.junit.Assume;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class AcdpTestUtil {
    private static final Logger l4j = Logger.getLogger( AcdpTestUtil.class );

    public static final String PROP_ACDP_ADMIN_ENDPOINT = "acdp.admin.endpoint";
    public static final String PROP_ACDP_ADMIN_USERNAME = "acdp.admin.username";
    public static final String PROP_ACDP_ADMIN_PASSWORD = "acdp.admin.password";
    public static final String PROP_ACDP_MGMT_ENDPOINT = "acdp.mgmt.endpoint";
    public static final String PROP_ACDP_MGMT_USERNAME = "acdp.mgmt.username";
    public static final String PROP_ACDP_MGMT_PASSWORD = "acdp.mgmt.password";

    public static AcdpAdminConfig loadAdminConfig() throws URISyntaxException {
        try {
            Properties p = TestConfig.getProperties();
            URI endpoint = new URI(TestConfig.getPropertyNotEmpty(p, PROP_ACDP_ADMIN_ENDPOINT));
            String username = TestConfig.getPropertyNotEmpty(p, PROP_ACDP_ADMIN_USERNAME);
            String password = TestConfig.getPropertyNotEmpty(p, PROP_ACDP_ADMIN_PASSWORD);
    
            return new AcdpAdminConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
        } catch(Exception e) {
            l4j.info("Could not load ACDP configuration: " + e);
            Assume.assumeNoException("Could not load ACDP configuration", e);
            return null;
        }
    }

    public static AcdpMgmtConfig loadMgmtConfig() throws URISyntaxException {
        try {
            Properties p = TestConfig.getProperties();
            URI endpoint = new URI(TestConfig.getPropertyNotEmpty(p, PROP_ACDP_MGMT_ENDPOINT));
            String username = TestConfig.getPropertyNotEmpty(p, PROP_ACDP_MGMT_USERNAME);
            String password = TestConfig.getPropertyNotEmpty(p, PROP_ACDP_MGMT_PASSWORD);
    
            return new AcdpMgmtConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
        } catch(Exception e) {
            l4j.info("Could not load ACDP configuration: " + e);
            Assume.assumeNoException("Could not load ACDP configuration", e);
            return null;
        }
    }


}
