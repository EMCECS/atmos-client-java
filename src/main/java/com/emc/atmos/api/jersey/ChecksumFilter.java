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
package com.emc.atmos.api.jersey;

import com.emc.atmos.api.ChecksumValue;
import com.emc.atmos.api.ChecksumValueImpl;
import com.emc.atmos.api.ChecksummedInputStream;
import com.emc.atmos.api.RestUtil;
import com.emc.util.HttpUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.log4j.Logger;

import javax.ws.rs.HttpMethod;
import java.security.NoSuchAlgorithmException;

public class ChecksumFilter extends ClientFilter {
    private static final Logger l4j = Logger.getLogger(ChecksumFilter.class);

    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        try {
            ClientResponse response = getNext().handle(request);

            String checksumHeader = response.getHeaders().getFirst(RestUtil.XHEADER_WSCHECKSUM);
            Object rangeHeader = request.getHeaders().getFirst(HttpUtil.HEADER_RANGE);

            // only verify checksum if this is a GET complete object request and there is a ws-checksum header in the
            // response
            if (checksumHeader != null && rangeHeader == null
                    && HttpMethod.GET.equals(request.getMethod()) && response.getLength() > 0) {
                l4j.debug("wschecksum detected (" + checksumHeader + "); wrapping entity stream");
                ChecksumValue checksum = new ChecksumValueImpl(checksumHeader);
                response.setEntityInputStream(new ChecksummedInputStream(response.getEntityInputStream(), checksum));
            }

            return response;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
