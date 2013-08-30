package com.emc.atmos;

import java.net.URI;

public interface LoadBalancingAlgorithm {
    URI getNextEndpoint( URI[] endpoints );
}
