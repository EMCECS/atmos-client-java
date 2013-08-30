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
package com.emc.atmos;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds configuration parameters common between Atmos data and mgmt APIs.
 * <p/>
 * If multiple endpoints are provided, they will be balanced (round-robin style) between threads and each thread will
 * be assigned its own endpoint to avoid MDS sync issues.
 */
public class AbstractConfig {
    private static final Logger l4j = Logger.getLogger( AbstractConfig.class );

    protected String context;
    protected URI[] endpoints;
    protected boolean disableSslValidation = false;
    protected int resolveCount = 0;
    protected LoadBalancingAlgorithm loadBalancingAlgorithm = new RoundRobinAlgorithm();
    protected ThreadLocal<URI> threadEndpoint = new ThreadLocal<URI>();

    public AbstractConfig( String context, URI... endpoints ) {
        this.context = context;
        this.endpoints = endpoints;
    }

    /**
     * Resolves a path relative to the API context. The returned URI will be of the format
     * scheme://host[:port]/context/relativePath?query. The scheme, host and port (endpoint) to use is delegated to the
     * configured loadBalancingAlgorithm to balance load across multiple endpoints.
     */
    public URI resolvePath( String relativePath, String query ) {
        String path = relativePath;

        // make sure we have a root path
        if ( path.length() == 0 || path.charAt( 0 ) != '/' ) path = '/' + path;

        // don't add the context if it's already there
        if ( !path.startsWith( context ) ) path = context + path;

        URI endpoint = loadBalancingAlgorithm.getNextEndpoint( endpoints );

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

    /**
     * Returns the base API context (i.e. "/rest" for the Atmos data API).
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the base API context (i.e. "/rest" for the Atmos data API).
     */
    public void setContext( String context ) {
        this.context = context;
    }

    /**
     * Returns whether SSL validation should be disabled (allowing self-signed certificates to be used for https
     * requests).
     */
    public boolean isDisableSslValidation() {
        return disableSslValidation;
    }

    /**
     * Sets whether SSL validation should be disabled (allowing self-signed certificates to be used for https
     * requests).
     */
    public void setDisableSslValidation( boolean disableSslValidation ) {
        this.disableSslValidation = disableSslValidation;
    }

    /**
     * Returns the configured endpoints.
     */
    public URI[] getEndpoints() {
        return endpoints;
    }

    /**
     * Sets the configured endpoints. These URIs should be of the format scheme://host[:port]. They should not contain
     * a path or query.
     */
    public void setEndpoints( URI[] endpoints ) {
        this.endpoints = endpoints;
    }

    /**
     * Returns the load balancing algorithm implementation used to distribute requests between multiple endpoints.
     */
    public LoadBalancingAlgorithm getLoadBalancingAlgorithm() {
        return loadBalancingAlgorithm;
    }

    /**
     * Sets the load balancing algorithm implementation used to distribute requests between multiple endpoints.
     */
    public void setLoadBalancingAlgorithm( LoadBalancingAlgorithm loadBalancingAlgorithm ) {
        this.loadBalancingAlgorithm = loadBalancingAlgorithm;
    }
}
