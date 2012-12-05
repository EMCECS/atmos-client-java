package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement( name = "ListObjectsResponse" )
public class ListObjectsResponse extends BasicResponse {
    private List<ObjectEntry> entries;

    @XmlElement( name = "Object" )
    public List<ObjectEntry> getEntries() {
        return this.entries;
    }

    public void setEntries( List<ObjectEntry> entries ) {
        this.entries = entries;
    }
}
