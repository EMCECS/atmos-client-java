/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.util;

import java.io.UnsupportedEncodingException;
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
            // don't want '+' decoded to a space
            return URLDecoder.decode( value.replace( "+", "%2B" ), "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "UTF-8 encoding isn't supported on this system", e ); // unrecoverable
        }
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
}
