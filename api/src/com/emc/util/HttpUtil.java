package com.emc.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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
        return StreamUtil.readAsString( in );
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
        int contentLength = con.getContentLength();
        // If we know the content length, read it directly into a buffer.
        if ( contentLength != -1 ) {
            return StreamUtil.readAsBytes( in, contentLength );

            // Else, use a ByteArrayOutputStream to collect the response.
        } else {
            l4j.debug( "Content length is unknown.  Buffering output." );
            byte[] data = StreamUtil.readAsBytes( in );
            l4j.debug( "Buffered " + data.length + " response bytes" );
            return data;
        }
    }

    /**
     * Simply writes the given content to the given connection's output stream.
     * WARNING: DO NOT connect the connection before calling this method (it will be connected here)
     */
    public static void writeRequest( HttpURLConnection con, String content ) throws IOException {
        con.setDoOutput( true );
        con.setFixedLengthStreamingMode( content.getBytes( "UTF-8" ).length );
        con.connect();
        OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() );
        writer.write( content );
        writer.flush();
        writer.close();
    }
}
