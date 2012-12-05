package com.emc.atmos.api.request;

import com.emc.atmos.api.RestUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class ListRequest<T extends ListRequest<T>> extends Request {
    protected int limit;
    protected String token;

    protected abstract T me();

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new TreeMap<String, List<Object>>();

        RestUtil.addValue( headers, RestUtil.XHEADER_UTF8, "true" );

        if ( limit > 0 ) RestUtil.addValue( headers, RestUtil.XHEADER_LIMIT, limit );

        if ( token != null ) RestUtil.addValue( headers, RestUtil.XHEADER_TOKEN, token );

        return headers;
    }

    public T limit( int limit ) {
        setLimit( limit );
        return me();
    }

    public T token( String token ) {
        setToken( token );
        return me();
    }

    public int getLimit() {
        return limit;
    }

    public String getToken() {
        return token;
    }

    public void setLimit( int limit ) {
        this.limit = limit;
    }

    public void setToken( String token ) {
        this.token = token;
    }
}
