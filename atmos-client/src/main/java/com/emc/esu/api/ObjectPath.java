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

/**
 * Represents an object that is identified by its path in the 
 * filesystem namespace.  If the object path ends in a "/", it
 * is considered a directory object.  If it does not, it is a 
 * regular object.
 */
public class ObjectPath implements Identifier {

    /**
     * Stores the string representation of the identifier
     */
    private String path;

    /**
     * Constructs a new object identifier
     * @param path the object ID as a string
     */
    public ObjectPath( String path ) {
        this.path = path;
    }
    
    /**
     * Returns the identifier as a string
     * @return the identifier as a string
     */
    public String toString() {
        return path;
    }
    
    /**
     * Returns true if the object IDs are equal.
     */
    public boolean equals( Object obj ) {
        if( !(obj instanceof ObjectPath) ) {
            return false;
        }
        
        return path.equals( ((ObjectPath)obj).toString() );
        
    }
    
    /**
     * Gets the name of the object (the last path component)
     */
    public String getName() {
    	if( isDirectory() ) {
    		if( path.equals( "/" ) ) {
    			return "";
    		} else {
    			int slash = path.substring(0, path.length()-1 ).lastIndexOf( '/' );
    			return path.substring( slash+1, path.length()-1 );
    		}
    	} else {
    		int slash = path.lastIndexOf( '/' );
    		return path.substring( slash+1, path.length() );
    	}
    }
    
    /**
     * Returns a hash code for this object id.
     */
    public int hashCode() {
        return path.hashCode();
    }
    
    /**
     * Returns true if this path represents a directory object.
     */
    public boolean isDirectory() {
    	return path.endsWith( "/" );
    }
}
