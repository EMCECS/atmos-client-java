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
package com.emc.atmos.mgmt.jersey;

import com.emc.atmos.AbstractJerseyClient;
import com.emc.atmos.mgmt.TenantMgmtApi;
import com.emc.atmos.mgmt.TenantMgmtConfig;
import com.emc.atmos.mgmt.bean.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.net.URI;

public class TenantMgmtClient extends AbstractJerseyClient<TenantMgmtConfig> implements TenantMgmtApi {
    public TenantMgmtClient(TenantMgmtConfig config) {
        super(config);
    }

    @Override
    protected Client createClient(TenantMgmtConfig config) {
        return JerseyUtil.createClient(config);
    }

    @Override
    public GetTenantInfoResponse getTenantInfo() {
        URI uri = config.resolveHost("/tenant_admin/get_tenant_info", null);

        WebResource resource = client.resource(uri);
        resource.setProperty(AuthFilter.PROP_POX_REQUEST, Boolean.TRUE);

        ClientResponse response = resource.get(ClientResponse.class);

        GetTenantInfoResponse tenantResponse = new GetTenantInfoResponse();
        tenantResponse.setTenant(response.getEntity(PoxTenant.class));
        response.close();

        fillResponse(tenantResponse, response);

        return tenantResponse;
    }

    @Override
    public ListSubtenantsResponse listSubtenants() {
        return executeAndClose(buildRequest(tenantPrefix() + "/subtenants", null), ListSubtenantsResponse.class);
    }

    @Override
    public GetSubtenantResponse getSubtenant(String subtenantName) {
        ClientResponse response = buildRequest(tenantPrefix() + "/subtenants/" + subtenantName, null).get(ClientResponse.class);

        GetSubtenantResponse subtenantResponse = new GetSubtenantResponse();
        subtenantResponse.setSubtenant(response.getEntity(SubtenantDetails.class));
        response.close();

        fillResponse(subtenantResponse, response);

        return subtenantResponse;
    }

    @Override
    public ListPoliciesResponse listPolicies() {
        return executeAndClose(buildRequest("/" + config.getTenant() + "/policies", null), ListPoliciesResponse.class);
    }

    @Override
    public ListUidsResponse listUids(String subtenantName) {
        return executeAndClose(buildRequest(tenantPrefix() + "/subtenants/" + subtenantName + "/uids", null), ListUidsResponse.class);
    }

    @Override
    public SharedSecret getSharedSecret(String subtenantName, String uid) {
        return executeAndClose(buildRequest(tenantPrefix() + "/subtenants/" + subtenantName + "/uids/" + uid, null), SharedSecret.class);
    }

    protected String tenantPrefix() {
        return "/tenants/" + config.getTenant();
    }
}
