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
 * A grantee represents a user or group that recieves a permission grant.
 */
public class Grantee {
    public static enum GRANT_TYPE { USER, GROUP };
    
    /**
     * Static instance that represents the special group 'other'
     */
    public static final Grantee OTHER = new Grantee( "other", GRANT_TYPE.GROUP );
    
    private String name;
    private GRANT_TYPE type;
    
    /**
     * Creates a new grantee.
     * @param name the name of the user or group
     * @param type the type of grantee, e.g. USER or GROUP.  Use the enum in 
     * this class to specify the type of grantee
     */
    public Grantee( String name, GRANT_TYPE type ) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * Gets the grantee's name
     * @return the name of the grantee
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the grantee's type.  You can compare this value to the enum
     * @return the type of grantee.
     */
    public GRANT_TYPE getType() {
        return type;
    }
    
    /**
     * Checks to see if a Grantee is equal to another.  Returns true if both
     * the names and types are equal.
     */
    public boolean equals( Object obj ) {
        if( !( obj instanceof Grantee ) ) {
            return false;
        }
        
        Grantee g = (Grantee)obj;
        
        return g.getName().equals( name ) && g.getType() == type;
    }
    
    /**
     * Returns a hash code for the Grantee.
     */
    public int hashCode() {
        return toString().hashCode();
    }

}
