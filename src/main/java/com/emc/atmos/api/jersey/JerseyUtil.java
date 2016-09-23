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
package com.emc.atmos.api.jersey;

import com.emc.atmos.AtmosException;
import com.emc.atmos.api.AtmosConfig;
import com.emc.atmos.api.jersey.provider.*;
import com.emc.util.SslUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.core.impl.provider.entity.ByteArrayProvider;
import com.sun.jersey.core.impl.provider.entity.FileProvider;
import com.sun.jersey.core.impl.provider.entity.StringProvider;
import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.List;

public class JerseyUtil {
    public static Client createClient( AtmosConfig config,
                                       List<Class<MessageBodyReader<?>>> readers,
                                       List<Class<MessageBodyWriter<?>>> writers ) {
        try {
            ClientConfig clientConfig = new DefaultClientConfig();

            // register an open trust manager to allow SSL connections to servers with self-signed certificates
            if ( config.isDisableSslValidation() ) {
                clientConfig.getProperties().put( HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                                                  new HTTPSProperties( SslUtil.gullibleVerifier,
                                                                       SslUtil.createGullibleSslContext() ) );
            }

            addHandlers( clientConfig, readers, writers );

            Client client;
            if ( config.getProxyUri() != null ) {

                // set proxy configuration
                HttpURLConnectionFactory factory = new ProxyURLConnectionFactory( config.getProxyUri(),
                                                                                  config.getProxyUser(),
                                                                                  config.getProxyPassword() );
                client = new Client( new URLConnectionClientHandler( factory ), clientConfig );
            } else {

                // this should pick up proxy config from system properties
                client = Client.create( clientConfig );
            }

            // add preemptive proxy authorization (this will only work for unencrypted requests)
            String proxyUser = config.getProxyUser(), proxyPassword = config.getProxyPassword();
            if ( proxyUser == null ) {
                proxyUser = System.getProperty( "http.proxyUser" );
                proxyPassword = System.getProperty( "http.proxyPassword" );
            }
            client.addFilter( new ProxyAuthFilter( proxyUser, proxyPassword ) );

            addFilters( client, config );

            return client;

        } catch ( Exception e ) {
            throw new AtmosException( "Error configuring REST client", e );
        }
    }

    static void addHandlers( ClientConfig clientConfig,
                             List<Class<MessageBodyReader<?>>> readers,
                             List<Class<MessageBodyWriter<?>>> writers ) {
        // add our message body handlers
        clientConfig.getClasses().clear();

        // custom types and buffered writers to ensure content-length is set
        clientConfig.getClasses().add( MeasuredStringWriter.class );
        clientConfig.getClasses().add( MeasuredJaxbWriter.App.class );
        clientConfig.getClasses().add( MeasuredJaxbWriter.Text.class );
        clientConfig.getClasses().add( MeasuredJaxbWriter.General.class );
        clientConfig.getClasses().add( MeasuredInputStreamWriter.class );
        clientConfig.getClasses().add( BufferSegmentWriter.class );
        clientConfig.getClasses().add( MultipartReader.class );

        // Jersey providers for types we support
        clientConfig.getClasses().add( ByteArrayProvider.class );
        clientConfig.getClasses().add( FileProvider.class );
        clientConfig.getClasses().add( StringProvider.class );
        clientConfig.getClasses().add( XMLRootElementProvider.App.class );
        clientConfig.getClasses().add( XMLRootElementProvider.Text.class );
        clientConfig.getClasses().add( XMLRootElementProvider.General.class );

        // user-defined types
        if ( readers != null ) {
            for ( Class<MessageBodyReader<?>> reader : readers ) {
                clientConfig.getClasses().add( reader );
            }
        }
        if ( writers != null ) {
            for ( Class<MessageBodyWriter<?>> writer : writers ) {
                clientConfig.getClasses().add( writer );
            }
        }
    }

    static void addFilters( Client client, AtmosConfig config ) {
        // add filters
        client.addFilter( new ChecksumFilter() );
        client.addFilter( new ErrorFilter() );
        if ( config.isEnableRetry() ) client.addFilter( new RetryFilter( config ) );
        client.addFilter( new AuthFilter( config ) );
    }

    private JerseyUtil() {
    }
}
