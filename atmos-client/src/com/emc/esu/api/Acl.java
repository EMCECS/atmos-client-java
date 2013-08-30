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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An Access Control List (ACL) is a collection of Grants that assign privileges
 * to users and/or groups.
 */
public class Acl implements Iterable<Grant> {
    private Set<Grant> grants;

    /**
     * Creates a new access control list
     */
    public Acl() {
        this.grants = new HashSet<Grant>();
    }
    
    
    /**
     * Adds a grant to the access control list
     * @param g the grant to add.
     */
    public void addGrant( Grant g ) {
        grants.add( g );
    }
    
    /**
     * Removes a grant from the access control list.
     * @param g the grant to remove
     */
    public void removeGrant( Grant g ) {
        grants.remove( g );
    }
    
    public int count() {
        return grants.size();
    }

    /**
     * Returns an iterator over this ACL's grant objects.
     */
    public Iterator<Grant> iterator() {
        return grants.iterator();
    }
    
    /**
     * Clears all the grants in the ACL.
     */
    public void clear() {
        grants.clear();
    }
    
    /**
     * Returns true if the ACLs are equal.  This is done by ensuring they
     * have the same number of grants and each ACL contains the same
     * set of grants.
     */
    public boolean equals( Object obj ) {
        if( !( obj instanceof Acl ) ) {
            return false;
        }
        
        Acl acl2 = (Acl)obj;

        return this.grants.equals( acl2.grants );
    }
    
    /**
     * Returns the ACL's grant set as a String.
     */
    public String toString() {
        return grants.toString();
    }

    /**
     * Returns true if this ACL contains the specified Grant.
     * @param g the grant to check for
     * @return true if this ACL contains the grant.
     */
    public boolean contains(Grant g) {
        return grants.contains( g );
    }
    
}
