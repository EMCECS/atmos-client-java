package com.emc.acdp.api.jersey;

import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpException;
import com.emc.util.SslUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class JerseyUtil {
    public static Client createClient( AcdpConfig config ) {
        try {
            ClientConfig clientConfig = new DefaultClientConfig();

            // register an open trust manager to allow SSL connections to servers with self-signed certificates
            if ( config.isDisableSslValidation() && "https".equals( config.getProto().toLowerCase() ) ) {
                clientConfig.getProperties().put( HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                                                  new HTTPSProperties( SslUtil.gullibleVerifier,
                                                                       SslUtil.createGullibleSslContext() ) );
            }

            Client client = Client.create( clientConfig );
            client.addFilter( new ErrorFilter() );
            client.addFilter( new AuthFilter( config ) );
            return client;

        } catch ( Exception e ) {
            throw new AcdpException( "Error configuring REST client", e );
        }
    }

    private JerseyUtil() {
    }
}
