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
package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.RestUtil;
import com.emc.util.BasicResponse;

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
