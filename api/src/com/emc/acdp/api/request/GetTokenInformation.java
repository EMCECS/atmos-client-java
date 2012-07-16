/**
 * 
 */
package com.emc.acdp.api.request;

import java.text.MessageFormat;

import com.emc.cdp.services.rest.model.Token;

/**
 * @author cwikj
 *
 */
public class GetTokenInformation extends AcdpXmlResponseRequest<Token> {
    private String accountId;
    private String subscriptionId;
    private String tokenGroupId;
    private String tokenId;
    private String adminSessionId;
    private boolean showFullInfo;

    public GetTokenInformation(String accountId, String subscriptionId,
            String tokenGroupId, String tokenId, boolean showFullInfo,
            String adminSessionId) {
        this.accountId = accountId;
        this.subscriptionId = subscriptionId;
        this.tokenGroupId = tokenGroupId;
        this.tokenId = tokenId;
        this.adminSessionId = adminSessionId;
        this.showFullInfo = showFullInfo;
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format(
                "/cdp-rest/v1/admin/accounts/{0}/storage/{1}/tokengroups/{2}/tokens/{3}",
                accountId, subscriptionId, tokenGroupId, tokenId);
    }

    @Override
    public String getRequestQuery() {
        return MessageFormat.format("{0}={1}&{2}={3}",
                CDP_SESSION_PARAM, adminSessionId, "show_full_info", ""+showFullInfo);
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

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @return the tokenGroupId
     */
    public String getTokenGroupId() {
        return tokenGroupId;
    }

    /**
     * @return the tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * @return the adminSessionId
     */
    public String getAdminSessionId() {
        return adminSessionId;
    }

    /**
     * @return the showFullInfo
     */
    public boolean isShowFullInfo() {
        return showFullInfo;
    }

}
