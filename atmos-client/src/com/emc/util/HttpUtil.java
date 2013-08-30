// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class HttpUtil {
    private static final String HEADER_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> headerFormat = new ThreadLocal<DateFormat>();
    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ThreadLocal<DateFormat> iso8601Format = new ThreadLocal<DateFormat>();
    private static final Logger l4j = Logger.getLogger( HttpUtil.class );

    public static synchronized String headerFormat( Date date ) {
        return getHeaderFormat().format( date );
    }

    public static String encodeUtf8( String value ) {
        // Use %20, not +
        try {
            return URLEncoder.encode( value, "UTF-8" ).replace( "+", "%20" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "UTF-8 encoding isn't supported on this system", e ); // unrecoverable
        }
    }

    public static String decodeUtf8( String value ) {
        try {
            return URLDecoder.decode( value, "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "UTF-8 encoding isn't supported on this system", e ); // unrecoverable
        }
    }

    /**
     * Reads the response body and returns it as a string.
     *
     * @param con the HTTP connection
     *
     * @return the string containing the response body
     *
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
     *
     * @return the byte array containing the response body. Note that if you
     *         pass in a buffer, this will the same buffer object. Be sure to
     *         check the content length to know what data in the buffer is valid
     *         (from zero to contentLength).
     *
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

    private static DateFormat getHeaderFormat() {
        DateFormat format = headerFormat.get();
        if ( format == null ) {
            format = new SimpleDateFormat( HEADER_FORMAT, Locale.ENGLISH );
            format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            headerFormat.set( format );
        }
        return format;
    }

    public static DateFormat get8601Format() {
        DateFormat format = iso8601Format.get();
        if ( format == null ) {
            format = new SimpleDateFormat( ISO_8601_FORMAT );
            format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            iso8601Format.set( format );
        }
        return format;
    }
}
