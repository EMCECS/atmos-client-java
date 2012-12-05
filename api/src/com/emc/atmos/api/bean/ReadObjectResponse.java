package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ChecksumValue;
import com.emc.atmos.api.ChecksumValueImpl;
import com.emc.atmos.api.RestUtil;

import java.util.Map;
import java.util.TreeMap;

public class ReadObjectResponse<T> extends BasicResponse {
    private T object;
    private ObjectMetadata metadata;

    public ReadObjectResponse() {
    }

    public ReadObjectResponse( T object ) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void setObject( T object ) {
        this.object = object;
    }

    public synchronized ObjectMetadata getMetadata() {
        if ( metadata == null ) {
            Acl acl = new Acl( RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_USER_ACL ) ),
                               RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_GROUP_ACL ) ) );

            Map<String, Metadata> metaMap = new TreeMap<String, Metadata>();
            metaMap.putAll( RestUtil.parseMetadataHeader( getFirstHeader( RestUtil.XHEADER_META ), false ) );
            metaMap.putAll( RestUtil.parseMetadataHeader( getFirstHeader( RestUtil.XHEADER_LISTABLE_META ), true ) );

            metadata = new ObjectMetadata( metaMap, acl, getContentType() );
        }
        return metadata;
    }

    public ChecksumValue getWsChecksum() {
        return new ChecksumValueImpl( getFirstHeader( RestUtil.XHEADER_WSCHECKSUM ) );
    }

    public ChecksumValue getServerGeneratedChecksum() {
        return new ChecksumValueImpl( getFirstHeader( RestUtil.XHEADER_CONTENT_CHECKSUM ) );
    }
}
