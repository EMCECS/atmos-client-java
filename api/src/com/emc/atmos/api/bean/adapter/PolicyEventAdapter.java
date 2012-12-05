package com.emc.atmos.api.bean.adapter;

import com.emc.atmos.api.bean.PolicyEvent;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PolicyEventAdapter extends XmlAdapter<PolicyEvent, Date> {
    private static final DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );

    static {
        formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    @Override
    public Date unmarshal( PolicyEvent policyEvent ) throws Exception {
        if ( policyEvent.getEndAt() == null || policyEvent.getEndAt().length() == 0 ) return null;
        return formatter.parse( policyEvent.getEndAt() );
    }

    @Override
    public PolicyEvent marshal( Date date ) throws Exception {
        PolicyEvent event = new PolicyEvent();
        if ( date != null ) {
            event.setEnabled( true );
            event.setEndAt( formatter.format( date ) );
        }
        return event;
    }
}
