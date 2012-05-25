/**
 * 
 */
package com.emc.acdp.api.request;

import java.text.MessageFormat;

import com.emc.cdp.services.rest.model.Account;

/**
 * @author cwikj
 *
 */
public class GetAccountRequest extends AcdpXmlResponseRequest<Account> {

    private String accountId;
    private String adminSessionId;

    public GetAccountRequest(String accountId, String adminSession) {
        this.accountId = accountId;
        this.adminSessionId = adminSession;
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format("/cdp-rest/v1/admin/accounts/{0}", accountId);
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
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public byte[] getRequestData() {
        // TODO Auto-generated method stub
        return null;
    }

}
