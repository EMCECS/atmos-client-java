/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
