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
