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
package com.emc.atmos.api.multipart;

import com.emc.atmos.api.Range;
import com.emc.util.StreamUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a multipart response entity.
 */
public class MultipartEntity extends ArrayList<MultipartPart> {
    private static final long serialVersionUID = -4788353053749563899L;

    private static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile( "^Content-Type: (.+)$" );
    private static final Pattern PATTERN_CONTENT_RANGE = Pattern.compile( "^Content-Range: bytes (\\d+)-(\\d+)/(\\d+)$" );

    /**
     * Parses a multipart response body provided by an InputStream. Returns an instance of this class that represents
     * the response. boundary may start with "--" or omit it.
     */
    public static MultipartEntity fromStream( InputStream is, String boundary ) throws IOException {
        if ( boundary.startsWith( "--" ) ) boundary = boundary.substring( 2 );

        List<MultipartPart> parts = new ArrayList<MultipartPart>();

        try {
            while ( true ) {
                String line = StreamUtil.readLine( is );

                // there *may* be an additional CRLF before the first boundary
                if ( "".equals( line ) ) line = StreamUtil.readLine( is );

                // two dashes after the boundary means EOS
                if ( ("--" + boundary + "--").equals( line ) ) break;

                if ( !("--" + boundary).equals( line ) ) throw new MultipartException(
                        "Parse error: expected [--" + boundary + "], instead got [" + line + "]" );

                Matcher matcher;
                String contentType = null;
                int start = -1, end = 0, length = 0;
                while ( !"".equals( line = StreamUtil.readLine( is ) ) ) {
                    matcher = PATTERN_CONTENT_TYPE.matcher( line );
                    if ( matcher.find() ) {
                        contentType = matcher.group( 1 );
                        continue;
                    }

                    matcher = PATTERN_CONTENT_RANGE.matcher( line );
                    if ( matcher.find() ) {
                        start = Integer.parseInt( matcher.group( 1 ) );
                        end = Integer.parseInt( matcher.group( 2 ) );
                        length = end - start + 1;
                        // total = Integer.parseInt( matcher.group( 3 ) );
                        continue;
                    }

                    throw new MultipartException( "Unrecognized header line: " + line );
                }

                if ( contentType == null )
                    throw new MultipartException( "Parse error: No content-type specified in part" );

                if ( start == -1 )
                    throw new MultipartException( "Parse error: No content-range specified in part" );

                // then the data of the part
                byte[] data = new byte[length];
                int read, count = 0;
                while ( count < length ) {
                    read = is.read( data, 0, length - count );
                    count += read;
                }

                parts.add( new MultipartPart( contentType, new Range( start, end ), data ) );

                // after each data block there should be a CRLF
                if ( !"".equals( StreamUtil.readLine( is ) ) )
                    throw new MultipartException( "Parse error: expected EOL before boundary" );
            }
        } finally {
            is.close();
        }

        return new MultipartEntity( parts );
    }

    public MultipartEntity( List<MultipartPart> parts ) {
        super( parts );
    }

    /**
     * Convenience method that aggregates the bytes of all parts into one contiguous byte array.
     */
    public byte[] aggregateBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for ( MultipartPart part : this ) {
                baos.write( part.getData() );
            }
            return baos.toByteArray();
        } catch ( IOException e ) {
            throw new RuntimeException( "Unexpected error", e ); // unrecoverable
        }
    }
}
