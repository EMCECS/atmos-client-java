package com.emc.atmos.api.bean.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Iso8601Adapter extends XmlAdapter<String, Date> {
    private static final DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );

    static {
        formatter.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    /**
     * Until Java 7, SimpleDateFormat doesn't support ISO 8601 time zones ('Z', '+0000', '-03', etc.)  This extra
     * parsing ensures that we can *read* them.
     */
    @Override
    public Date unmarshal( String s ) throws Exception {
        int hourOffset = 0, minuteOffset = 0;

        String tzPattern = "([-+])(\\d{2}):?(\\d{2})?$";

        Matcher matcher = Pattern.compile( tzPattern ).matcher( s );
        if ( matcher.find() ) {

            hourOffset = Integer.parseInt( matcher.group( 2 ) );

            if ( matcher.group( 3 ) != null ) minuteOffset = Integer.parseInt( matcher.group( 3 ) );

            // formatter reads as GMT, so reverse the offset to get the real GMT time
            if ( "+".equals( matcher.group( 1 ) ) ) {
                hourOffset *= -1;
                minuteOffset *= -1;
            }

            s = s.replaceAll( tzPattern, "" ) + "Z";
        }

        Calendar cal = Calendar.getInstance();

        cal.setTime( formatter.parse( s ) );

        cal.add( Calendar.HOUR_OF_DAY, hourOffset );
        cal.add( Calendar.MINUTE, minuteOffset );

        return cal.getTime();
    }

    /**
     * We will always write in UTC with no offset, so no need for extra logic here.
     */
    @Override
    public String marshal( Date date ) throws Exception {
        return formatter.format( date );
    }
}
