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

import java.io.*;

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

    /**
     * Reads from the input stream until a linefeed is encountered. All data up until that point is returned as a
     * string. If the byte preceding the linefeed is a carriage return, that is also removed from the returned value.
     * The stream is positioned immediately after the linefeed.
     */
    public static String readLine( InputStream in ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c = in.read();
        if ( c == -1 || c == '\n' ) return "";
        int c2 = in.read();

        while ( c2 != -1 && (char) c2 != '\n' ) {
            baos.write( c );
            c = c2;
            c2 = in.read();
        }

        if ( (char) c != '\r' ) baos.write( c );

        return new String( baos.toByteArray(), "UTF-8" );
    }

    public static long copy( InputStream is, OutputStream os, long maxBytes ) throws IOException {
        byte[] buffer = new byte[1024 * 64]; // 64k buffer
        long count = 0;
        int read = 0, maxRead;

        while ( count < maxBytes ) {
            maxRead = (int) Math.min( (long) buffer.length, maxBytes - count );
            if ( -1 == (read = is.read( buffer, 0, maxRead )) ) break;
            os.write( buffer, 0, read );
            count += read;
        }
        return count;
    }

    private StreamUtil() {
    }
}
