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

import com.emc.atmos.AtmosException;
import com.emc.atmos.mgmt.AbstractMgmtConfig;
import com.emc.util.HttpUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Map;

public class AuthFilter extends ClientFilter {
    public static final String PROP_POX_REQUEST = AuthFilter.class.getName() + ".poxRequest";
    public static final String SESSION_COOKIE = "_gui_session_id";

    private AbstractMgmtConfig config;

    public AuthFilter(AbstractMgmtConfig config) {
        this.config = config;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        if (request.getPropertyAsFeature(PROP_POX_REQUEST, false)) return handlePox(request);
        else return handleRest(request);
    }

    private ClientResponse handleRest(ClientRequest request) {
        request.getHeaders().putAll(config.getRestAuthenticationHeaders());
        return getNext().handle(request);
    }

    private ClientResponse handlePox(ClientRequest request) throws ClientHandlerException {
        if (config.getSessionCookie() == null) {

            // first login
            login(request);
        }

        attachSessionCookie(request);

        ClientResponse response;
        try {
            response = getNext().handle(request);

            // if unauthorized, try one more time after logging in
        } catch (AtmosException e) {
            // apparently Atmos returns a 302 redirect to the login page instead of a 401 when the cookie is expired
            if (e.getHttpCode() == 302) {
                invalidateCookie();
                login(request);

                response = getNext().handle((request));
            } else {
                throw e;
            }
        }

        return response;
    }

    private void attachSessionCookie(ClientRequest request) {
        request.getHeaders().putSingle(HttpUtil.HEADER_COOKIE, new Cookie(SESSION_COOKIE, config.getSessionCookie()));
    }

    private synchronized void invalidateCookie() {
        config.setSessionCookie(null);
    }

    private synchronized void login(ClientRequest request) {
        if (config.getSessionCookie() != null) return;

        // hold existing request configuration (we can't create a new request here)
        String holdMethod = request.getMethod();
        URI holdUri = request.getURI();
        Object holdEntity = request.getEntity();
        Object holdType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        Object holdAccept = request.getHeaders().getFirst(HttpHeaders.ACCEPT);

        // login

        // get login POST params
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : config.getPoxLoginParams().entrySet()) {
            params.putSingle(entry.getKey(), entry.getValue());
        }

        // build login request
        request.setMethod(HttpMethod.POST);
        request.setURI(request.getURI().resolve(config.getPoxLoginPath()));
        request.setEntity(params);
        request.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        request.getHeaders().putSingle(HttpHeaders.ACCEPT, "application/xml");

        // make login call
        ClientResponse response = getNext().handle(request);

        // get cookie from response
        String sessionCookie = null;
        for (NewCookie cookie : response.getCookies()) {
            if (SESSION_COOKIE.equals(cookie.getName())) {
                sessionCookie = cookie.getValue();
                break;
            }
        }

        if (sessionCookie == null) // there was a problem
            throw new RuntimeException("Auth failure");
        else // save session cookie
            config.setSessionCookie(sessionCookie);

        // reset initial request configuration
        request.setMethod(holdMethod);
        request.setURI(holdUri);
        request.setEntity(holdEntity);
        request.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, holdType);
        request.getHeaders().putSingle(HttpHeaders.ACCEPT, holdAccept);
    }
}
