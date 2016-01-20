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
package com.emc.atmos.api.request;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents a request that has been pre-signed and is valid until <code>expiration</code>.
 * <p/>
 * Immutable.
 */
public class PreSignedRequest implements Serializable {
    private static final long serialVersionUID = -5841074558608401979L;

    private URL url;
    private String method;
    private String contentType;
    private Map<String, List<Object>> headers;
    private Date expiration;

    public PreSignedRequest( URL url,
                             String method,
                             String contentType,
                             Map<String, List<Object>> headers,
                             Date expiration ) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.expiration = expiration;
    }

    /**
     * Gets the URL of the request. This includes the Atmos endpoint.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Gets the HTTP method of the request.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the content-type to use for the request.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the headers to use in the request.
     */
    public Map<String, List<Object>> getHeaders() {
        return headers;
    }

    /**
     * Gets the date this request expires (is no longer valid).
     */
    public Date getExpiration() {
        return expiration;
    }
}
