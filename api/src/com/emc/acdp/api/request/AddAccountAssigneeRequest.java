/**
 * 
 */
package com.emc.acdp.api.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * Adds a new assignee to the account. This will create a new identity and then
 * assign it to the account with the requested role. Note that the identity must
 * not exist.
 * 
 * @author cwikj
 * @since 1.1.2
 */
public class AddAccountAssigneeRequest extends BasicAcdpRequest {

    private String accountId;
    private String identityId;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private String adminSessionId;
    private String email;
    private byte[] data;

    /**
     * Creates a new request.
     * 
     * @param accountId the ID for the account
     * @param identityId the ID for the new identity
     * @param password the password for the new identity
     * @param firstName the firstName for the new identity
     * @param lastName the lastName for the new identity
     * @param email the email address for the new identity.
     * @param role The role for the identity.  Should be account_manager or
     * account_user.
     * @param adminSessionId the Admin session authentication token.
     */
    public AddAccountAssigneeRequest(String accountId, String identityId,
            String password, String firstName, String lastName, String email,
            String role, String adminSessionId) {
        this.accountId = accountId;
        this.identityId = identityId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.adminSessionId = adminSessionId;
        this.email = email;
        
        try {
            String postdata = "account_role=" + URLEncoder.encode(role, "UTF-8") + 
                    "&password=" + URLEncoder.encode(password, "UTF-8") + 
                    "&firstName=" + URLEncoder.encode(firstName, "UTF-8") +
                    "&lastName=" + URLEncoder.encode(lastName, "UTF-8") +
                    "&email=" + URLEncoder.encode(email, "UTF-8");
            data = postdata.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding data: " + e.getMessage(), e);
        }
        
        requestHeaders.put(CONTENT_TYPE, FORM_CONTENT_TYPE);
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return MessageFormat.format(
                "/cdp-rest/v1/admin/accounts/{0}/identities/{1}", accountId,
                identityId);
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
        return false;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @return the identityId
     */
    public String getIdentityId() {
        return identityId;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return the adminSessionId
     */
    public String getAdminSessionId() {
        return adminSessionId;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

}
