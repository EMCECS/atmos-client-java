package com.emc.atmos.api.jersey;

import com.emc.atmos.AtmosException;
import com.emc.atmos.api.AtmosConfig;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RetryFilter extends ClientFilter {
    private static final int ATMOS_1040_DELAY_MS = 300;

    private static final Logger log = Logger.getLogger( RetryFilter.class );

    private AtmosConfig config;

    public RetryFilter( AtmosConfig config ) {
        this.config = config;
    }

    @Override
    public ClientResponse handle( ClientRequest clientRequest ) throws ClientHandlerException {
        int retryCount = 0;
        while ( true ) {
            try {
                return getNext().handle( clientRequest );
            } catch ( RuntimeException orig ) {
                Throwable t = orig;

                // in this case, the exception was wrapped by Jersey
                if ( t instanceof ClientHandlerException ) t = t.getCause();

                if ( t instanceof AtmosException ) {
                    AtmosException ae = (AtmosException) t;

                    // retry all 50x errors
                    if ( ae.getHttpCode() < 500 ) throw orig;

                    // add small delay to Atmos code 1040 (server busy)
                    if ( ae.getErrorCode() == 1040 ) {
                        try {
                            Thread.sleep( ATMOS_1040_DELAY_MS );
                        } catch ( InterruptedException e ) {
                            log.warn( "Interrupted while waiting after a 1040 response: " + e.getMessage() );
                        }
                    }

                    // retry all IO exceptions
                } else if ( !(t instanceof IOException) ) throw orig;

                // only retry maxRetries times
                if ( ++retryCount > config.getMaxRetries() ) throw orig;

                log.info( "Error received in response (" + t + "), retrying..." );

                // wait for retry delay
                if ( config.getRetryDelayMillis() > 0 ) {
                    try {
                        Thread.sleep( config.getRetryDelayMillis() );
                    } catch ( InterruptedException e ) {
                        log.warn( "Interrupted while waiting to retry: " + e.getMessage() );
                    }
                }
            }
        }
    }
}
