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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@XmlType( propOrder = {"objectId", "fileType", "filename", "systemMetadata", "userMetadata"} )
public class DirectoryEntry {
    private ObjectId objectId;
    private FileType fileType;
    private String filename;
    private List<Metadata> systemMetadata;
    private List<Metadata> userMetadata;

    @XmlElement( name = "ObjectID" )
    @XmlJavaTypeAdapter( ObjectIdAdapter.class )
    public ObjectId getObjectId() {
        return objectId;
    }

    @XmlElement( name = "Filename" )
    public String getFilename() {
        return filename;
    }

    @XmlElement( name = "FileType" )
    public FileType getFileType() {
        return fileType;
    }

    @XmlElementWrapper( name = "SystemMetadataList" )
    @XmlElement( name = "Metadata" )
    public List<Metadata> getSystemMetadata() {
        return systemMetadata;
    }

    @XmlTransient
    public Map<String, Metadata> getSystemMetadataMap() {
        if ( systemMetadata == null ) return null;
        Map<String, Metadata> metadataMap = new TreeMap<String, Metadata>();
        for ( Metadata metadata : systemMetadata ) {
            metadataMap.put( metadata.getName(), metadata );
        }
        return metadataMap;
    }

    @XmlElementWrapper( name = "UserMetadataList" )
    @XmlElement( name = "Metadata" )
    public List<Metadata> getUserMetadata() {
        return userMetadata;
    }

    @XmlTransient
    public Map<String, Metadata> getUserMetadataMap() {
        if ( userMetadata == null ) return null;
        Map<String, Metadata> metadataMap = new TreeMap<String, Metadata>();
        for ( Metadata metadata : userMetadata ) {
            metadataMap.put( metadata.getName(), metadata );
        }
        return metadataMap;
    }

    public void setObjectId( ObjectId objectId ) {
        this.objectId = objectId;
    }

    public void setFilename( String filename ) {
        this.filename = filename;
    }

    public void setFileType( FileType fileType ) {
        this.fileType = fileType;
    }

    public void setSystemMetadata( List<Metadata> systemMetadata ) {
        this.systemMetadata = systemMetadata;
    }

    public void setUserMetadata( List<Metadata> userMetadata ) {
        this.userMetadata = userMetadata;
    }

    @XmlTransient
    public boolean isDirectory() {
        return FileType.directory == this.fileType;
    }

    public static enum FileType {
        directory, regular
    }
}
