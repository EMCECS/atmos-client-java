/**
 * 
 */
package com.emc.acdp.api.request;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import com.emc.cdp.services.rest.model.LifecycleEvent;
import com.emc.cdp.services.rest.model.LifecycleEventType;
import com.emc.cdp.services.rest.model.LifecycleTargetType;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.esu.api.EsuException;

/**
 * Sends an account lifecycle event request.
 * @author cwikj
 *
 */
public class AdminAccountEventRequest extends BasicAcdpRequest {
    private String accountId;
    private LifecycleEventType eventType;
    private String adminSessionId;
    
    private byte[] data;
    
    public AdminAccountEventRequest(String accountId, LifecycleEventType eventType, String adminSessionId) {
        this.accountId = accountId;
        this.eventType = eventType;
        this.adminSessionId = adminSessionId;
        
        ObjectFactory of = new ObjectFactory();
        LifecycleEvent evt = of.createLifecycleEvent();
        evt.setTargetId(accountId);
        evt.setTargetType(LifecycleTargetType.ACCOUNT);
        evt.setEventType(eventType);
        
        try {
            data = serialize(evt);
        } catch (JAXBException e) {
            throw new EsuException("Error marshalling XML: " + e.getMessage(),
                    e);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
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
        return "/cdp-rest/v1/admin/events";
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
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the eventType
     */
    public LifecycleEventType getEventType() {
        return eventType;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(LifecycleEventType eventType) {
        this.eventType = eventType;
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
