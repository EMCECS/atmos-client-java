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
package com.emc.atmos.api.request;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.ObjectPath;
import com.emc.atmos.api.RestUtil;
import com.emc.atmos.api.bean.AccessTokenPolicy;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a request to create an anonymous access token.
 */
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
        Map<String, List<Object>> headers = super.generateHeaders();

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

    /**
     * Builder method for {@link #setAcl(com.emc.atmos.api.Acl)}
     */
    public CreateAccessTokenRequest acl( Acl acl ) {
        this.acl = acl;
        return this;
    }

    /**
     * Builder method for {@link #setPolicy(com.emc.atmos.api.bean.AccessTokenPolicy)}
     */
    public CreateAccessTokenRequest policy( AccessTokenPolicy policy ) {
        this.policy = policy;
        return this;
    }

    /**
     * Gets the ACL that will be assigned to objects created using this access token.
     */
    public Acl getAcl() {
        return acl;
    }

    /**
     * Gets the token policy for the new access token.
     */
    public AccessTokenPolicy getPolicy() {
        return policy;
    }

    /**
     * Sets the ACL that will be assigned to objects created using this access token.
     */
    public void setAcl( Acl acl ) {
        this.acl = acl;
    }

    /**
     * Sets the token policy for the new access token.
     */
    public void setPolicy( AccessTokenPolicy policy ) {
        this.policy = policy;
    }
}
