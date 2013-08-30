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
package com.emc.atmos.sync.util;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class Iso8601Util {
    private static final String ISO_8601_DATE_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final Logger l4j = Logger.getLogger(Iso8601Util.class);

    private static final ThreadLocal<DateFormat> iso8601Format = new ThreadLocal<DateFormat>();

    public static Date parse(String string) {
        try {
            if (string == null) return null;
            return getFormat().parse(string);
        } catch (Exception e) {
            LogMF.warn( l4j, "Could not parse date {0}: {1}", string, e.getMessage() );
            return null;
        }
    }

    public static String format(Date date) {
        if (date == null) return null;
        return getFormat().format(date);
    }

    private static DateFormat getFormat() {
        DateFormat format = iso8601Format.get();
        if (format == null) {
            format = new SimpleDateFormat(ISO_8601_DATE_Z);
            format.setTimeZone( TimeZone.getTimeZone( "UTC" ));
            iso8601Format.set(format);
        }
        return format;
    }

    private Iso8601Util() {}
}
