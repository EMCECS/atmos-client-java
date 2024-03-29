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
package com.emc.atmos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds configuration parameters common between Atmos data and mgmt APIs.
 * <p/>
 * If multiple endpoints are provided, they will be balanced (round-robin style) between threads and each thread will
 * be assigned its own endpoint to avoid MDS sync issues.
 */
public class AbstractConfig {
    private static final Logger l4j = LoggerFactory.getLogger( AbstractConfig.class );

    private String context;
    private URI[] endpoints;
    private boolean disableSslValidation = false;
    private LoadBalancingAlgorithm loadBalancingAlgorithm = new RoundRobinAlgorithm();

    public AbstractConfig( String context, URI... endpoints ) {
        this.context = context;
        this.endpoints = endpoints;
    }

    /**
     * @deprecated Please use {@link #resolveHostAndPath(String, String)} instead
     */
    public URI resolvePath( String relativePath, String query ) {
        return resolveHostAndPath(relativePath, query);
    }

    /**
     * Resolves a path relative to the API context. The returned URI will be of the format
     * scheme://host[:port]/context/relativePath?query. The scheme, host and port (endpoint) to use is delegated to the
     * configured loadBalancingAlgorithm to balance load across multiple endpoints.
     */
    public URI resolveHostAndPath(String relativePath, String query) {
        String path = relativePath;

        // make sure we have a root path
        if (path.length() == 0 || path.charAt(0) != '/') path = '/' + path;

        // don't add the context if it's already there
        if (!path.startsWith(context)) path = context + path;

        // normalize double-slashes *before* signing
        // previously, older versions of apache httpclient would not normalize these - they would get signed,
        // sent over the wire as-is, and ECS/Atmos would normalize the path on the other side (so foo/ and foo//
        // normalize to the same directory)
        // recent versions of httpclient will normalize double-slash, so the path sent over the wire is different from
        // the path that was signed (which invalidates the signature and causes a 403)
        // this change is necessary to keep the behavior consistent, since we did not previously reject
        // double-slashes (that would be the appropriate behavior)
        path = path.replaceAll("//", "/");

        return resolveHost(path, query);
    }

    public URI resolveHost(String absolutePath, String query) {
        URI endpoint = loadBalancingAlgorithm.getNextEndpoint(endpoints);

        try {
            URI uri = new URI(endpoint.getScheme(), null, endpoint.getHost(), endpoint.getPort(),
                    absolutePath, query, null);
            l4j.debug("raw path & query: " + absolutePath + "?" + query);
            l4j.debug("encoded URI: " + uri);
            return uri;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI syntax", e);
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
