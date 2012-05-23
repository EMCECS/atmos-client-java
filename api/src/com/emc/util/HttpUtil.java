package com.emc.util;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpUtil {
    private static final Logger l4j = Logger.getLogger( HttpUtil.class );

    /**
     * Reads the response body and returns it as a string.
     *
     * @param con the HTTP connection
     * @return the string containing the response body
     * @throws java.io.IOException if reading the response stream fails
     */
    public static String readResponseString( HttpURLConnection con )
            throws IOException {
        InputStream in = null;
        if ( con.getResponseCode() > 299 ) {
            in = con.getErrorStream();
        }
        if ( in == null ) {
            in = con.getInputStream();
        }
        if ( in == null ) {
            // could not get stream
            return "";
        }
        try {
            return new java.util.Scanner( in, "UTF-8" ).useDelimiter( "\\A" ).next();
        } catch ( java.util.NoSuchElementException e ) {
            return "";
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
    }

    /**
     * Reads the response body and returns it in a byte array.
     *
     * @param con the HTTP connection
     * @return the byte array containing the response body. Note that if you
     *         pass in a buffer, this will the same buffer object. Be sure to
     *         check the content length to know what data in the buffer is valid
     *         (from zero to contentLength).
     * @throws IOException if reading the response stream fails.
     */
    public static byte[] readResponse( HttpURLConnection con )
            throws IOException {
        InputStream in = null;
        if ( con.getResponseCode() > 299 ) {
            in = con.getErrorStream();
            if ( in == null ) {
                in = con.getInputStream();
            }
        } else {
            in = con.getInputStream();
        }
        if ( in == null ) {
            // could not get stream
            return new byte[0];
        }
        try {
            byte[] output;
            int contentLength = con.getContentLength();
            // If we know the content length, read it directly into a buffer.
            if ( contentLength != -1 ) {
                output = new byte[con.getContentLength()];

                int c = 0;
                while ( c < contentLength ) {
                    int read = in.read( output, c, contentLength - c );
                    if ( read == -1 ) {
                        // EOF!
                        throw new EOFException(
                                "EOF reading response at position " + c
                                        + " size " + (contentLength - c) );
                    }
                    c += read;
                }

                return output;
            } else {
                l4j.debug( "Content length is unknown.  Buffering output." );
                // Else, use a ByteArrayOutputStream to collect the response.
                byte[] buffer = new byte[4096];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int c = 0;
                while ( (c = in.read( buffer )) != -1 ) {
                    baos.write( buffer, 0, c );
                }
                baos.close();

                l4j.debug( "Buffered " + baos.size() + " response bytes" );

                return baos.toByteArray();
            }
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
    }
}
