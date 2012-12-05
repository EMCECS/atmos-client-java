package com.emc.atmos.api.request;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.ObjectPath;
import com.emc.atmos.api.RestUtil;
import com.emc.atmos.api.bean.AccessTokenPolicy;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CreateAccessTokenRequest extends ObjectRequest<CreateAccessTokenRequest> implements ContentRequest {
    protected Acl acl;
    protected AccessTokenPolicy policy;

    @Override
    public String getServiceRelativePath() {
        return "accesstokens";
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new TreeMap<String, List<Object>>();

        // target object
        if ( identifier != null ) {
            if ( identifier instanceof ObjectId )
                RestUtil.addValue( headers, RestUtil.XHEADER_OBJECTID, identifier );
            else if ( identifier instanceof ObjectPath )
                RestUtil.addValue( headers, RestUtil.XHEADER_PATH, identifier );
            else
                throw new UnsupportedOperationException(
                        "Only object ID and path are currently supported in access tokens" );
        }

        // acl (applied to uploads)
        if ( acl != null ) {
            headers.put( RestUtil.XHEADER_USER_ACL, acl.getUserAclHeader() );
            headers.put( RestUtil.XHEADER_GROUP_ACL, acl.getGroupAclHeader() );
        }

        return headers;
    }

    @Override
    protected CreateAccessTokenRequest me() {
        return this;
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public Object getContent() {
        return policy;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    public CreateAccessTokenRequest acl( Acl acl ) {
        this.acl = acl;
        return this;
    }

    public CreateAccessTokenRequest policy( AccessTokenPolicy policy ) {
        this.policy = policy;
        return this;
    }

    public Acl getAcl() {
        return acl;
    }

    public AccessTokenPolicy getPolicy() {
        return policy;
    }

    public void setAcl( Acl acl ) {
        this.acl = acl;
    }

    public void setPolicy( AccessTokenPolicy policy ) {
        this.policy = policy;
    }
}
