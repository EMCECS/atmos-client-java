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
package com.emc.esu.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The MetadataTags class contains a collection of metadata tags.
 */
public class MetadataTags implements Iterable<MetadataTag> {
    private Map<String,MetadataTag> tags = new HashMap<String,MetadataTag>();
    
    /**
     * Adds a tag to the set of tags
     * @param tag the tag to add
     */
    public void addTag( MetadataTag tag ) {
        tags.put( tag.getName(), tag );
    }
    
    /**
     * Removes a tag from the set of tags
     * @param tag the tag to remove
     */
    public void removeTag( MetadataTag tag ) {
        tags.remove( tag.getName() );
    }
    
    /**
     * Gets a tag from the set with the given name
     * @param name the name to search for.
     * @return the tag or null if this set does not contain a tag with the
     * given name.
     */
    public MetadataTag getTag( String name ) {
        return tags.get( name );
    }
    
    /**
     * Returns true if this set contains a tag with the given name.
     * @param name the name to search for
     * @return true if this set contains a tag with the given name.
     */
    public boolean contains( String name ) {
        return tags.containsKey( name );
    }
    
    /**
     * Return true if this set contains the given tag object.
     * @param tag the tag to search for
     * @return true if this set contains the given tag
     */
    public boolean contains( MetadataTag tag ) {
        return tags.containsValue( tag );
    }

    /**
     * Returns an iterator that iterates over the set of tags.
     */
    public Iterator<MetadataTag> iterator() {
        return tags.values().iterator();
    }
    
    /**
     * Returns the number of tags in this set
     * @return the tag count
     */
    public int count() {
        return tags.size();
    }
    
}
