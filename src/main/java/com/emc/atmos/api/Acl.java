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
package com.emc.atmos.api;

import com.emc.atmos.api.bean.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Acl {
    public static final String GROUP_OTHER = "other";

    private Map<String, Permission> userAcl;
    private Map<String, Permission> groupAcl;

    public Acl() {
        this.userAcl = new TreeMap<String, Permission>();
        this.groupAcl = new TreeMap<String, Permission>();
    }

    public Acl( Map<String, Permission> userAcl, Map<String, Permission> groupAcl ) {
        this.userAcl = userAcl;
        this.groupAcl = groupAcl;
    }

    public Map<String, Permission> getGroupAcl() {
        return groupAcl;
    }

    public List<Object> getGroupAclHeader() {
        List<Object> values = new ArrayList<Object>();

        // empty ACL still needs to be set
        if ( groupAcl.isEmpty() ) values.add( "" );

        for ( String name : groupAcl.keySet() ) {
            values.add( name + "=" + groupAcl.get( name ) );
        }

        return values;
    }

    public void setGroupAcl( Map<String, Permission> groupAcl ) {
        this.groupAcl = groupAcl;
    }

    public Map<String, Permission> getUserAcl() {
        return userAcl;
    }

    public List<Object> getUserAclHeader() {
        List<Object> values = new ArrayList<Object>();

        // empty ACL still needs to be set
        if ( userAcl.isEmpty() ) values.add( "" );

        for ( String name : userAcl.keySet() ) {
            values.add( name + "=" + userAcl.get( name ) );
        }

        return values;
    }

    public void setUserAcl( Map<String, Permission> userAcl ) {
        this.userAcl = userAcl;
    }

    public Acl addUserGrant( String name, Permission permission ) {
        this.userAcl.put( name, permission );
        return this;
    }

    public Acl removeUserGrant( String name ) {
        this.userAcl.remove( name );
        return this;
    }

    public Acl addGroupGrant( String name, Permission permission ) {
        this.groupAcl.put( name, permission );
        return this;
    }

    public Acl removeGroupGrant( String name ) {
        this.groupAcl.remove( name );
        return this;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Acl acl = (Acl) o;

        if ( !groupAcl.equals( acl.groupAcl ) ) return false;
        if ( !userAcl.equals( acl.userAcl ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userAcl.hashCode();
        result = 31 * result + groupAcl.hashCode();
        return result;
    }
}
