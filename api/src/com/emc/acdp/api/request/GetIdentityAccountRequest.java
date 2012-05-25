/**
 * 
 */
package com.emc.acdp.api.request;

import java.text.MessageFormat;

import com.emc.cdp.services.rest.model.Account;

/**
 * Gets the account assigned to an identity.
 * @author cwikj
 *
 */
public class GetIdentityAccountRequest extends AcdpXmlResponseRequest<Account> {
    private String identityId;
    private String adminSessionId;

    public GetIdentityAccountRequest(String identityId, String adminSessionId) {
        this.identityId = identityId;
        this.adminSessionId = adminSessionId;
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format("/cdp-rest/v1/admin/identities/{0}/account", identityId);
    }

    @Override
    public String getRequestQuery() {
        return CDP_SESSION_PARAM + "=" + adminSessionId;
    }

    @Override
    public String getMethod() {
        return GET_METHOD;
    }

    @Override
    public long getRequestSize() {
        return -1;
    }

    @Override
    public byte[] getRequestData() {
        return null;
    }

}
