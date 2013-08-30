// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.api.request;

import com.emc.atmos.api.RestUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common elements of a request for a list of items. Atmos uses the limit and token headers to page results. The limit
 * is specified in the request to limit the number of results returned. The token is specified in the response to
 * signify that there are more results to get and in the request to get the next page of results.
 *
 * @param <T> Represents the implementation type. Allows a consistent builder interface throughout the request
 *            hierarchy. Parameterize concrete subclasses with their own type and implement {@link #me()} to return
 *            "this". In abstract subclasses, return me() in builder methods.
 */
public abstract class ListRequest<T extends ListRequest<T>> extends Request {
    protected int limit;
    protected String token;

    /**
     * Returns "this" in concrete implementation classes. Used in builder methods to be consistent throughout the
     * hierarchy. For example, you can call <code>new CreateObjectRequest().identifier(path).content(content)</code>.
     *
     * @return this
     */
    protected abstract T me();

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new TreeMap<String, List<Object>>();

        RestUtil.addValue( headers, RestUtil.XHEADER_UTF8, "true" );

        if ( limit > 0 ) RestUtil.addValue( headers, RestUtil.XHEADER_LIMIT, limit );

        if ( token != null ) RestUtil.addValue( headers, RestUtil.XHEADER_TOKEN, token );

        return headers;
    }

    /**
     * Builder method for {@link #setLimit(int)}
     */
    public T limit( int limit ) {
        setLimit( limit );
        return me();
    }

    /**
     * Builder method for {@link #setToken(String)}
     */
    public T token( String token ) {
        setToken( token );
        return me();
    }

    /**
     * Gets the limit (page size) for this request.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the cursor token for this request, returned in the response to indicate there is another page of results
     * and in the request to get the next page of results.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the limit (page size) for this request.
     */
    public void setLimit( int limit ) {
        this.limit = limit;
    }

    /**
     * Sets the cursor token for this request, returned in the response to indicate there is another page of results
     * and in the request to get the next page of results. API implementations should set this value automatically if
     * it is returned in the response, so you can check this value for null to see if you have received the entire
     * list.
     */
    public void setToken( String token ) {
        this.token = token;
    }
}
