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
 * Used to grant a permission to a grantee (a user or group)
 */
public class Grant {
    // Developer Note
    // --------------
    // Grants are immutable because changing their values will change their
    // hashcode.  If the hashcode is changed, the Acl's Set of Grant objects
    // will likely break it's contains() method because the HashSet searches
    // for objects by first attempting to locate their bucket by hashcode.  If
    // the hashcode changes, the bucket will likely change and therefore the
    // hashset will look in the wrong bucket when calling contains().  The
    // result of breaking contains() is that Acl equals() will also break.

    
    private Grantee grantee;
    private String permission;
    

    /**
     * Creates a new grant
     * @param grantee the recipient of the permission
     * @param permission the rights to grant to the grantee.  Use
     * the constants in the Permission class.
     */
    public Grant( Grantee grantee, String permission ) { 
        this.grantee = grantee;
        this.permission = permission;
    }
    
    /**
     * Gets the recipient of the grant
     * @return the grantee
     */
    public Grantee getGrantee() {
        return grantee;
    }
    
    /**
     * Gets the rights assigned the grantee
     * @return the permissions assigned
     */
    public String getPermission() {
        return permission;
    }
        
    /**
     * Returns the grant in string form: grantee=permission
     */
    public String toString() {
        return grantee.getName() + "=" + permission;
    }
    
    /**
     * Checks to see if grants are equal.  This is true if the grantee and
     * permission are equal.
     */
    public boolean equals( Object obj ) {
        if( !(obj instanceof Grant ) ) {
            return false;
        }
        Grant g = (Grant) obj;
        return g.permission.equals(permission) && g.grantee.equals( grantee );
    }
    
    /**
     * Returns a hash code for the Grant.
     */
    public int hashCode() {
        return toString().hashCode();
    }
}
