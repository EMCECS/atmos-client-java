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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains a list of metadata items
 */
public class MetadataList implements Iterable<Metadata> {
    private Map<String,Metadata> meta = new HashMap<String,Metadata>();

    /**
     * Returns an iterator that iterates over the set of metadata items in
     * the list.
     */
    public Iterator<Metadata> iterator() {
        return meta.values().iterator();
    }
    
    /**
     * Adds a metadata item to the list
     * @param m metadata to add
     */
    public void addMetadata( Metadata m ) {
        meta.put( m.getName(), m );
    }
    
    /**
     * Removes a metadata item from the list
     * @param m metadata to remove
     */
    public void removeMetadata( Metadata m ) {
        meta.remove( m.getName() );
    }
    
    /**
     * Returns the metadata item with the specified name
     * @param name name to search for
     * @return the metadata with the given name or null if the list does not
     * contain metadata with the requested name.
     */
    public Metadata getMetadata( String name ) {
        return meta.get( name );
    }
    
    /**
     * Returns the number of items in the metadata list.
     * @return the item count
     */
    public int count() {
        return meta.size();
    }

}
