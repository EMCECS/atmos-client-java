package com.emc.atmos;

import java.net.URI;

/**
 * This implementation will tie a specific endpoint to each thread to avoid MDS sync issues. However, multiple threads
 * will be distributed between the configured endpoints.
 */
public class StickyThreadAlgorithm implements LoadBalancingAlgorithm {
    protected ThreadLocal<URI> threadEndpoint = new ThreadLocal<URI>();
    protected int callCount = 0;

    @Override
    public URI getNextEndpoint( URI[] endpoints ) {
        // tie the endpoint to the current thread to eliminate MDS sync issues when using multiple endpoints
        URI endpoint = threadEndpoint.get();
        if ( endpoint == null ) {
            endpoint = endpoints[callCount++ % endpoints.length];
            threadEndpoint.set( endpoint );
        }
        return endpoint;
    }
}
