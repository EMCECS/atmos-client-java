/**
 * 
 */
package com.emc.acdp.api.request;

import java.text.MessageFormat;

/**
 * Deletes an identity
 * @author cwikj
 *
 */
public class DeleteIdentityRequest extends BasicAcdpRequest {
    private String identityId;
    private String adminSessionId;

    public DeleteIdentityRequest(String identityId, String adminSessionId) {
        this.identityId = identityId;
        this.adminSessionId = adminSessionId;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return MessageFormat.format("/cdp-rest/v1/admin/identities/{0}", identityId);
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
        return DELETE_METHOD;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestSize()
     */
    @Override
    public long getRequestSize() {
        return -1;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestData()
     */
    @Override
    public byte[] getRequestData() {
        return null;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#hasResponseBody()
     */
    @Override
    public boolean hasResponseBody() {
        // TODO Auto-generated method stub
        return false;
    }

}
