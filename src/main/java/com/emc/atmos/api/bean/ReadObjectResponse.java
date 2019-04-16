/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2013-2018, Dell EMC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ChecksumValue;
import com.emc.atmos.api.ChecksumValueImpl;
import com.emc.atmos.api.RestUtil;
import com.emc.util.BasicResponse;

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

    public ObjectMetadata getMetadata() {
        return getMetadata( true );
    }

    public synchronized ObjectMetadata getMetadata( boolean decodeUtf8 ) {
        if ( metadata == null ) {
            Acl acl = new Acl( RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_USER_ACL ) ),
                               RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_GROUP_ACL ) ) );

            Map<String, Metadata> metaMap = new TreeMap<String, Metadata>();
            metaMap.putAll( RestUtil.parseMetadataHeader( getFirstHeader( RestUtil.XHEADER_META ),
                    false, decodeUtf8 ) );
            metaMap.putAll( RestUtil.parseMetadataHeader( getFirstHeader( RestUtil.XHEADER_LISTABLE_META ),
                    true, decodeUtf8 ) );

            String wsChecksumHeader = getFirstHeader( RestUtil.XHEADER_WSCHECKSUM );
            ChecksumValue wsChecksum = wsChecksumHeader == null ? null : new ChecksumValueImpl( wsChecksumHeader );
            String serverChecksumHeader = getFirstHeader( RestUtil.XHEADER_CONTENT_CHECKSUM );
            ChecksumValue serverChecksum = serverChecksumHeader == null ? null :
                                           new ChecksumValueImpl( getFirstHeader( RestUtil.XHEADER_CONTENT_CHECKSUM ) );
            String retentionPeriod = getFirstHeader( RestUtil.XHEADER_RETENTION_PERIOD );

            metadata = new ObjectMetadata( metaMap, acl, getContentType(), wsChecksum, serverChecksum );
            if ( retentionPeriod != null ) metadata.setRetentionPeriod( Long.parseLong( retentionPeriod ) );
            metadata.setRetentionPolicy( getFirstHeader( RestUtil.XHEADER_RETENTION_POLICY ) );

            metadata.setETag( getETag() );
        }
        return metadata;
    }

    public ChecksumValue getWsChecksum() {
        return getMetadata().getWsChecksum();
    }

    public ChecksumValue getServerGeneratedChecksum() {
        return getMetadata().getServerChecksum();
    }
}
