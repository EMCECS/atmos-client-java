package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( namespace = "", name = "access-token" )
public class AccessToken extends AccessTokenPolicy {
    private String id;

    @XmlElement( namespace = "", name = "access-token-id" )
    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }
}
