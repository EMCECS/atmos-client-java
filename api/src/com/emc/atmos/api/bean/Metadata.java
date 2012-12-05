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
