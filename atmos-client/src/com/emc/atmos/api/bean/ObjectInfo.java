// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.api.bean;

import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.bean.adapter.ObjectIdAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "GetObjectInfoResponse")
@XmlType(propOrder = {"objectId", "selection", "numReplicas", "replicas", "retention", "expiration"})
public class ObjectInfo {
    private ObjectId objectId;
    private String selection;
    private int numReplicas;
    private List<Replica> replicas;
    private PolicyEvent retention;
    private PolicyEvent expiration;

    @XmlElement( name = "expiration" )
    public PolicyEvent getExpiration() {
        return expiration;
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
    public PolicyEvent getRetention() {
        return retention;
    }

    @XmlElement( name = "selection" )
    public String getSelection() {
        return selection;
    }

    @XmlTransient
    public Date getRetainedUntil() {
        if ( retention == null ) return null;
        return retention.getEndAt();
    }

    @XmlTransient
    public Date getExpiresAt() {
        if ( expiration == null ) return null;
        return expiration.getEndAt();
    }

    public void setExpiration( PolicyEvent expiration ) {
        this.expiration = expiration;
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

    public void setRetention( PolicyEvent retention ) {
        this.retention = retention;
    }

    public void setSelection( String selection ) {
        this.selection = selection;
    }
}
