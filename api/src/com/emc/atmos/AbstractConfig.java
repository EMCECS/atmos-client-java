package com.emc.atmos;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class AbstractConfig {
    private static final Logger l4j = Logger.getLogger( AbstractConfig.class );

    protected String context;
    protected URI[] endpoints;
    protected boolean disableSslValidation = false;
    protected int resolveCount = 0;
    protected ThreadLocal<URI> threadEndpoint = new ThreadLocal<URI>();

    public AbstractConfig() {
    }

    public AbstractConfig( String context, URI... endpoints ) {
        this.context = context;
        this.endpoints = endpoints;
    }

    public URI resolvePath( String relativePath, String query ) {
        String path = relativePath;

        // make sure we have a root path
        if ( path.length() == 0 || path.charAt( 0 ) != '/' ) path = '/' + path;

        // don't add the context if it's already there
        if ( !path.startsWith( context ) ) path = context + path;

        // tie the endpoint to the current thread to eliminate MDS sync issues when using multiple endpoints
        URI endpoint = threadEndpoint.get();
        if ( endpoint == null ) {
            endpoint = endpoints[resolveCount++ % endpoints.length];
            threadEndpoint.set( endpoint );
        }

        try {
            URI uri = new URI( endpoint.getScheme(), null, endpoint.getHost(), endpoint.getPort(),
                               path, query, null );
            l4j.debug( "raw path & query: " + path + "?" + query );
            l4j.debug( "encoded URI: " + uri );
            return uri;
        } catch ( URISyntaxException e ) {
            throw new RuntimeException( "Invalid URI syntax", e );
        }
    }

    public String getContext() {
        return context;
    }

    public void setContext( String context ) {
        this.context = context;
    }

    public boolean isDisableSslValidation() {
        return disableSslValidation;
    }

    public void setDisableSslValidation( boolean disableSslValidation ) {
        this.disableSslValidation = disableSslValidation;
    }

    public URI[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints( URI[] endpoints ) {
        this.endpoints = endpoints;
    }

    public static enum HostSelectionAlgorithm {
        RoundRobin, ThreadAssignment
    }
}
