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

import com.emc.atmos.mgmt.SubTenantMgmtApi;
import com.emc.atmos.mgmt.SubTenantMgmtConfig;
import com.emc.atmos.mgmt.TenantMgmtApi;
import com.emc.atmos.mgmt.bean.ListUidsResponse;
import com.emc.atmos.mgmt.bean.SharedSecret;

/**
 * This class is basically a delegator to a {@link TenantMgmtClient} instance, exposing only the subtenant operations
 * and always using the subtenant specified by the {@link SubTenantMgmtConfig}
 */
public class SubTenantMgmtClient implements SubTenantMgmtApi {
    private SubTenantMgmtConfig config;
    private TenantMgmtApi tenantClient;

    public SubTenantMgmtClient(SubTenantMgmtConfig config) {
        this.config = config;
        tenantClient = new TenantMgmtClient(config);
    }

    @Override
    public ListUidsResponse listUids() {
        return tenantClient.listUids(config.getSubTenant());
    }

    @Override
    public SharedSecret getSharedSecret(String uid) {
        return tenantClient.getSharedSecret(config.getSubTenant(), uid);
    }
}
