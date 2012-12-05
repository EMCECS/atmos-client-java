package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement( name = "ListDirectoryResponse" )
public class ListDirectoryResponse extends BasicResponse {
    private List<DirectoryEntry> entries;

    @XmlElementWrapper( name = "DirectoryList" )
    @XmlElement( name = "DirectoryEntry" )
    public List<DirectoryEntry> getEntries() {
        return this.entries;
    }

    public void setEntries( List<DirectoryEntry> entries ) {
        this.entries = entries;
    }
}
