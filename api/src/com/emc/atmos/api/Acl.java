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
