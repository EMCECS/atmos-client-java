package com.emc.atmos.api.bean;

import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.bean.adapter.ObjectIdAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlType( propOrder = {"versionNumber", "versionId", "itime"} )
public class ObjectVersion {
    private int versionNumber;
    private ObjectId versionId;
    private Date itime;

    public ObjectVersion() {
    }

    public ObjectVersion( int versionNumber, ObjectId versionId, Date iTime ) {
        this.versionNumber = versionNumber;
        this.versionId = versionId;
        this.itime = iTime;
    }

    @XmlElement( name = "itime" )
    public Date getItime() {
        return itime;
    }

    public void setItime( Date itime ) {
        this.itime = itime;
    }

    @XmlElement( name = "OID" )
    @XmlJavaTypeAdapter( ObjectIdAdapter.class )
    public ObjectId getVersionId() {
        return versionId;
    }

    public void setVersionId( ObjectId versionId ) {
        this.versionId = versionId;
    }

    @XmlElement( name = "VerNum" )
    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber( int versionNumber ) {
        this.versionNumber = versionNumber;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ObjectVersion that = (ObjectVersion) o;

        if ( versionNumber != that.versionNumber ) return false;
        if ( !versionId.equals( that.versionId ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = versionNumber;
        result = 31 * result + versionId.hashCode();
        return result;
    }
}
