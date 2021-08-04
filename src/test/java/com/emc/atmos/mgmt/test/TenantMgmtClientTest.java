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

import com.emc.atmos.mgmt.TenantMgmtApi;
import com.emc.atmos.mgmt.TenantMgmtConfig;
import com.emc.atmos.mgmt.bean.*;
import com.emc.atmos.mgmt.jersey.TenantMgmtClient;
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

public class TenantMgmtClientTest {
    private static final Logger l4j = LoggerFactory.getLogger(TenantMgmtClientTest.class);

    private TenantMgmtApi client;
    private String subtenant;

    @Before
    public void setup() {
        TenantMgmtConfig config = createTenantMgmtConfig();
        Assume.assumeNotNull(config);
        client = new TenantMgmtClient(config);
    }

    private TenantMgmtConfig createTenantMgmtConfig() {
        try {
            Properties props = TestConfig.getProperties();

            String endpoints = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_ENDPOINTS);
            String tenant = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_TENANT);
            subtenant = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_SUB_TENANT);
            String user = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_TENANTADMIN_USER);
            String password = TestConfig.getPropertyNotEmpty(props, TestConstants.PROP_MGMT_TENANTADMIN_PASS);
            String proxyUrl = props.getProperty(TestConstants.PROP_PROXY);

            List<URI> endpointUris = new ArrayList<URI>();
            for (String endpoint : endpoints.split(",")) {
                endpointUris.add(new URI(endpoint));
            }

            TenantMgmtConfig config = new TenantMgmtConfig(tenant, user, password, endpointUris.toArray(new URI[0]));
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
    public void testGetTenantInfo() {
        GetTenantInfoResponse result = client.getTenantInfo();
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getTenant());
        checkTenant(result.getTenant());
    }

    private void checkTenant(PoxTenant tenant) {
        Assert.assertNotNull(tenant);
        Assert.assertNotNull(tenant.getName());
        Assert.assertTrue(tenant.getName().trim().length() > 0);
        Assert.assertNotNull(tenant.getId());
        Assert.assertTrue(tenant.getId().trim().length() > 0);

        Assert.assertNotNull(tenant.getTenantAdminList());
        Assert.assertTrue(tenant.getTenantAdminList().size() > 0);
        checkAdminUser(tenant.getTenantAdminList().get(0));

        Assert.assertNotNull(tenant.getSubtenantList());
        Assert.assertTrue(tenant.getSubtenantList().size() > 0);
        checkSubtenant(tenant.getSubtenantList().get(0));
    }

    @Test
    public void testListSubtenants() {
        ListSubtenantsResponse response = client.listSubtenants();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getSubtenants());
        Assert.assertTrue(response.getSubtenants().size() > 0);
        for (Subtenant subtenant : response.getSubtenants()) {
            if (this.subtenant.equals(subtenant.getName()))
                checkSubtenant(subtenant);
        }
    }

    private void checkSubtenant(AbstractSubtenant subtenant) {
        Assert.assertNotNull(subtenant);
        Assert.assertNotNull(subtenant.getName());
        Assert.assertTrue(subtenant.getName().trim().length() > 0);
        Assert.assertNotNull(subtenant.getId());
        Assert.assertTrue(subtenant.getId().trim().length() > 0);
        Assert.assertNotNull(subtenant.getAuthenticationSource());
        Assert.assertNotNull(subtenant.getStatus());
        if (subtenant instanceof Subtenant) {
            Subtenant subt = (Subtenant) subtenant;
            Assert.assertNotNull(subt.getSubtenantAdminNames());
            if (subt.getSubtenantAdminNames().size() > 0)
                Assert.assertNotNull(subt.getSubtenantAdminNames().get(0));
        } else if (subtenant instanceof SubtenantDetails) {
            SubtenantDetails subt = (SubtenantDetails) subtenant;
            Assert.assertNotNull(subt.getSubtenantAdminList());
            if (subt.getSubtenantAdminList().size() > 0)
                checkAdminUser(subt.getSubtenantAdminList().get(0));
        }
    }

    private void checkAdminUser(AdminUser adminUser) {
        Assert.assertNotNull(adminUser);
        Assert.assertNotNull(adminUser.getName());
        Assert.assertNotNull(adminUser.getAuthenticationSource());
    }

    private void checkObjectUser(ObjectUser objectUser) {
        Assert.assertNotNull(objectUser);
        Assert.assertNotNull(objectUser.getUid());
        Assert.assertTrue(objectUser.getUid().trim().length() > 0);
        Assert.assertNotNull(objectUser.getSharedSecret());
        Assert.assertTrue(objectUser.getSharedSecret().trim().length() > 0);
        Assert.assertNotNull(objectUser.getStatus());
    }

    @Test
    public void testGetSubtenantDetails() {
        ListSubtenantsResponse listResult = client.listSubtenants();
        GetSubtenantResponse response = client.getSubtenant(listResult.getSubtenants().get(0).getName());
        Assert.assertNotNull(response);
        checkSubtenant(response.getSubtenant());
        Assert.assertNotNull(response.getSubtenant().getDefaultPolicySpec());
        Assert.assertTrue(response.getSubtenant().getDefaultPolicySpec().trim().length() > 0);
        Assert.assertNotNull(response.getSubtenant().getObjectUsers());
        Assert.assertTrue(response.getSubtenant().getObjectUsers().size() > 0);
        checkObjectUser(response.getSubtenant().getObjectUsers().get(0));
    }

    @Test
    public void testListPolicies() {
        ListPoliciesResponse response = client.listPolicies();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPolicies());
        Assert.assertTrue(response.getPolicies().size() > 0);
        checkPolicy(response.getPolicies().get(0));
    }

    private void checkPolicy(PoxPolicy policy) {
        Assert.assertNotNull(policy);
        Assert.assertNotNull(policy.getName());
        Assert.assertTrue(policy.getName().trim().length() > 0);
        if (policy.getReplicaList().size() > 0)
            checkReplica(policy.getReplicaList().get(0));
    }

    private void checkReplica(PoxReplica replica) {
        Assert.assertNotNull(replica);
        Assert.assertNotNull(replica.getType());
        Assert.assertTrue(replica.getType().trim().length() > 0);
        Assert.assertNotNull(replica.getLocations());
        Assert.assertTrue(replica.getLocations().trim().length() > 0);
        Assert.assertNotNull(replica.getLocation());
        Assert.assertTrue(replica.getLocation().trim().length() > 0);
    }

    @Test
    public void testListUids() {
        ListUidsResponse response = client.listUids(subtenant);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getUids());
        Assert.assertTrue(response.getUids().size() > 0);
        Assert.assertNotNull((response.getUids().get(0)));
    }

    @Test
    public void testGetSharedSecret() {
        ListUidsResponse listResult = client.listUids(subtenant);
        SharedSecret response = client.getSharedSecret(subtenant, listResult.getUids().get(0));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getKeyCreateTime());
        Assert.assertNotNull(response.getSharedSecret());
    }
}
