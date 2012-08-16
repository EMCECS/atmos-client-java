package com.emc.acdp.api.jaxrs;

import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpException;
import com.emc.util.SslUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import javax.ws.rs.core.MediaType;

public class RestUtil {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String METHOD_POST = "POST";

    public static final String TYPE_FORM_DATA = "application/x-www-form-urlencoded";
    public static final String TYPE_XML = "text/xml";

    public static final MediaType MEDIA_TYPE_XML = new MediaType( "text", "xml" );

    public static Client createClient( AcdpConfig config ) {
        try {
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getClasses().add( AcdpWriter.class );
            clientConfig.getClasses().add( AcdpReader.class );

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

    private RestUtil() {
    }
}
