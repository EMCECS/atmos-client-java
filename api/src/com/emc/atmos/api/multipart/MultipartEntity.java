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

public class MultipartEntity extends ArrayList<MultipartPart> {
    private static final long serialVersionUID = -4788353053749563899L;

    private static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile( "^Content-Type: (.+)$" );
    private static final Pattern PATTERN_CONTENT_RANGE = Pattern.compile( "^Content-Range: bytes (\\d+)-(\\d+)/(\\d+)$" );

    public static MultipartEntity fromStream( InputStream is, String boundary ) throws IOException {
        if ( boundary.startsWith( "--" ) ) boundary = boundary.substring( 2 );

        List<MultipartPart> parts = new ArrayList<MultipartPart>();

        try {
            while ( true ) {

                // first, we expect a boundary ( EOL + '--' + <boundary_string> + EOL )
                if ( !"".equals( StreamUtil.readLine( is ) ) )
                    throw new MultipartException( "Parse error: expected EOL before boundary" );
                String line = StreamUtil.readLine( is );

                // two dashes after the boundary means EOS
                if ( ("--" + boundary + "--").equals( line ) ) break;

                if ( !("--" + boundary).equals( line ) ) throw new MultipartException(
                        "Parse error: expected [--" + boundary + "], instead got [" + line + "]" );

                // the first line should be the content-type for the part
                Matcher matcher = PATTERN_CONTENT_TYPE.matcher( StreamUtil.readLine( is ) );
                if ( !matcher.find() ) throw new MultipartException( "Parse error: No content-type specified in part" );
                String contentType = matcher.group( 1 );

                // the second line should be the content-range for the part
                matcher = PATTERN_CONTENT_RANGE.matcher( StreamUtil.readLine( is ) );
                if ( !matcher.find() )
                    throw new MultipartException( "Parse error: No content-range specified in part" );
                int start = Integer.parseInt( matcher.group( 1 ) ), end = Integer.parseInt( matcher.group( 2 ) );
                int length = end - start + 1;
                // int total = Integer.parseInt( matcher.group( 3 ) );

                // next there should be a blank line
                if ( !"".equals( StreamUtil.readLine( is ) ) )
                    throw new MultipartException( "Parse error: expected blank line after part headers" );

                // then the data of the part
                byte[] data = new byte[length];
                int read, count = 0;
                while ( count < length ) {
                    read = is.read( data, 0, length - count );
                    count += read;
                }

                parts.add( new MultipartPart( contentType, new Range( start, end ), data ) );
            }
        } finally {
            is.close();
        }

        return new MultipartEntity( parts );
    }

    public MultipartEntity( List<MultipartPart> parts ) {
        super( parts );
    }

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
