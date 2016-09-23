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

import com.emc.util.HttpUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( propOrder = {"name", "value", "listable"} )
public class Metadata {
    private String name;
    private String value;
    private boolean listable;

    public Metadata() {
    }

    public Metadata( String name, String value, boolean listable ) {
        this.name = name;
        this.value = value;
        this.listable = listable;
    }

    @XmlElement( name = "Name" )
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @XmlElement( name = "Value" )
    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    @XmlElement( name = "Listable" )
    public boolean isListable() {
        return listable;
    }

    public void setListable( boolean listable ) {
        this.listable = listable;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Metadata metadata = (Metadata) o;

        if ( !name.equals( metadata.name ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        String value = (this.value == null) ? "" : this.value;
        return name + "=" + value;
    }

    public String toASCIIString() {
        String value = (this.value == null) ? "" : this.value;
        return HttpUtil.encodeUtf8( name ) + "=" + HttpUtil.encodeUtf8( value );
    }
}
