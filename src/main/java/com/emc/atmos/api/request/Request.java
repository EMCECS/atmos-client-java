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

import java.util.List;
import java.util.Map;

/**
 * Represents an Atmos REST request.
 */
public abstract class Request {
    /**
     * Returns the service-relative path of this request (i.e. "objects/{identifier}" for read-object).
     */
    public abstract String getServiceRelativePath();

    /**
     * Override if a request requires a query string (i.e. "metadata/system" for getting system metadata)
     *
     * @return the URL query string for this request
     */
    public String getQuery() {
        return null;
    }

    /**
     * Returns the HTTP method this request will use.
     */
    public abstract String getMethod();

    /**
     * Returns the HTTP headers to send in this request, to be generated from other request properties immediately
     * before sending.
     */
    public abstract Map<String, List<Object>> generateHeaders();

    /**
     * Override and return true if this request supports the Expect: 100-continue header. Typically only object write
     * requests support this option.
     */
    public boolean supports100Continue() {
        return false;
    }
}
