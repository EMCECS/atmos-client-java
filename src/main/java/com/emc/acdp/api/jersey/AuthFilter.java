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
package com.emc.acdp.api.jersey;

import com.emc.acdp.AcdpConfig;
import com.emc.acdp.AcdpException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthFilter extends ClientFilter {
    private static final String PARAM_SESSION_TOKEN = "cdp_session";
    private static final String PARAM_USER_ID = "cdp-identity-id";
    private static final String PARAM_PASSWORD = "cdp-password";

    private AcdpConfig config;

    public AuthFilter( AcdpConfig config ) {
        this.config = config;
    }

    @Override
    public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {
        if ( !config.isSecureRequest( request.getURI().getPath(), request.getMethod() ) )
            return getNext().handle( request );

        if ( config.getSessionToken() == null ) {

            // must login
            login( request );
        } else {
            attachSessionToken( request );
        }

        ClientResponse response;
        try {
            response = getNext().handle( request );

            // if unauthorized, try one more time after logging in
        } catch ( AcdpException e ) {
            if ( e.getHttpCode() == 401 ) {
                login( request );

                response = getNext().handle( (request) );
            } else {
                throw e;
            }
        }

        return response;
    }

    private void attachSessionToken( ClientRequest request ) {

        // append session token to query in URI
        String uriStr = request.getURI().toString();

        URI uri = request.getURI();
        if ( uri.getQuery() != null && uri.getQuery().length() > 0 )
            uriStr += "&";
        else
            uriStr += "?";

        uriStr += PARAM_SESSION_TOKEN + "=" + config.getSessionToken();

        try {
            request.setURI( new URI( uriStr ) );
        } catch ( URISyntaxException e ) {
            throw new RuntimeException( e );
        }
    }

    private void login( ClientRequest request ) {

        // hold existing request configuration (we can't create a new request here)
        String holdMethod = request.getMethod();
        URI holdUri = request.getURI();
        Object holdEntity = request.getEntity();
        Object holdType = request.getHeaders().getFirst( HttpHeaders.CONTENT_TYPE );

        // login
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( PARAM_USER_ID, config.getUsername() );
        params.putSingle( PARAM_PASSWORD, config.getPassword() );

        request.setMethod( HttpMethod.POST );
        request.setURI( request.getURI().resolve( config.getLoginPath() ) );
        request.setEntity( params );
        request.getHeaders().putSingle( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE );

        ClientResponse response = getNext().handle( request );

        // get token from response
        String token = response.getEntity( String.class );
        //String token = new java.util.Scanner( response.getEntityInputStream(), "UTF-8" ).useDelimiter( "\\A" ).next();
        config.setSessionToken( token );

        // reset initial request configuration
        request.setMethod( holdMethod );
        request.setURI( holdUri );
        request.setEntity( holdEntity );
        request.getHeaders().putSingle( HttpHeaders.CONTENT_TYPE, holdType );

        attachSessionToken( request );
    }
}
