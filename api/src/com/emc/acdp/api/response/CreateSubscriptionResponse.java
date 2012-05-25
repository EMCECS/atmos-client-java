/**
 * 
 */
package com.emc.acdp.api.response;

import java.util.List;
import java.util.Map;

/**
 * @author cwikj
 * 
 */
public class CreateSubscriptionResponse extends AcdpResponse {
    private String subscriptionId;

    /**
     * @param e
     */
    public CreateSubscriptionResponse(Throwable e) {
        super(e);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param responseHeaders
     * @param contentType
     * @param httpCode
     * @param successful
     */
    public CreateSubscriptionResponse(
            Map<String, List<String>> responseHeaders, String contentType,
            int httpCode, boolean successful, String subscriptionId) {
        super(responseHeaders, contentType, httpCode, successful);
        this.subscriptionId = subscriptionId;
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

}
