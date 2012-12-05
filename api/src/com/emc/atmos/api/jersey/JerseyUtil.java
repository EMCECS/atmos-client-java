package com.emc.atmos.api.jersey;

import com.emc.atmos.api.AtmosConfig;
import com.emc.atmos.AtmosException;
import com.emc.util.SslUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class JerseyUtil {
    public static Client createClient( AtmosConfig config, ClientHandler root ) {
        try {
            ClientConfig clientConfig = new DefaultClientConfig();

            // register an open trust manager to allow SSL connections to servers with self-signed certificates
            if ( config.isDisableSslValidation() ) {
                clientConfig.getProperties().put( HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                                                  new HTTPSProperties( SslUtil.gullibleVerifier,
                                                                       SslUtil.createGullibleSslContext() ) );
            }

            clientConfig.getClasses().add( MeasuredInputStreamWriter.class );
            clientConfig.getClasses().add( BufferSegmentWriter.class );
            clientConfig.getClasses().add( MultipartReader.class );

            Client client = (root == null) ? Client.create( clientConfig ) : new Client( root, clientConfig );
            configureClient( client, config );
            return client;

        } catch ( Exception e ) {
            throw new AtmosException( "Error configuring REST client", e );
        }
    }

    /**
     * Note that this method cannot disable SSL validation, so that configuration option is ignored here. You are
     * responsible for configuring the client with any proxy, ssl or other options prior to calling this constructor.
     */
    public static void configureClient( Client client, AtmosConfig config ) {
        client.addFilter( new ErrorFilter() );
        client.addFilter( new AuthFilter( config ) );
    }

    private JerseyUtil() {
    }
}
