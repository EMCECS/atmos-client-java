/**
 * 
 */
package com.emc.acdp.api.request;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

/**
 * Provisions the storage's subscription (creates subtenant, token group,
 * and default token).
 * @author cwikj
 */
public class ProvisionSubscriptionRequest extends BasicAcdpRequest {
    private byte[] data;
    private String accountId;
    private String subscriptionId;
    private boolean sendEmail;
    private String adminSessionId;

    /**
     * 
     */
    public ProvisionSubscriptionRequest(String accountId,
            String subscriptionId, boolean sendEmail, String adminSessionId) {
        this.accountId = accountId;
        this.subscriptionId = subscriptionId;
        this.sendEmail = sendEmail;
        this.adminSessionId = adminSessionId;

        requestHeaders.put(CONTENT_TYPE, FORM_CONTENT_TYPE);

        String postdata = "send_email=" + sendEmail;
        try {
            data = postdata.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding failed: " + e.getMessage(), e);
        }
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return MessageFormat.format(
                "/cdp-rest/v1/admin/accounts/{0}/storage/{1}", accountId,
                subscriptionId);
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestQuery()
     */
    @Override
    public String getRequestQuery() {
        return CDP_SESSION_PARAM + "=" + adminSessionId;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return POST_METHOD;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestSize()
     */
    @Override
    public long getRequestSize() {
        return data.length;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestData()
     */
    @Override
    public byte[] getRequestData() {
        return data;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#hasResponseBody()
     */
    @Override
    public boolean hasResponseBody() {
        return false;
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
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionId
     *            the subscriptionId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the sendEmail
     */
    public boolean isSendEmail() {
        return sendEmail;
    }

    /**
     * @param sendEmail
     *            the sendEmail to set
     */
    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    /**
     * @return the adminSessionId
     */
    public String getAdminSessionId() {
        return adminSessionId;
    }

    /**
     * @param adminSessionId
     *            the adminSessionId to set
     */
    public void setAdminSessionId(String adminSessionId) {
        this.adminSessionId = adminSessionId;
    }

}
