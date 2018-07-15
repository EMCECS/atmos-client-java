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
package com.emc.atmos;

import com.emc.util.BasicResponse;
import com.emc.util.HttpUtil;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public abstract class AbstractClient {

    /**
     * Populates a response object with data from the ClientResponse.
     */
    protected <T extends BasicResponse> T fillResponse(T response, ClientResponse clientResponse) {
        Response.StatusType statusType = clientResponse.getStatusInfo();
        MediaType type = clientResponse.getType();
        URI location = clientResponse.getLocation();
        response.setHttpStatus(clientResponse.getStatus());
        response.setHttpMessage(statusType == null ? null : statusType.getReasonPhrase());
        response.setHeaders(clientResponse.getHeaders());
        response.setContentType(type == null ? null : type.toString());
        response.setContentLength(clientResponse.getLength());
        response.setLocation(location == null ? null : location.toString());
        if (clientResponse.getHeaders() != null) {
            // workaround for Github Issue #3
            response.setDate(HttpUtil.safeHeaderParse(clientResponse.getHeaders().getFirst(HttpUtil.HEADER_DATE)));
            response.setLastModified(HttpUtil.safeHeaderParse(clientResponse.getHeaders().getFirst(HttpUtil.HEADER_LAST_MODIFIED)));
            response.setETag(clientResponse.getHeaders().getFirst(HttpUtil.HEADER_ETAG));
        }
        return response;
    }
}
