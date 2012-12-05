package com.emc.atmos.api.request;

import com.emc.atmos.api.Range;
import com.emc.atmos.api.RestUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReadObjectRequest extends ObjectRequest<ReadObjectRequest> {
    protected List<Range> ranges;

    @Override
    public String getServiceRelativePath() {
        return identifier.getRelativeResourcePath();
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new TreeMap<String, List<Object>>();

        RestUtil.addValue( headers, RestUtil.XHEADER_UTF8, "true" );

        if ( ranges != null )
            RestUtil.addValue( headers, RestUtil.HEADER_RANGE, "bytes=" + RestUtil.join( ranges, "," ) );

        return headers;
    }

    @Override
    protected ReadObjectRequest me() {
        return this;
    }

    public ReadObjectRequest ranges( Range... range ) {
        this.ranges = Arrays.asList( range );
        return this;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public void setRanges( List<Range> ranges ) {
        this.ranges = ranges;
    }
}
