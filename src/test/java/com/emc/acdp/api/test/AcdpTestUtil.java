/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
