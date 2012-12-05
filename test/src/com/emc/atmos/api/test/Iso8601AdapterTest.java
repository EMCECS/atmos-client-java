package com.emc.atmos.api.test;

import com.emc.atmos.api.bean.adapter.Iso8601Adapter;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class Iso8601AdapterTest {
    private static Iso8601Adapter adapter = new Iso8601Adapter();

    @Test
    public void testNegativeOffset() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone( TimeZone.getTimeZone( "GMT-0600" ) );
        cal.clear();

        cal.set( 2012, Calendar.DECEMBER, 1, 5, 0, 0 );
        Assert.assertEquals( "2012-12-01T11:00:00Z", adapter.marshal( cal.getTime() ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T05:00:00-0600" ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T05:00:00-06" ) );
    }

    @Test
    public void testGmt() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        cal.clear();

        cal.set( 2012, Calendar.DECEMBER, 1, 5, 0, 0 );
        Assert.assertEquals( "2012-12-01T05:00:00Z", adapter.marshal( cal.getTime() ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T05:00:00Z" ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T05:00:00+00" ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T05:00:00+0000" ) );
    }

    @Test
    public void testPositiveOffset() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone( TimeZone.getTimeZone( "GMT+0600" ) );
        cal.clear();

        cal.set( 2012, Calendar.DECEMBER, 1, 10, 0, 0 );
        Assert.assertEquals( "2012-12-01T04:00:00Z", adapter.marshal( cal.getTime() ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T10:00:00+0600" ) );
        Assert.assertEquals( cal.getTime(), adapter.unmarshal( "2012-12-01T10:00:00+06" ) );
    }
}
