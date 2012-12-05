package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( propOrder = {"enabled", "endAt"} )
public class PolicyEvent {
    private boolean enabled;
    private String endAt;

    public PolicyEvent() {
        enabled = false;
        endAt = "";
    }

    @XmlElement( name = "enabled" )
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }

    @XmlElement( name = "endAt" )
    public String getEndAt() {
        return endAt;
    }

    public void setEndAt( String endAt ) {
        this.endAt = endAt;
    }
}
