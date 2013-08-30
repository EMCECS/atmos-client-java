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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains information from the GetServiceInformation call
 *
 * @author jason
 */
@XmlRootElement( name = "Service" )
public class ServiceInformation {
    private Version version;
    private Set<Feature> features;

    public ServiceInformation() {
        features = new HashSet<Feature>();
    }

    /**
     * @return the atmosVersion
     */
    @XmlTransient
    public String getAtmosVersion() {
        if ( version == null ) return null;
        return version.getAtmos();
    }

    @XmlElement( name = "Version" )
    public Version getVersion() {
        return version;
    }

    public void setVersion( Version version ) {
        this.version = version;
    }

    /**
     * Adds a feature to the list of supported features.
     */
    public void addFeature( Feature feature ) {
        features.add( feature );
    }

    public void addFeatureFromHeaderName( String headerName ) {
        features.add( Feature.fromHeaderName( headerName ) );
    }

    /**
     * Checks to see if a feature is supported.
     */
    public boolean hasFeature( Feature feature ) {
        return features.contains( feature );
    }

    /**
     * Gets the features advertised by the service
     */
    @XmlTransient
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet( features );
    }

    private static class Version {
        private String atmos;

        @XmlElement( name = "Atmos" )
        public String getAtmos() {
            return atmos;
        }

        public void setAtmos( String atmos ) {
            this.atmos = atmos;
        }
    }

    @XmlTransient
    public static enum Feature {
        Object( "object" ),
        Namespace( "namespace" ),
        Utf8( "utf-8" ),
        BrowserCompat( "browser-compat" ),
        KeyValue( "key-value" ),
        Hardlink( "hardlink" ),
        Query( "query" ),
        Versioning( "versioning" );

        public static Feature fromHeaderName( String headerName ) {
            for ( Feature feature : values() )
                if ( feature.getHeaderName().equals( headerName ) ) return feature;
            return null;
        }

        private String headerName;

        private Feature( String headerName ) {
            this.headerName = headerName;
        }

        public String getHeaderName() {
            return headerName;
        }
    }
}
