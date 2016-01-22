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

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

public class ProxyAuthFilter extends ClientFilter {
    private String proxyUser, proxyPassword;

    public ProxyAuthFilter( String proxyUser, String proxyPassword ) {
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }

    @Override
    public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {
        handleProxyAuth( request );

        return getNext().handle( request );
    }

    protected void handleProxyAuth( ClientRequest request ) {
        if ( proxyUser != null && proxyUser.length() > 0 ) {
            String userPass = proxyUser + ":" + ((proxyPassword == null) ? "null" : proxyPassword);

            String userPass64;
            try {
                userPass64 = DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
            } catch ( UnsupportedEncodingException e ) {
                userPass64 = DatatypeConverter.printBase64Binary(userPass.getBytes());
            }

            // Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459815
            userPass64 = userPass64.replaceAll("\n", "");

            request.getHeaders().putSingle( "Proxy-Authorization", "Basic " + userPass64 );
        }
    }
}
