/**
 * 
 */
package com.emc.acdp.api.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.CreateSubscriptionResponse;
import com.emc.cdp.services.rest.model.Error;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.cdp.services.rest.model.Subscription;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class CreateSubscriptionRequest extends
        AcdpRequest<CreateSubscriptionResponse> {
    private static final Logger l4j = Logger
            .getLogger(CreateSubscriptionRequest.class);

    private String accountId;
    private String adminSessionId;
    private byte[] data;
    private String serviceId;


    /**
     * 
     * @param accountId The account Id to add a subscription to.
     * @param serviceId The service to add.  You probably want "storageservice".
     * @param adminSessionId Your Admin session ID.
     */
    public CreateSubscriptionRequest(String accountId, String serviceId,
            String adminSessionId) {
        this.accountId = accountId;
        this.adminSessionId = adminSessionId;
        this.serviceId = serviceId;

        ObjectFactory of = new ObjectFactory();
        Subscription s = of.createSubscription();
        s.setServiceId(serviceId);
        try {
            data = serialize(s);
        } catch (UnsupportedEncodingException e) {
            throw new EsuException("Error marshalling XML: " + e.getMessage(),
                    e);
        } catch (JAXBException e) {
            throw new EsuException("Error marshalling XML: " + e.getMessage(),
                    e);
        }

        requestHeaders.put(CONTENT_TYPE, XML_CONTENT_TYPE);
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return "/cdp-rest/v1/admin/accounts/" + accountId + "/subscriptions";
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
     * @see com.emc.acdp.api.request.AcdpRequest#parseResponse(int,
     *      java.lang.String, java.util.Map, java.io.InputStream)
     */
    @Override
    public CreateSubscriptionResponse parseResponse(int responseCode,
            String responseLine, Map<String, List<String>> headerFields,
            InputStream in) {
        throw new UnsupportedOperationException("Unexpected response body");
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#parseResponse(int,
     *      java.lang.String, java.util.Map)
     */
    @Override
    public CreateSubscriptionResponse parseResponse(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields) {
        // Parse the new subscription ID from the Location header.
        List<String> location = headerFields.get(LOCATION_FIELD);
        if (location == null || location.size() < 1) {
            return parseError(new EsuException(
                    "Location header not found in response"));
        }

        String sloc = location.get(0);
        // Parse subscription ID from sloc
        // e.g.
        // http://localhost:8080/cdp-rest/v1/admin/accounts/A9634
        // 2522441/subscriptions/fd5da3cd-7388-4a7b-894a-183b8589c715
        int last = sloc.lastIndexOf('/');
        if (last == -1) {
            return parseError(new EsuException("Could not parse account ID"));
        }

        String subscriptionId = sloc.substring(last + 1);

        return new CreateSubscriptionResponse(headerFields, null, responseCode,
                true, subscriptionId);
    }

    @Override
    public CreateSubscriptionResponse parseError(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields,
            byte[] errorBody) {
        try {
            Error err = (Error) deserialize(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            CreateSubscriptionResponse r = new CreateSubscriptionResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            CreateSubscriptionResponse r = new CreateSubscriptionResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public CreateSubscriptionResponse parseError(Throwable e) {
        return new CreateSubscriptionResponse(e);
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

    /**
     * @return the subscriptionType
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param subscriptionType
     *            the subscriptionType to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

}
