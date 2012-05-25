/**
 * 
 */
package com.emc.acdp.api.request;

import java.text.MessageFormat;

import com.emc.cdp.services.rest.model.SubscriptionList;

/**
 * Lists the subscriptions assigned to an account.
 * @author cwikj
 *
 */
public class ListAccountSubscriptionsRequest extends AcdpXmlResponseRequest<SubscriptionList> {
    private String accountId;
    private String adminSessionId;

    public ListAccountSubscriptionsRequest(String accountId, String adminSessionId) {
        this.accountId = accountId;
        this.adminSessionId = adminSessionId;
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format("/cdp-rest/v1/admin/accounts/{0}/subscriptions", accountId);
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
