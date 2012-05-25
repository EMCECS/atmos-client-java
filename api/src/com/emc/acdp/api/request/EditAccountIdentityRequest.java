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

package com.emc.acdp.api.request;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

/**
 * Changes the role of an identity in an account, e.g. from account_manager to
 * account_user.
 * 
 * @author cwikj
 * 
 */
public class EditAccountIdentityRequest extends BasicAcdpRequest {
    private static final Logger l4j = Logger
            .getLogger(EditAccountIdentityRequest.class);

    private String identityId;
    private String newRole;
    private String accountId;
    private String adminSessionId;
    private byte[] data;

    /**
     * Creates a new edit account identity request.
     * 
     * @param accountId the Account ID, e.g. A14855709755
     * @param identityId the Identity ID, e.g. BCool@AphidsRus.com
     * @param newRole the new role, e.g. account_manager or account_user
     * @param adminSessionId the admin session ID
     */
    public EditAccountIdentityRequest(String accountId, String identityId,
            String newRole, String adminSessionId) {
        this.identityId = identityId;
        this.newRole = newRole;
        this.accountId = accountId;
        this.adminSessionId = adminSessionId;

        requestHeaders.put(CONTENT_TYPE, FORM_CONTENT_TYPE);
        String formdata = "account_role=" + newRole;
        l4j.debug("form data: " + formdata);
        try {
            this.data = formdata.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding failed: " + e, e);
        }
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format(
                "/cdp-rest/v1/admin/accounts/{0}/identities/{1}", accountId,
                identityId);
    }

    @Override
    public String getRequestQuery() {
        return CDP_SESSION_PARAM + "=" + adminSessionId;
    }

    @Override
    public String getMethod() {
        return POST_METHOD;
    }

    @Override
    public long getRequestSize() {
        return data.length;
    }

    @Override
    public byte[] getRequestData() {
        return data;
    }

    @Override
    public boolean hasResponseBody() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return the identityId
     */
    public String getIdentityId() {
        return identityId;
    }

    /**
     * @param identityId
     *            the identityId to set
     */
    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    /**
     * @return the newRole
     */
    public String getNewRole() {
        return newRole;
    }

    /**
     * @param newRole
     *            the newRole to set
     */
    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the adminSessionId
     */
    public String getAdminSessionId() {
        return adminSessionId;
    }

    /**
     * @param adminSessionId the adminSessionId to set
     */
    public void setAdminSessionId(String adminSessionId) {
        this.adminSessionId = adminSessionId;
    }

}
