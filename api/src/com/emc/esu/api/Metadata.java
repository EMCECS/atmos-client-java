// Copyright (c) 2008, EMC Corporation.
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
package com.emc.esu.api;

/**
 * Encapsulates a piece of object metadata
 */
public class Metadata {
    private String name;
    private String value;
    private boolean listable;
    
    /**
     * Creates a new piece of metadata
     * @param name the name of the metadata (e.g. 'Title')
     * @param value the metadata value (e.g. 'Hamlet')
     * @param listable whether to make the value listable.  You can
     * query objects with a specific listable metadata tag using the listObjects
     * method in the API.
     */
    public Metadata( String name, String value, boolean listable ) {
            this.name = name;
            this.value = value;
            this.listable = listable;
    }
    
    /**
     * Returns a string representation of the metadata.
     * @return the metadata object in the format name=value.  Listable
     * metadata will appear as name(listable)=value
     */
    public String toString() {
            return name + (listable?"(listable)":"") + "=" + value;
    }
    
    /**
     * Returns the name of the metadata object
     */
    public String getName() {
            return name;
    }
    
    /**
     * Returns the metadata object's value
     */
    public String getValue() {
            return value;
    }
    
    /**
     * Sets the metadata's value.  Use updateObject to change this value on
     * the server.
     */
    public void setValue( String value ) {
            this.value = value;  
    }
    
    /**
     * Returns true if this metadata object is listable
     */
    public boolean isListable() {
            return listable;
    }
    
    /**
     * Sets the value of the listable flag.
     * @param listable whether this metadata object is listable.
     */
    public void setListable( boolean listable ) {
            this.listable = listable;
    }
}
