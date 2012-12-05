package com.emc.atmos.api.bean;

import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.bean.adapter.ObjectIdAdapter;
import com.emc.atmos.api.bean.adapter.PolicyEventAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

@XmlRootElement( name = "GetObjectInfoResponse" )
@XmlType( propOrder = {"objectId", "selection", "numReplicas", "replicas", "retainedUntil", "expiresAt"} )
public class ObjectInfo {
    private ObjectId objectId;
    private String selection;
    private int numReplicas;
    private List<Replica> replicas;
    private Date retainedUntil;
    private Date expiresAt;

    @XmlElement( name = "expiration" )
    @XmlJavaTypeAdapter( PolicyEventAdapter.class )
    public Date getExpiresAt() {
        return expiresAt;
    }

    @XmlElement( name = "numReplicas" )
    public int getNumReplicas() {
        return numReplicas;
    }

    @XmlElement( name = "objectId" )
    @XmlJavaTypeAdapter( ObjectIdAdapter.class )
    public ObjectId getObjectId() {
        return objectId;
    }

    @XmlElementWrapper( name = "replicas" )
    @XmlElement( name = "replica" )
    public List<Replica> getReplicas() {
        return replicas;
    }

    @XmlElement( name = "retention" )
    @XmlJavaTypeAdapter( PolicyEventAdapter.class )
    public Date getRetainedUntil() {
        return retainedUntil;
    }

    @XmlElement( name = "selection" )
    public String getSelection() {
        return selection;
    }

    public void setExpiresAt( Date expiresAt ) {
        this.expiresAt = expiresAt;
    }

    public void setNumReplicas( int numReplicas ) {
        this.numReplicas = numReplicas;
    }

    public void setObjectId( ObjectId objectId ) {
        this.objectId = objectId;
    }

    public void setReplicas( List<Replica> replicas ) {
        this.replicas = replicas;
    }

    public void setRetainedUntil( Date retainedUntil ) {
        this.retainedUntil = retainedUntil;
    }

    public void setSelection( String selection ) {
        this.selection = selection;
    }
}
