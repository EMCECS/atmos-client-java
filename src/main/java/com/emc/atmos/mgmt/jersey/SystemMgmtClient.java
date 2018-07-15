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
package com.emc.atmos.mgmt.jersey;

import com.emc.atmos.AbstractClient;
import com.emc.atmos.mgmt.SystemMgmtApi;
import com.emc.atmos.mgmt.SystemMgmtConfig;
import com.emc.atmos.mgmt.bean.ListRmgsResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.net.URI;

public class SystemMgmtClient extends AbstractClient implements SystemMgmtApi {
    private SystemMgmtConfig config;
    private Client client;

    public SystemMgmtClient(SystemMgmtConfig config) {
        this.config = config;
        this.client = JerseyUtil.createClient(config);
    }

    @Override
    public ListRmgsResponse listRmgs() {
        URI uri = config.resolvePath("/rmgs", null);
        WebResource.Builder builder = client.resource(uri).getRequestBuilder();

        ClientResponse response = builder.get(ClientResponse.class);

        ListRmgsResponse ret = response.getEntity(ListRmgsResponse.class);

        response.close();

        return fillResponse(ret, response);
    }
}
