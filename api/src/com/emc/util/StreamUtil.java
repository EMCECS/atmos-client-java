package com.emc.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
    public static String readAsString( InputStream in ) throws IOException {
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

    public static byte[] readAsBytes( InputStream in, int expectedLength ) throws IOException {
        try {
            byte[] output = new byte[expectedLength];

            int c = 0;
            while ( c < expectedLength ) {
                int read = in.read( output, c, expectedLength - c );
                if ( read == -1 ) {
                    // EOF!
                    throw new EOFException(
                            "EOF reading response at position " + c
                            + " size " + (expectedLength - c) );
                }
                c += read;
            }

            return output;
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
    }

    public static byte[] readAsBytes( InputStream in ) throws IOException {
        try {
            byte[] buffer = new byte[4096];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int c = 0;
            while ( (c = in.read( buffer )) != -1 ) {
                baos.write( buffer, 0, c );
            }
            baos.close();

            return baos.toByteArray();
        } finally {
            if ( in != null ) {
                in.close();
            }
        }
    }

    private StreamUtil() {
    }
}
