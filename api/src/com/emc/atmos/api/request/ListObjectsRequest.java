package com.emc.atmos.api.request;

import com.emc.atmos.api.RestUtil;
import com.emc.util.HttpUtil;

import java.util.List;
import java.util.Map;

public class ListObjectsRequest extends ListMetadataRequest<ListObjectsRequest> {
    private String metadataName;

    @Override
    public String getServiceRelativePath() {
        return "objects";
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = super.generateHeaders();

        RestUtil.addValue( headers, RestUtil.XHEADER_TAGS, HttpUtil.encodeUtf8( metadataName ) );

        return headers;
    }

    @Override
    protected ListObjectsRequest me() {
        return this;
    }

    public ListObjectsRequest metadataName( String metadataName ) {
        setMetadataName( metadataName );
        return this;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public void setMetadataName( String metadataName ) {
        this.metadataName = metadataName;
    }
}
