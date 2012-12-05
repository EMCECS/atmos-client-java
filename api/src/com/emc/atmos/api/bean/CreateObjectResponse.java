package com.emc.atmos.api.bean;

import com.emc.atmos.api.ChecksumValue;
import com.emc.atmos.api.ChecksumValueImpl;
import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.RestUtil;

public class CreateObjectResponse extends BasicResponse {
    public ObjectId getObjectId() {
        return RestUtil.parseObjectId( location );
    }

    public ChecksumValue getWsChecksum() {
        return new ChecksumValueImpl( getFirstHeader( RestUtil.XHEADER_WSCHECKSUM ) );
    }

    public ChecksumValue getServerGeneratedChecksum() {
        return new ChecksumValueImpl( getFirstHeader( RestUtil.XHEADER_CONTENT_CHECKSUM ) );
    }
}
