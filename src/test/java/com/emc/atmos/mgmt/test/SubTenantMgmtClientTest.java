/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2013-2018, Dell EMC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.emc.atmos.mgmt.test;

import com.emc.atmos.mgmt.SubTenantMgmtApi;
import com.emc.atmos.mgmt.SubTenantMgmtConfig;
import com.emc.atmos.mgmt.bean.*;
import com.emc.atmos.mgmt.jersey.SubTenantMgmtClient;
import com.emc.util.TestConfig;
import com.emc.util.TestConstants;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SubTenantMgmtClientTest {
    private static final Logger l4j = LoggerFactory.getLogger(SubTenantMgmtClientTest.class);

    protected SubTenantMgmtApi client;

    @Before
    public void setup() {
        SubTenantMgmtConfig config = createSubTenantMgmtConfig();
        Assume.assumeNotNull(config);
        client = new SubTenantMgmtClient(config);
    }

    private SubTenantMgmtConfig createSubTenantMgmtConfig() {
        try {
            Properties props = TestConfig.getProperties();

            String endpoints = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_ENDPOINTS);
            String tenant = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_TENANT);
            String subTenant = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_SUB_TENANT);
            String user = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_SUB_TENANTADMIN_USER);
            String password = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_SUB_TENANTADMIN_PASS);
            String proxyUrl = props.getProperty(TestConstants.PROP_PROXY);

            List<URI> endpointUris = new ArrayList<URI>();
            for (String endpoint : endpoints.split(",")) {
                endpointUris.add(new URI(endpoint));
            }

            SubTenantMgmtConfig config = new SubTenantMgmtConfig(tenant, subTenant, user, password, endpointUris.toArray(new URI[0]));
            config.setDisableSslValidation(true);

            if (proxyUrl != null) {
                URI proxyUri = new URI(proxyUrl);
                System.setProperty("http.proxyHost", proxyUri.getHost());
                System.setProperty("https.proxyHost", proxyUri.getHost());
                if (proxyUri.getPort() > 0) {
                    System.setProperty("http.proxyPort", "" + proxyUri.getPort());
                    System.setProperty("https.proxyPort", "" + proxyUri.getPort());
                }
            }

            return config;
        } catch (IOException e) {
            l4j.info("Could not load properties file: " + e);
            return null;
        } catch (URISyntaxException e) {
            l4j.info("Invalid endpoint or proxy URI: " + e);
            return null;
        }
    }

    @Test
    public void testListUids() {
        ListUidsResponse response = client.listUids();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getUids());
        Assert.assertTrue(response.getUids().size() > 0);
        Assert.assertNotNull((response.getUids().get(0)));
    }

    @Test
    public void testGetSharedSecret() {
        ListUidsResponse listResult = client.listUids();
        SharedSecret response = client.getSharedSecret(listResult.getUids().get(0));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getKeyCreateTime());
        Assert.assertNotNull(response.getSharedSecret());
    }
}
