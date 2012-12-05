package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType( propOrder = {"id", "type", "current", "location", "storageType"} )
public class Replica {
    private int id;
    private String type;
    private boolean current;
    private String location;
    private String storageType;

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent( boolean current ) {
        this.current = current;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation( String location ) {
        this.location = location;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType( String storageType ) {
        this.storageType = storageType;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }
}
