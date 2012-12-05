package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.RestUtil;

public class GetAccessTokenResponse extends BasicResponse {
    AccessToken token;
    Acl acl;

    public GetAccessTokenResponse() {
    }

    public GetAccessTokenResponse( AccessToken token ) {
        this.token = token;
    }

    public AccessToken getToken() {
        return token;
    }

    public void setToken( AccessToken token ) {
        this.token = token;
    }

    public synchronized Acl getAcl() {
        if ( acl == null ) {
            acl = new Acl( RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_USER_ACL ) ),
                           RestUtil.parseAclHeader( getFirstHeader( RestUtil.XHEADER_GROUP_ACL ) ) );
        }
        return acl;
    }
}
