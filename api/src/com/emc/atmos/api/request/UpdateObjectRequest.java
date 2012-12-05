package com.emc.atmos.api.request;

import com.emc.atmos.api.Range;
import com.emc.atmos.api.RestUtil;

import java.util.List;
import java.util.Map;

public class UpdateObjectRequest extends PutObjectRequest<UpdateObjectRequest> {
    protected Range range;

    @Override
    public String getServiceRelativePath() {
        return identifier.getRelativeResourcePath();
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = super.generateHeaders();

        if ( range != null )
            RestUtil.addValue( headers, RestUtil.HEADER_RANGE, "bytes=" + range );

        return headers;
    }

    @Override
    protected UpdateObjectRequest me() {
        return this;
    }

    public UpdateObjectRequest range( Range range ) {
        setRange( range );
        return this;
    }

    public Range getRange() {
        return range;
    }

    public void setRange( Range range ) {
        this.range = range;
    }
}
