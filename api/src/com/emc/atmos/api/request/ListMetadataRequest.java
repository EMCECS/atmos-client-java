package com.emc.atmos.api.request;

import com.emc.atmos.api.RestUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class ListMetadataRequest<T extends ListMetadataRequest<T>> extends ListRequest<T> {
    protected List<String> userMetadataNames;
    protected List<String> systemMetadataNames;
    protected boolean includeMetadata;

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = super.generateHeaders();

        if ( includeMetadata ) {
            RestUtil.addValue( headers, RestUtil.XHEADER_INCLUDE_META, 1 );
            if ( userMetadataNames != null )
                for ( String name : userMetadataNames ) RestUtil.addValue( headers, RestUtil.XHEADER_USER_TAGS, name );
            if ( systemMetadataNames != null )
                for ( String name : systemMetadataNames )
                    RestUtil.addValue( headers, RestUtil.XHEADER_SYSTEM_TAGS, name );
        }

        return headers;
    }

    public T userMetadataNames( String... userMetadataNames ) {
        setUserMetadataNames( Arrays.asList( userMetadataNames ) );
        return me();
    }

    public T systemMetadataNames( String... systemMetadataNames ) {
        setSystemMetadataNames( Arrays.asList( systemMetadataNames ) );
        return me();
    }

    public T includeMetadata( boolean includeMetadata ) {
        setIncludeMetadata( includeMetadata );
        return me();
    }

    public List<String> getUserMetadataNames() {
        return userMetadataNames;
    }

    public List<String> getSystemMetadataNames() {
        return systemMetadataNames;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setUserMetadataNames( List<String> userMetadataNames ) {
        this.userMetadataNames = userMetadataNames;
    }

    public void setSystemMetadataNames( List<String> systemMetadataNames ) {
        this.systemMetadataNames = systemMetadataNames;
    }

    public void setIncludeMetadata( boolean includeMetadata ) {
        this.includeMetadata = includeMetadata;
    }
}
