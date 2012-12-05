package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;

import java.util.Map;

public class ObjectMetadata {
    private Map<String, Metadata> metadata;
    private Acl acl;
    private String contentType;

    public ObjectMetadata() {
    }

    public ObjectMetadata( Map<String, Metadata> metadata, Acl acl, String contentType ) {
        this.metadata = metadata;
        this.acl = acl;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl( Acl acl ) {
        this.acl = acl;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata( Map<String, Metadata> metadata ) {
        this.metadata = metadata;
    }
}
