package com.emc.atmos.api;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

public class ObjectId implements ObjectIdentifier {
    private String id;

    public ObjectId( String id ) {
        this.id = id;
    }

    @XmlValue
    public String getId() {
        return this.id;
    }

    @Override
    @XmlTransient
    public String getRelativeResourcePath() {
        return "objects/" + id;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ObjectId objectId = (ObjectId) o;

        if ( !id.equals( objectId.id ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
