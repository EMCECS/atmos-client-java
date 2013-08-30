package com.emc.atmos;

import java.net.URI;

/**
 * Simple implementation that returns subsequent endpoints for each call.
 */
public class RoundRobinAlgorithm implements LoadBalancingAlgorithm {
    private int callCount = 0;

    @Override
    public URI getNextEndpoint( URI[] endpoints ) {
        return endpoints[callCount++ % endpoints.length];
    }
}
