package com.emc.acdp.api.jersey;

import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpMgmtApi;
import com.emc.cdp.services.rest.model.Identity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

public class AcdpMgmtApiClient implements AcdpMgmtApi {
    private AcdpConfig config;
    private Client client;

    public AcdpMgmtApiClient( AcdpConfig config ) {
        this.config = config;
        this.client = JerseyUtil.createClient( config );
    }

    @Override
    public void createIdentity( Identity identity ) {
        WebResource resource = client.resource( getMgmtUri() + "/identities" );
        resource.type( MediaType.TEXT_XML );
        resource.post( identity );
    }

    private String getMgmtUri() {
        return config.getBaseUri() + "/cdp-rest/v1";
    }
}
