package com.emc.atmos.api;

import com.emc.atmos.api.bean.*;
import com.emc.atmos.api.request.*;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public abstract class AbstractAtmosApi {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    protected AtmosConfig config;

    public AbstractAtmosApi( AtmosConfig config ) {
        this.config = config;
    }

    public abstract ServiceInformation getServiceInformation();

    public abstract long calculateServerClockSkew();

    public ObjectId createObject( Object content, String contentType ) {
        return createObject( new CreateObjectRequest().content( content ).contentType( contentType ) ).getObjectId();
    }

    public ObjectId createObject( ObjectIdentifier identifier, Object content, String contentType ) {
        return createObject( new CreateObjectRequest().identifier( identifier )
                                                      .content( content )
                                                      .contentType( contentType ) ).getObjectId();
    }

    public abstract CreateObjectResponse createObject( CreateObjectRequest request );

    public <T> T readObject( ObjectIdentifier identifier, Range range, Class<T> objectType ) throws IOException {
        return readObject( new ReadObjectRequest().identifier( identifier ).ranges( range ), objectType ).getObject();
    }

    public abstract <T> ReadObjectResponse<T> readObject( ReadObjectRequest request, Class<T> objectType )
            throws IOException;

    public abstract ReadObjectResponse<InputStream> readObjectStream( ObjectIdentifier identifier, Range range );

    public void updateObject( ObjectIdentifier identifier, Object content ) {
        updateObject( new UpdateObjectRequest().identifier( identifier ).content( content ) );
    }

    public void updateObject( ObjectIdentifier identifier, Object content, Range range ) {
        updateObject( new UpdateObjectRequest().identifier( identifier ).content( content ).range( range ) );
    }

    public abstract BasicResponse updateObject( UpdateObjectRequest request );

    public abstract void delete( ObjectIdentifier identifier );

    public abstract ObjectId createDirectory( ObjectPath path );

    public abstract ObjectId createDirectory( ObjectPath path, Acl acl, Metadata... metadata );

    public abstract ListDirectoryResponse listDirectory( ListDirectoryRequest request );

    public abstract void move( ObjectPath oldPath, ObjectPath newPath, boolean overwrite );

    public abstract Map<String, Boolean> getUserMetadataNames( ObjectIdentifier identifier );

    public abstract Map<String, Metadata> getUserMetadata( ObjectIdentifier identifier, String... metadataNames );

    public abstract Map<String, Metadata> getSystemMetadata( ObjectIdentifier identifier, String... metadataNames );

    public abstract ObjectMetadata getObjectMetadata( ObjectIdentifier identifier );

    public abstract void setUserMetadata( ObjectIdentifier identifier, Metadata... metadata );

    public abstract void deleteUserMetadata( ObjectIdentifier identifier, String... names );

    public abstract Set<String> listMetadata( String metadataName );

    public abstract ListObjectsResponse listObjects( ListObjectsRequest request );

    public abstract Acl getAcl( ObjectIdentifier identifier );

    public abstract void setAcl( ObjectIdentifier identifier, Acl acl );

    public abstract ObjectInfo getObjectInfo( ObjectIdentifier identifier );

    public abstract ObjectId createVersion( ObjectIdentifier identifier );

    public abstract ListVersionsResponse listVersions( ListVersionsRequest request );

    public abstract void restoreVersion( ObjectId objectId, ObjectId versionId );

    public abstract void deleteVersion( ObjectId versionId );

    public URL getShareableUrl( ObjectIdentifier identifier, Date expirationDate ) throws MalformedURLException {
        return getShareableUrl( identifier, expirationDate, null );
    }

    public URL getShareableUrl( ObjectIdentifier identifier, Date expirationDate, String disposition )
            throws MalformedURLException {
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

    public abstract CreateAccessTokenResponse createAccessToken( CreateAccessTokenRequest request )
            throws MalformedURLException;

    public GetAccessTokenResponse getAccessToken( URL url ) {
        return getAccessToken( RestUtil.lastPathElement( url.getPath() ) );
    }

    public abstract GetAccessTokenResponse getAccessToken( String accessTokenId );

    public void deleteAccessToken( URL url ) {
        deleteAccessToken( RestUtil.lastPathElement( url.getPath() ) );
    }

    public abstract void deleteAccessToken( String accessTokenId );

    public abstract ListAccessTokensResponse listAccessTokens();

    /**
     * Pre-signs a request with a specified expiration time. The pre-signed request can be executed at a later time via
     * the {@link #execute(com.emc.atmos.api.request.PreSignedRequest, Class, Object)} method. This feature is useful
     * if you intend to serialize the pre-signed request to some other system which does not have access to Atmos
     * credentials.
     *
     * @param request    the request to pre-sign (can be executed at a later time)
     * @param expiration the date at which the pre-signed request becomes invalid and will no longer be accepted
     * @return a pre-signed request that can be executed at a later time and expires at <code>expiration</code>
     * @throws MalformedURLException if the configured Atmos endpoint is invalid
     */
    public PreSignedRequest preSignRequest( Request request, Date expiration ) throws MalformedURLException {
        URI uri = config.resolvePath( request.getServiceRelativePath(), request.getQuery() );
        Map<String, List<Object>> headers = request.generateHeaders();

        String contentType = null;
        if ( request instanceof ContentRequest ) contentType = ((ContentRequest) request).getContentType();

        // add expiration header
        headers.put( RestUtil.XHEADER_EXPIRES, Arrays.asList( (Object) expiration.getTime() ) );

        RestUtil.signRequest( request.getMethod(),
                              uri.getPath(),
                              uri.getQuery(),
                              headers,
                              config.getTokenId(),
                              config.getSecretKey(),
                              config.getServerClockSkew() );

        return new PreSignedRequest( uri.toURL(), request.getMethod(), contentType, headers );
    }

    public abstract <T> GenericResponse<T> execute( PreSignedRequest request, Class<T> resultType, Object content )
            throws URISyntaxException;
}
