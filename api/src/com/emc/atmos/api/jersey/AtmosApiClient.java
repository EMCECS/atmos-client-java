package com.emc.atmos.api.jersey;

import com.emc.atmos.AtmosException;
import com.emc.atmos.api.*;
import com.emc.atmos.api.bean.*;
import com.emc.atmos.api.multipart.MultipartEntity;
import com.emc.atmos.api.request.*;
import com.emc.util.HttpUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class AtmosApiClient extends AbstractAtmosApi {
    private static final Logger l4j = Logger.getLogger( AtmosApiClient.class );

    private Client client;

    public AtmosApiClient( AtmosConfig config ) {
        this( config, null );
    }

    public AtmosApiClient( AtmosConfig config, Client client ) {
        super( config );
        this.client = JerseyUtil.createClient( config, client );
    }

    @Override
    public ServiceInformation getServiceInformation() {
        ClientResponse response = client.resource( config.resolvePath( "service", null ) ).get( ClientResponse.class );
        ServiceInformation serviceInformation = response.getEntity( ServiceInformation.class );

        String featureString = response.getHeaders().getFirst( RestUtil.XHEADER_FEATURES );
        if ( featureString != null ) {
            for ( String feature : featureString.split( "," ) )
                serviceInformation.addFeatureFromHeaderName( feature.trim() );
        }

        // legacy
        String utf8String = response.getHeaders().getFirst( RestUtil.XHEADER_SUPPORT_UTF8 );
        if ( utf8String != null && Boolean.valueOf( utf8String ) )
            serviceInformation.addFeature( ServiceInformation.Feature.Utf8 );

        return serviceInformation;
    }

    @Override
    public long calculateServerClockSkew() {
        ClientResponse response = client.resource( config.resolvePath( "", null ) ).get( ClientResponse.class );

        if ( response.getResponseDate() == null )
            throw new AtmosException( "Response date is null", response.getStatus() );

        config.setServerClockSkew( System.currentTimeMillis() - response.getResponseDate().getTime() );
        return config.getServerClockSkew();
    }

    @Override
    public CreateObjectResponse createObject( CreateObjectRequest request ) {
        ClientResponse response = build( request ).post( ClientResponse.class, getContent( request ) );

        return fillResponse( new CreateObjectResponse(), response );
    }

    @Override
    public <T> ReadObjectResponse<T> readObject( ReadObjectRequest request, Class<T> objectType ) throws IOException {
        if ( request.getRanges() != null && request.getRanges().size() > 1
             && !MultipartEntity.class.isAssignableFrom( objectType ) )
            l4j.warn( "multiple ranges imply a multi-part response. you should ask for MultipartEntity instead of " +
                      objectType.getSimpleName() );

        ClientResponse response = build( request ).get( ClientResponse.class );

        return fillResponse( new ReadObjectResponse<T>( response.getEntity( objectType ) ), response );
    }

    @Override
    public ReadObjectResponse<InputStream> readObjectStream( ObjectIdentifier identifier, Range range ) {
        ClientResponse response = build( new ReadObjectRequest().identifier( identifier ).ranges( range ) )
                .get( ClientResponse.class );
        return fillResponse( new ReadObjectResponse<InputStream>( response.getEntityInputStream() ), response );
    }

    @Override
    public BasicResponse updateObject( UpdateObjectRequest request ) {
        ClientResponse response = build( request ).put( ClientResponse.class, getContent( request ) );
        return fillResponse( new BasicResponse(), response );
    }

    @Override
    public void delete( ObjectIdentifier identifier ) {
        client.resource( config.resolvePath( identifier.getRelativeResourcePath(), null ) ).delete();
    }

    @Override
    public ObjectId createDirectory( ObjectPath path ) {
        CreateObjectRequest request = new CreateObjectRequest().identifier( path );

        ClientResponse response = build( request ).post( ClientResponse.class );
        return RestUtil.parseObjectId( response.getLocation().getPath() );
    }

    @Override
    public ObjectId createDirectory( ObjectPath path, Acl acl, Metadata... metadata ) {
        CreateObjectRequest request = new CreateObjectRequest().identifier( path ).acl( acl );
        request.userMetadata( metadata );

        ClientResponse response = build( request ).post( ClientResponse.class );
        return RestUtil.parseObjectId( response.getLocation().getPath() );
    }

    @Override
    public ListDirectoryResponse listDirectory( ListDirectoryRequest request ) {
        if ( !request.getPath().isDirectory() ) throw new AtmosException( "Path must be a directory" );

        ClientResponse response = build( request ).get( ClientResponse.class );

        request.setToken( response.getHeaders().getFirst( RestUtil.XHEADER_TOKEN ) );
        if ( request.getToken() != null )
            l4j.warn( "Results truncated. Call listDirectory again for next page of results." );

        return fillResponse( response.getEntity( ListDirectoryResponse.class ), response );
    }

    @Override
    public void move( ObjectPath oldPath, ObjectPath newPath, boolean overwrite ) {
        WebResource resource = client.resource( config.resolvePath( oldPath.getRelativeResourcePath(), "rename" ) );
        WebResource.Builder builder = resource.getRequestBuilder();
        builder.header( RestUtil.XHEADER_UTF8, "true" ).header( RestUtil.XHEADER_PATH, newPath.getPath() );
        if ( overwrite ) builder.header( RestUtil.XHEADER_FORCE, "true" );
        builder.post();
    }

    @Override
    public Map<String, Boolean> getUserMetadataNames( ObjectIdentifier identifier ) {
        WebResource resource = client.resource( config.resolvePath( identifier.getRelativeResourcePath(),
                                                                    "metadata/tags" ) );
        WebResource.Builder builder = resource.getRequestBuilder();
        ClientResponse response = builder.header( RestUtil.XHEADER_UTF8, "true" ).get( ClientResponse.class );

        Map<String, Boolean> metaNames = new TreeMap<String, Boolean>();

        String nameString = response.getHeaders().getFirst( RestUtil.XHEADER_TAGS );
        if ( nameString != null ) {
            for ( String name : nameString.split( "," ) )
                metaNames.put( HttpUtil.decodeUtf8( name.trim() ), false );
        }

        nameString = response.getHeaders().getFirst( RestUtil.XHEADER_LISTABLE_TAGS );
        if ( nameString != null ) {
            for ( String name : nameString.split( "," ) )
                metaNames.put( HttpUtil.decodeUtf8( name.trim() ), true );
        }

        return metaNames;
    }

    @Override
    public Map<String, Metadata> getUserMetadata( ObjectIdentifier identifier, String... metadataNames ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "metadata/user" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        if ( metadataNames != null ) {
            for ( String name : metadataNames ) {
                builder.header( RestUtil.XHEADER_TAGS, name );
            }
        }

        ClientResponse response = builder.header( RestUtil.XHEADER_UTF8, "true" ).get( ClientResponse.class );

        Map<String, Metadata> metaMap = new TreeMap<String, Metadata>();
        metaMap.putAll( RestUtil.parseMetadataHeader( response.getHeaders().getFirst( RestUtil.XHEADER_META ),
                                                      false ) );
        metaMap.putAll( RestUtil.parseMetadataHeader( response.getHeaders()
                                                              .getFirst( RestUtil.XHEADER_LISTABLE_META ),
                                                      true ) );

        return metaMap;
    }

    @Override
    public Map<String, Metadata> getSystemMetadata( ObjectIdentifier identifier, String... metadataNames ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "metadata/system" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        if ( metadataNames != null ) {
            for ( String name : metadataNames ) {
                builder.header( RestUtil.XHEADER_TAGS, name );
            }
        }

        ClientResponse response = builder.header( RestUtil.XHEADER_UTF8, "true" ).get( ClientResponse.class );

        return RestUtil.parseMetadataHeader( response.getHeaders().getFirst( RestUtil.XHEADER_META ), false );
    }

    @Override
    public ObjectMetadata getObjectMetadata( ObjectIdentifier identifier ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), null );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();
        ClientResponse response = builder.header( RestUtil.XHEADER_UTF8, "true" ).head();

        Acl acl = new Acl( RestUtil.parseAclHeader( response.getHeaders().getFirst( RestUtil.XHEADER_USER_ACL ) ),
                           RestUtil.parseAclHeader( response.getHeaders()
                                                            .getFirst( RestUtil.XHEADER_GROUP_ACL ) ) );

        Map<String, Metadata> metaMap = new TreeMap<String, Metadata>();
        metaMap.putAll( RestUtil.parseMetadataHeader( response.getHeaders().getFirst( RestUtil.XHEADER_META ),
                                                      false ) );
        metaMap.putAll( RestUtil.parseMetadataHeader( response.getHeaders()
                                                              .getFirst( RestUtil.XHEADER_LISTABLE_META ),
                                                      true ) );

        return new ObjectMetadata( metaMap, acl, response.getType().toString() );
    }

    @Override
    public void setUserMetadata( ObjectIdentifier identifier, Metadata... metadata ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "metadata/user" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        for ( Metadata oneMetadata : metadata ) {
            if ( oneMetadata.isListable() )
                builder.header( RestUtil.XHEADER_LISTABLE_META, oneMetadata.toASCIIString() );
            else builder.header( RestUtil.XHEADER_META, oneMetadata.toASCIIString() );
        }

        builder.header( RestUtil.XHEADER_UTF8, "true" ).post();
    }

    @Override
    public void deleteUserMetadata( ObjectIdentifier identifier, String... names ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "metadata/user" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        for ( String name : names ) {
            builder.header( RestUtil.XHEADER_TAGS, HttpUtil.encodeUtf8( name ) );
        }

        builder.header( RestUtil.XHEADER_UTF8, "true" ).delete();
    }

    @Override
    public Set<String> listMetadata( String metadataName ) {
        URI uri = config.resolvePath( "objects", "listabletags" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        if ( metadataName != null )
            builder.header( RestUtil.XHEADER_TAGS, HttpUtil.encodeUtf8( metadataName ) );

        ClientResponse response = builder.header( RestUtil.XHEADER_UTF8, "true" ).get( ClientResponse.class );
        String headerValue = response.getHeaders().getFirst( RestUtil.XHEADER_LISTABLE_TAGS );

        Set<String> names = new TreeSet<String>();
        if ( headerValue == null ) return names;
        for ( String name : headerValue.split( "," ) )
            names.add( HttpUtil.decodeUtf8( name.trim() ) );

        return names;
    }

    @Override
    public ListObjectsResponse listObjects( ListObjectsRequest request ) {
        if ( request.getMetadataName() == null )
            throw new AtmosException( "You must specify the name of a listable piece of metadata" );

        ClientResponse response;
        try {
            response = build( request ).get( ClientResponse.class );
        } catch ( AtmosException e ) {

            // if the name doesn't exist, return an empty result instead of throwing an exception (requested by users)
            if ( e.getErrorCode() != 1003 ) throw e;
            ListObjectsResponse lor = new ListObjectsResponse();
            lor.setEntries( new ArrayList<ObjectEntry>() );
            return lor;
        }

        request.setToken( response.getHeaders().getFirst( RestUtil.XHEADER_TOKEN ) );
        if ( request.getToken() != null )
            l4j.warn( "Results truncated. Call listObjects again for next page of results." );

        return fillResponse( response.getEntity( ListObjectsResponse.class ), response );
    }

    @Override
    public Acl getAcl( ObjectIdentifier identifier ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "acl" );
        ClientResponse response = client.resource( uri ).get( ClientResponse.class );

        Acl acl = new Acl();
        acl.setUserAcl( RestUtil.parseAclHeader( response.getHeaders().getFirst( RestUtil.XHEADER_USER_ACL ) ) );
        acl.setGroupAcl( RestUtil.parseAclHeader( response.getHeaders().getFirst( RestUtil.XHEADER_GROUP_ACL ) ) );

        return acl;
    }

    @Override
    public void setAcl( ObjectIdentifier identifier, Acl acl ) {
        URI uri = config.resolvePath( identifier.getRelativeResourcePath(), "acl" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        if ( acl != null ) {
            for ( Object value : acl.getUserAclHeader() ) builder.header( RestUtil.XHEADER_USER_ACL, value );
            for ( Object value : acl.getGroupAclHeader() ) builder.header( RestUtil.XHEADER_GROUP_ACL, value );
        }

        builder.post();
    }

    @Override
    public ObjectInfo getObjectInfo( ObjectIdentifier identifier ) {
        return client.resource( config.resolvePath( identifier.getRelativeResourcePath(), "info" ) )
                     .get( ObjectInfo.class );
    }

    @Override
    public ObjectId createVersion( ObjectIdentifier identifier ) {
        ClientResponse response = client.resource(
                config.resolvePath( identifier.getRelativeResourcePath(), "versions" ) )
                                        .post( ClientResponse.class );
        return RestUtil.parseObjectId( response.getLocation().getPath() );
    }

    @Override
    public ListVersionsResponse listVersions( ListVersionsRequest request ) {
        ClientResponse response = build( request ).get( ClientResponse.class );

        request.setToken( response.getHeaders().getFirst( RestUtil.XHEADER_TOKEN ) );
        if ( request.getToken() != null )
            l4j.warn( "Results truncated. Call listVersions again for next page of results." );

        return fillResponse( response.getEntity( ListVersionsResponse.class ), response );
    }

    @Override
    public void restoreVersion( ObjectId objectId, ObjectId versionId ) {
        URI uri = config.resolvePath( objectId.getRelativeResourcePath(), "versions" );
        WebResource.Builder builder = client.resource( uri ).getRequestBuilder();

        builder.header( RestUtil.XHEADER_VERSION_OID, versionId ).put();
    }

    @Override
    public void deleteVersion( ObjectId versionId ) {
        client.resource( config.resolvePath( versionId.getRelativeResourcePath(), "versions" ) ).delete();
    }

    @Override
    public CreateAccessTokenResponse createAccessToken( CreateAccessTokenRequest request )
            throws MalformedURLException {
        ClientResponse response = build( request ).post( ClientResponse.class, request.getPolicy() );
        URI tokenUri = config.resolvePath( response.getLocation().getPath(), response.getLocation().getQuery() );
        return fillResponse( new CreateAccessTokenResponse( tokenUri.toURL() ), response );
    }

    @Override
    public GetAccessTokenResponse getAccessToken( String accessTokenId ) {
        URI uri = config.resolvePath( "accesstokens/" + accessTokenId, "info" );
        ClientResponse response = client.resource( uri ).get( ClientResponse.class );

        return fillResponse( new GetAccessTokenResponse( response.getEntity( AccessToken.class ) ), response );
    }

    @Override
    public void deleteAccessToken( String accessTokenId ) {
        client.resource( config.resolvePath( "accesstokens/" + accessTokenId, null ) ).delete();
    }

    @Override
    public ListAccessTokensResponse listAccessTokens() {
        URI uri = config.resolvePath( "accesstokens", null );
        ClientResponse response = client.resource( uri ).get( ClientResponse.class );
        return fillResponse( response.getEntity( ListAccessTokensResponse.class ), response );
    }

    @Override
    public <T> GenericResponse<T> execute( PreSignedRequest request, Class<T> resultType, Object content )
            throws URISyntaxException {
        WebResource.Builder builder = client.resource( request.getUrl().toURI() ).getRequestBuilder();
        addHeaders( builder, request.getHeaders() ).type( request.getContentType() );
        ClientResponse response = builder.method( request.getMethod(), ClientResponse.class, content );
        return fillResponse( new GenericResponse<T>( response.getEntity( resultType ) ), response );
    }

    protected WebResource.Builder build( Request request ) {
        WebResource resource = client.resource( config.resolvePath( request.getServiceRelativePath(),
                                                                    request.getQuery() ) );
        WebResource.Builder builder = resource.getRequestBuilder();

        if ( request instanceof ContentRequest ) {
            ContentRequest contentRequest = (ContentRequest) request;
            if ( contentRequest.getContentType() == null ) builder.type( AbstractAtmosApi.DEFAULT_CONTENT_TYPE );
            else builder.type( contentRequest.getContentType() );
        }

        return addHeaders( builder, request.generateHeaders() );
    }

    protected WebResource.Builder addHeaders( WebResource.Builder builder, Map<String, List<Object>> headers ) {
        for ( String name : headers.keySet() ) {
            for ( Object value : headers.get( name ) ) {
                builder.header( name, value );
            }
        }
        return builder;
    }

    protected <T extends BasicResponse> T fillResponse( T response, ClientResponse clientResponse ) {
        ClientResponse.Status status = clientResponse.getClientResponseStatus();
        MediaType type = clientResponse.getType();
        URI location = clientResponse.getLocation();
        response.setHttpStatus( clientResponse.getStatus() );
        response.setHttpMessage( status == null ? null : status.getReasonPhrase() );
        response.setHeaders( clientResponse.getHeaders() );
        response.setContentType( type == null ? null : type.toString() );
        response.setContentLength( clientResponse.getLength() );
        response.setLocation( location == null ? null : location.toString() );
        response.setLastModified( clientResponse.getLastModified() );
        response.setDate( clientResponse.getResponseDate() );
        return response;
    }

    protected Object getContent( ContentRequest request ) {
        if ( request.getContent() == null ) return ""; // need this to provide Content-Length: 0

        else if ( request.getContent() instanceof InputStream ) {
            if ( request.getContentLength() < 0 )
                throw new UnsupportedOperationException(
                        "Content request with input stream must provide content length" );

            if ( request.getContentLength() == 0 )
                l4j.warn( "Content request with input stream and zero-length will not send any data" );

            return new MeasuredInputStream( (InputStream) request.getContent(), request.getContentLength() );

        } else return request.getContent();
    }
}
