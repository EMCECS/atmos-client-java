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

import com.emc.atmos.mgmt.TenantMgmtApi;
import com.emc.atmos.mgmt.TenantMgmtConfig;
import com.emc.atmos.mgmt.bean.GetSubtenantResponse;
import com.emc.atmos.mgmt.bean.ListSubtenantsResponse;
import com.emc.atmos.mgmt.bean.SubtenantDetails;
import com.sun.jersey.api.client.ClientResponse;

public class TenantMgmtClient extends AbstractJerseyMgmtClient implements TenantMgmtApi {
    public TenantMgmtClient(TenantMgmtConfig config) {
        super(config);
    }

    @Override
    public ListSubtenantsResponse listSubtenants() {
        return executeAndClose("/subtenants", null, ListSubtenantsResponse.class);
    }

    @Override
    public GetSubtenantResponse getSubtenant(String subtenantName) {
        ClientResponse response = execute("/subtenants/" + subtenantName, null);

        GetSubtenantResponse subtenantResponse = new GetSubtenantResponse();
        subtenantResponse.setSubtenant(response.getEntity(SubtenantDetails.class));
        response.close();

        fillResponse(subtenantResponse, response);

        return subtenantResponse;
    }
}
