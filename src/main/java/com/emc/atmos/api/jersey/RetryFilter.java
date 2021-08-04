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
import com.emc.atmos.api.RestUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class RetryFilter extends ClientFilter {
    private static final int ATMOS_1040_DELAY_MS = 300;

    private static final Logger log = LoggerFactory.getLogger( RetryFilter.class );

    private AtmosConfig config;

    public RetryFilter( AtmosConfig config ) {
        this.config = config;
    }

    @Override
    public ClientResponse handle( ClientRequest clientRequest ) throws ClientHandlerException {
        int retryCount = 0;
        InputStream entityStream = null;
        if ( clientRequest.getEntity() instanceof InputStream ) entityStream = (InputStream) clientRequest.getEntity();
        while ( true ) {
            try {
                // if using an InputStream, mark the stream so we can rewind it in case of an error
                if ( entityStream != null && entityStream.markSupported() )
                    entityStream.mark( config.getRetryBufferSize() );

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

                    // retry all IO exceptions unless wschecksum is enabled (can't overwrite data in this case)
                } else if ( !(t instanceof IOException)
                            || clientRequest.getHeaders().getFirst( RestUtil.XHEADER_WSCHECKSUM ) != null ) throw orig;

                // only retry maxRetries times
                if ( ++retryCount > config.getMaxRetries() ) throw orig;

                // attempt to reset InputStream if it has been read from
                if ( entityStream != null ) {
                    if ( !(entityStream instanceof MeasuredInputStream)
                         || ((MeasuredInputStream) entityStream).getRead() > 0 ) {
                        try {
                            if ( !entityStream.markSupported() ) throw new IOException( "Mark is not supported" );
                            entityStream.reset();
                        } catch ( IOException e ) {
                            log.warn( "Could not reset entity stream for retry: " + e.getMessage() );
                            throw orig;
                        }
                    }
                }

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
