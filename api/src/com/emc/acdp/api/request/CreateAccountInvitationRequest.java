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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.CreateAccountInvitationResponse;
import com.emc.cdp.services.rest.model.Error;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class CreateAccountInvitationRequest extends
        AcdpRequest<CreateAccountInvitationResponse> {
    private static final Logger l4j = Logger
            .getLogger(CreateAccountInvitationRequest.class);

    private String accountId;
    private String identity;
    private String accountRole;
    private byte[] data;
    private String adminSessionId;

    public CreateAccountInvitationRequest(String accountId, String identity,
            String accountRole, String adminSessionId) {
        this.accountId = accountId;
        this.identity = identity;
        this.accountRole = accountRole;
        this.adminSessionId = adminSessionId;

        // Create data
        try {
            String formdata = "email=" + URLEncoder.encode(identity, "UTF-8")
                    + "&account_role="
                    + URLEncoder.encode(accountRole, "UTF-8");
            l4j.debug("Form data: " + formdata);
            data = formdata.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode data");
        }

        requestHeaders.put(CONTENT_TYPE, FORM_CONTENT_TYPE);
    }

    @Override
    public String getRequestPath() {
        return "/cdp-rest/v1/admin/accounts/" + accountId + "/invitations";
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
        return false;
    }

    @Override
    public CreateAccountInvitationResponse parseResponse(int responseCode,
            String responseLine, Map<String, List<String>> headerFields,
            InputStream in) {
        throw new UnsupportedOperationException("Unexpected response body");
    }

    @Override
    public CreateAccountInvitationResponse parseResponse(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields) {

        List<String> location = headerFields.get(LOCATION_FIELD);
        if (location == null || location.size() < 1) {
            return parseError(new EsuException(
                    "Location header not found in response"));
        }

        String sloc = location.get(0);
        // Parse account invite ID from sloc
        // e.g.
        // http://localhost:8080/cdp-rest/v1/admin/accounts/A96342522441/invitations/fd5da3cd-7388-4a7b-894a-183b8589c715
        int last = sloc.lastIndexOf('/');
        if (last == -1) {
            return parseError(new EsuException(
                    "Could not parse account invitation ID"));
        }

        String accountId = sloc.substring(last + 1);

        return new CreateAccountInvitationResponse(headerFields, "",
                responseCode, true, accountId);
    }

    @Override
    public CreateAccountInvitationResponse parseError(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields,
            byte[] errorBody) {
        try {
            Error err = (Error) deserialize(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            CreateAccountInvitationResponse r = new CreateAccountInvitationResponse(
                    ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            CreateAccountInvitationResponse r = new CreateAccountInvitationResponse(
                    ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public CreateAccountInvitationResponse parseError(Throwable e) {
        return new CreateAccountInvitationResponse(e);
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId
     *            the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @param identity
     *            the identity to set
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * @return the accountRole
     */
    public String getAccountRole() {
        return accountRole;
    }

    /**
     * @param accountRole
     *            the accountRole to set
     */
    public void setAccountRole(String accountRole) {
        this.accountRole = accountRole;
    }
}
