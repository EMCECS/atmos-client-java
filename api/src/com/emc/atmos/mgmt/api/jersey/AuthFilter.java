package com.emc.atmos.mgmt.api.jersey;

import com.emc.atmos.mgmt.AtmosMgmtConfig;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class AuthFilter extends ClientFilter {
    private AtmosMgmtConfig config;

    public AuthFilter( AtmosMgmtConfig config ) {
        this.config = config;
    }

    @Override
    public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {
        request.getHeaders().putAll( config.getAuthenticationHeaders() );

        return getNext().handle( request );
    }
}
