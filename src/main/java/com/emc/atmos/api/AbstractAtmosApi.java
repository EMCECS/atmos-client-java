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
package com.emc.atmos.api;

import com.emc.atmos.AbstractClient;
import com.emc.atmos.api.bean.GetAccessTokenResponse;
import com.emc.atmos.api.request.*;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class AbstractAtmosApi extends AbstractClient implements AtmosApi {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    protected AtmosConfig config;

    public AbstractAtmosApi( AtmosConfig config ) {
        this.config = config;
    }

    @Override
    public ObjectId createObject( Object content, String contentType ) {
        return createObject( new CreateObjectRequest().content( content ).contentType( contentType ) ).getObjectId();
    }

    @Override
    public ObjectId createObject( ObjectIdentifier identifier, Object content, String contentType ) {
        return createObject( new CreateObjectRequest().identifier( identifier )
                                                      .content( content )
                                                      .contentType( contentType ) ).getObjectId();
    }

    @Override
    public <T> T readObject( ObjectIdentifier identifier, Class<T> objectType ) throws IOException {
        return readObject( new ReadObjectRequest().identifier( identifier ), objectType ).getObject();
    }

    @Override
    public <T> T readObject( ObjectIdentifier identifier, Range range, Class<T> objectType ) throws IOException {
        return readObject( new ReadObjectRequest().identifier( identifier ).ranges( range ), objectType ).getObject();
    }

    @Override
    public void updateObject( ObjectIdentifier identifier, Object content ) {
        updateObject( new UpdateObjectRequest().identifier( identifier ).content( content ) );
    }

    @Override
    public void updateObject( ObjectIdentifier identifier, Object content, Range range ) {
        updateObject( new UpdateObjectRequest().identifier( identifier ).content( content ).range( range ) );
    }

    @Override
    public URL getShareableUrl( ObjectIdentifier identifier, Date expirationDate ) throws MalformedURLException {
        return getShareableUrl( identifier, expirationDate, null );
    }

    @Override
    public URL getShareableUrl( ObjectIdentifier identifier, Date expirationDate, String disposition )
            throws MalformedURLException {
        if ( identifier instanceof ObjectKey )
            throw new IllegalArgumentException( "You cannot create shareable URLs using a key; try using the object ID" );

        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), null );
        String path = uri.getPath().toLowerCase();
        long expiresTime = expirationDate.getTime() / 1000;

        String hashString = "GET\n"
                            + path + '\n'
                            + config.getTokenId() + '\n'
                            + expiresTime;
        if ( disposition != null )
            hashString += '\n' + disposition;

        String hash = RestUtil.sign( hashString, config.getSecretKey() );

        String query = "uid=" + HttpUtil.encodeUtf8( config.getTokenId() ) + "&expires=" + expiresTime
                       + "&signature=" + HttpUtil.encodeUtf8( hash );
        if ( disposition != null )
            query += "&disposition=" + HttpUtil.encodeUtf8( disposition );

        // we must manually append the query string to ensure the equals sign in the signature gets encoded properly
        return new URL( uri + "?" + query );
    }

    @Override
    public GetAccessTokenResponse getAccessToken( URL url ) {
        return getAccessToken( RestUtil.lastPathElement( url.getPath() ) );
    }

    @Override
    public void deleteAccessToken( URL url ) {
        deleteAccessToken( RestUtil.lastPathElement( url.getPath() ) );
    }

    @Override
    public PreSignedRequest preSignRequest( Request request, Date expiration ) throws MalformedURLException {
        URI uri = config.resolvePath( request.getServiceRelativePath(), request.getQuery() );
        Map<String, List<Object>> headers = request.generateHeaders( config.isEncodeUtf8() );

        String contentType = null;
        if ( request instanceof ContentRequest ) contentType = ((ContentRequest) request).getContentType();
        // workaround for clients that set a default content-type for POSTs
        if ( "POST".equals( request.getMethod() ) ) contentType = RestUtil.TYPE_DEFAULT;

        // add expiration header
        headers.put( RestUtil.XHEADER_EXPIRES, Arrays.asList( (Object) expiration.getTime() ) );

        RestUtil.signRequest( request.getMethod(),
                              uri.getPath(),
                              uri.getQuery(),
                              headers,
                              config.getTokenId(),
                              config.getSecretKey(),
                              config.getServerClockSkew() );

        return new PreSignedRequest( uri.toURL(), request.getMethod(), contentType, headers, expiration );
    }
}
