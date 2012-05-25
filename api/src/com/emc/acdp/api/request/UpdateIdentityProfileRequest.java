/**
 * 
 */
package com.emc.acdp.api.request;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import javax.xml.bind.JAXBException;

import com.emc.cdp.services.rest.model.Profile;

/**
 * Updates the profile for an identity.
 * @author cwikj
 *
 */
public class UpdateIdentityProfileRequest extends BasicAcdpRequest {

    private String identityId;
    private String adminSessionId;
    private byte[] data;
    private Profile profile;

    public UpdateIdentityProfileRequest(String identityId, Profile profile,
            String adminSessionId) {
        this.identityId = identityId;
        this.adminSessionId = adminSessionId;
        this.profile = profile;
        
        try {
            data = serialize(profile);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding data: " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new RuntimeException("Error encoding data: " + e.getMessage(), e);
        }
        requestHeaders.put(CONTENT_TYPE, XML_CONTENT_TYPE);
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return MessageFormat.format("/cdp-rest/v1/admin/identities/{0}/profile", identityId);
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
        return PUT_METHOD;
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
     * @return the adminSessionId
     */
    public String getAdminSessionId() {
        return adminSessionId;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

}
