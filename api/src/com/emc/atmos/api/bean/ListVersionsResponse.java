package com.emc.atmos.api.bean;

import com.emc.atmos.api.ObjectId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement( name = "ListVersionsResponse" )
public class ListVersionsResponse extends BasicResponse {
    private List<ObjectVersion> versions;

    @XmlElement( name = "Ver" )
    public List<ObjectVersion> getVersions() {
        return versions;
    }

    public void setVersions( List<ObjectVersion> versions ) {
        this.versions = versions;
    }

    @XmlTransient
    public List<ObjectId> getVersionIds() {
        List<ObjectId> vIds = new ArrayList<ObjectId>();
        for ( ObjectVersion version : versions ) {
            vIds.add( version.getVersionId() );
        }
        return vIds;
    }
}
