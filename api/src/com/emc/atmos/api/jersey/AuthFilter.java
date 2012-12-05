package com.emc.atmos.api.jersey;

import com.emc.atmos.api.AtmosConfig;
import com.emc.atmos.api.RestUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class AuthFilter extends ClientFilter {
    private AtmosConfig config;

    public AuthFilter( AtmosConfig config ) {
        this.config = config;
    }

    @Override
    public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {
        RestUtil.signRequest( request.getMethod(),
                              request.getURI().getPath(),
                              request.getURI().getQuery(),
                              request.getHeaders(),
                              config.getTokenId(),
                              config.getSecretKey(),
                              config.getServerClockSkew() );

        return getNext().handle( request );
    }
}
