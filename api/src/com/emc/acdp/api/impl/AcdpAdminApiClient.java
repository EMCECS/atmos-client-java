// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification, 
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice, 
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright 
//       notice, this list of conditions and the following disclaimer in the 
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote 
//       products derived from this software without specific prior written 
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//      POSSIBILITY OF SUCH DAMAGE.

package com.emc.acdp.api.impl;

import java.util.Date;
import java.util.List;

import com.emc.acdp.api.AcdpAdminApi;
import com.emc.acdp.api.request.AcdpRequest;
import com.emc.acdp.api.request.AddAccountAssigneeRequest;
import com.emc.acdp.api.request.AdminAccountEventRequest;
import com.emc.acdp.api.request.AdminLoginRequest;
import com.emc.acdp.api.request.CreateAccountRequest;
import com.emc.acdp.api.request.CreateIdentityRequest;
import com.emc.acdp.api.request.CreateSubscriptionRequest;
import com.emc.acdp.api.request.DeleteAccountRequest;
import com.emc.acdp.api.request.DeleteIdentityRequest;
import com.emc.acdp.api.request.DeleteSubscriptionRequest;
import com.emc.acdp.api.request.GetAccountRequest;
import com.emc.acdp.api.request.GetIdentityAccountRequest;
import com.emc.acdp.api.request.GetIdentityRequest;
import com.emc.acdp.api.request.GetSubscriptionUsage;
import com.emc.acdp.api.request.ListAccountSubscriptionsRequest;
import com.emc.acdp.api.request.ProvisionSubscriptionRequest;
import com.emc.acdp.api.request.UnassignAccountIdentityRequest;
import com.emc.acdp.api.request.UpdateIdentityProfileRequest;
import com.emc.acdp.api.response.AcdpResponse;
import com.emc.acdp.api.response.AcdpXmlResponse;
import com.emc.acdp.api.response.AdminLoginResponse;
import com.emc.acdp.api.response.CreateAccountResponse;
import com.emc.acdp.api.response.CreateSubscriptionResponse;
import com.emc.cdp.services.rest.model.Account;
import com.emc.cdp.services.rest.model.Identity;
import com.emc.cdp.services.rest.model.LifecycleEventType;
import com.emc.cdp.services.rest.model.MeteringUsageList;
import com.emc.cdp.services.rest.model.Profile;
import com.emc.cdp.services.rest.model.SubscriptionList;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class AcdpAdminApiClient implements AcdpAdminApi {
    private String endpoint;
    private String adminSession;

    public AcdpAdminApiClient(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.emc.acdp.api.AcdpAdminApi#adminLogin(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void adminLogin(String identity, String password)
            throws EsuException {
        AdminLoginRequest req = new AdminLoginRequest(identity, password);

        AdminLoginResponse r = execute(req);

        adminSession = r.getAdminSessionId();

    }

    /**
     * @see com.emc.acdp.api.AcdpAdminApi#createIdentity(com.emc.cdp.services.rest.model
     *      .Identity)
     */
    @Override
    public void createIdentity(Identity id) throws EsuException {
        CreateIdentityRequest cir = new CreateIdentityRequest(id);

        execute(cir);
    }

    /**
     * @see com.emc.acdp.api.AcdpAdminApi#createAccount(com.emc.cdp.services.rest.model
     *      .Account)
     */
    @Override
    public String createAccount(Account acct) throws EsuException {
        if (adminSession == null) {
            throw new IllegalStateException(
                    "You must perform an adminLogin before createAccount");
        }

        CreateAccountRequest car = new CreateAccountRequest(acct, adminSession);

        CreateAccountResponse r = execute(car);

        return r.getAccountId();

    }

    @Override
    public void deleteAccount(String accountId) throws EsuException {
        DeleteAccountRequest dar = new DeleteAccountRequest(accountId,
                adminSession);
        execute(dar);
    }

    @Override
    public Identity adminGetIdentity(String id) {
        GetIdentityRequest gir = new GetIdentityRequest(id, adminSession);

        AcdpXmlResponse<Identity> r = execute(gir);

        return r.getResponse();
    }

    private <U extends AcdpRequest<T>, T extends AcdpResponse> T execute(U req) {
        req.setEndpoint(endpoint);

        T res;
        try {
            res = req.call();
        } catch (Exception e) {
            if (e instanceof EsuException) {
                throw (EsuException) e;
            } else {
                throw new EsuException("Error executing request: "
                        + e.getMessage(), e);
            }
        }

        if (!res.isSuccessful()) {
            if (res.getError() instanceof EsuException) {
                throw (EsuException) res.getError();
            } else if (res.getError() == null) {
                throw new EsuException("Request failed but no error logged");
            } else {
                throw new EsuException("Error in execute: " + res.getError(),
                        res.getError());
            }
        }

        return res;
    }

    @Override
    public String createSubscription(String accountId, String serviceId) {
        CreateSubscriptionRequest req = new CreateSubscriptionRequest(
                accountId, serviceId, adminSession);

        CreateSubscriptionResponse res = execute(req);

        return res.getSubscriptionId();
    }

    @Override
    public void provisionSubscription(String accountId, String subscriptionId,
            boolean sendEmail) {
        ProvisionSubscriptionRequest psr = new ProvisionSubscriptionRequest(
                accountId, subscriptionId, sendEmail, adminSession);

        execute(psr);
    }

    @Override
    public Account getIdentityAccount(String identity) {
        GetIdentityAccountRequest req = new GetIdentityAccountRequest(identity,
                adminSession);

        return execute(req).getResponse();
    }

    @Override
    public SubscriptionList getAccountSubscriptions(String accountId) {
        ListAccountSubscriptionsRequest req = new ListAccountSubscriptionsRequest(
                accountId, adminSession);

        return execute(req).getResponse();
    }

    @Override
    public void adminAccountEvent(String accountId, LifecycleEventType eventType) {
        AdminAccountEventRequest req = new AdminAccountEventRequest(accountId,
                eventType, adminSession);

        execute(req);
    }

    @Override
    public void deleteIdentity(String identityId) {
        DeleteIdentityRequest req = new DeleteIdentityRequest(identityId,
                adminSession);

        execute(req);
    }

    @Override
    public void addAccountAssignee(String accountId, String identityId,
            String password, String firstName, String lastName, String email,
            String role) {

        AddAccountAssigneeRequest req = new AddAccountAssigneeRequest(
                accountId, identityId, password, firstName, lastName, email,
                role, adminSession);

        execute(req);
    }

    @Override
    public void updateIdentityProfile(String identityId, Profile profile) {
        UpdateIdentityProfileRequest req = new UpdateIdentityProfileRequest(
                identityId, profile, adminSession);

        execute(req);
    }

    @Override
    public Identity getIdentity(String identityId) {
        GetIdentityRequest req = new GetIdentityRequest(identityId,
                adminSession);

        return execute(req).getResponse();
    }

    public Account getAccount(String accountId) {
        GetAccountRequest req = new GetAccountRequest(accountId, adminSession);

        return execute(req).getResponse();
    }

    @Override
    public MeteringUsageList getSubscriptionUsage(String accountId,
            String subscriptionId, Date startDate, Date endDate,
            List<String> resources, String category) {
        return getSubscriptionUsage(accountId, subscriptionId, startDate,
                endDate, resources, category, -1, -1);
    }

    @Override
    public MeteringUsageList getSubscriptionUsage(String accountId,
            String subscriptionId, Date startDate, Date endDate,
            List<String> resources, String category, int start, int count) {
        GetSubscriptionUsage req = new GetSubscriptionUsage(accountId,
                subscriptionId, startDate, endDate, resources, category, adminSession);
        req.setStart(start);
        req.setCount(count);

        return execute(req).getResponse();
    }

    @Override
    public void deleteSubscription(String accountId, String subscriptionId) {
        DeleteSubscriptionRequest req = new DeleteSubscriptionRequest(accountId, subscriptionId, adminSession);
        
        execute(req);
    }

	@Override
	public void unassignAccountIdentity(String accountId, String identityId) {
		UnassignAccountIdentityRequest req = new UnassignAccountIdentityRequest(accountId, identityId, adminSession);
		
		execute(req);
	}

}
