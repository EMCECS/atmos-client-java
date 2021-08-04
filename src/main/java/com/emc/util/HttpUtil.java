/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2013-2018, Dell EMC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.emc.util;

import com.sun.jersey.core.header.HttpDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger( HttpUtil.class );

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_EXPECT = "Expect";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_COOKIE = "Cookie";

    private static final String HEADER_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> headerFormat = new ThreadLocal<DateFormat>();

    public static String headerFormat( Date date ) {
        return getHeaderFormat().format( date );
    }

    public static Date safeHeaderParse( String dateString ) {
        if ( dateString == null || dateString.trim().length() == 0 ) return null;
        try {
            return HttpDateFormat.readDate( dateString );
        } catch ( ParseException e ) {
            log.warn( "Cannot parse date header: " + dateString, e );
            return null;
        }
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
