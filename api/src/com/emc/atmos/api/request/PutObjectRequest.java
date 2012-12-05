package com.emc.atmos.api.request;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ChecksumValue;
import com.emc.atmos.api.RestUtil;
import com.emc.atmos.api.bean.Metadata;

import java.util.*;

public abstract class PutObjectRequest<T extends PutObjectRequest<T>> extends ObjectRequest<T>
        implements ContentRequest {
    protected Object content;
    protected long contentLength;
    protected String contentType;
    protected Acl acl;
    protected Map<String, Metadata> userMetadata;
    protected ChecksumValue wsChecksum;

    public PutObjectRequest() {
        userMetadata = new TreeMap<String, Metadata>();
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new TreeMap<String, List<Object>>();

        // enable UTF-8
        if ( !getUserMetadata().isEmpty() ) RestUtil.addValue( headers, RestUtil.XHEADER_UTF8, "true" );

        // metadata
        for ( Metadata metadata : getUserMetadata() ) {
            if ( metadata.isListable() )
                RestUtil.addValue( headers, RestUtil.XHEADER_LISTABLE_META, metadata.toASCIIString() );
            else
                RestUtil.addValue( headers, RestUtil.XHEADER_META, metadata.toASCIIString() );
        }

        // acl
        if ( acl != null ) {
            headers.put( RestUtil.XHEADER_USER_ACL, acl.getUserAclHeader() );
            headers.put( RestUtil.XHEADER_GROUP_ACL, acl.getGroupAclHeader() );
        }

        // wschecksum
        if ( wsChecksum != null ) {
            RestUtil.addValue( headers, RestUtil.XHEADER_WSCHECKSUM, wsChecksum );
        }

        return headers;
    }

    public T content( Object content ) {
        setContent( content );
        return me();
    }

    public T contentLength( long contentLength ) {
        setContentLength( contentLength );
        return me();
    }

    public T contentType( String contentType ) {
        setContentType( contentType );
        return me();
    }

    public T acl( Acl acl ) {
        setAcl( acl );
        return me();
    }

    public T userMetadata( Metadata... userMetadata ) {
        if ( userMetadata == null ) userMetadata = new Metadata[0];
        setUserMetadata( Arrays.asList( userMetadata ) );
        return me();
    }

    public T wsChecksum( ChecksumValue wsChecksum ) {
        setWsChecksum( wsChecksum );
        return me();
    }

    public Object getContent() {
        return content;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public Acl getAcl() {
        return acl;
    }

    public ChecksumValue getWsChecksum() {
        return wsChecksum;
    }

    public Set<Metadata> getUserMetadata() {
        return new HashSet<Metadata>( userMetadata.values() );
    }

    public void setContent( Object content ) {
        this.content = content;
    }

    public void setContentLength( long contentLength ) {
        this.contentLength = contentLength;
    }

    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    public void setAcl( Acl acl ) {
        this.acl = acl;
    }

    public void setUserMetadata( Collection<Metadata> userMetadata ) {
        this.userMetadata.clear();
        for ( Metadata metadata : userMetadata ) {
            this.userMetadata.put( metadata.getName(), metadata );
        }
    }

    public void setWsChecksum( ChecksumValue wsChecksum ) {
        this.wsChecksum = wsChecksum;
    }
}
