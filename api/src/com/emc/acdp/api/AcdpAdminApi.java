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

package com.emc.acdp.api;

import java.util.Date;
import java.util.List;

import com.emc.cdp.services.rest.model.Account;
import com.emc.cdp.services.rest.model.Identity;
import com.emc.cdp.services.rest.model.LifecycleEventType;
import com.emc.cdp.services.rest.model.MeteringUsageList;
import com.emc.cdp.services.rest.model.Profile;
import com.emc.cdp.services.rest.model.SubscriptionList;
import com.emc.esu.api.EsuException;

/**
 * Interface for Atmos Cloud Delivery Platform API
 * 
 * @author cwikj
 */
public interface AcdpAdminApi {
    void adminLogin(String identity, String password) throws EsuException;

    void createIdentity(Identity id) throws EsuException;

    String createAccount(Account acct) throws EsuException;

    void deleteAccount(String accountId) throws EsuException;

    Identity adminGetIdentity(String id);

    String createSubscription(String accountId, String serviceId);

    void provisionSubscription(String accountId, String subscriptionId,
            boolean sendEmail);

    Account getIdentityAccount(String identity);

    SubscriptionList getAccountSubscriptions(String accountId);

    void adminAccountEvent(String accountId, LifecycleEventType adminSuspend);

    void deleteIdentity(String identityId);

    void addAccountAssignee(String accountId, String identityId,
            String password, String firstName, String lastName, String email,
            String role);

    void updateIdentityProfile(String identityId, Profile p);

    Identity getIdentity(String identityId);

    Account getAccount(String accountId);

    MeteringUsageList getSubscriptionUsage(String accountId, String subscriptionId,
            Date startDate, Date endDate, List<String> resources,
            String category);

    MeteringUsageList getSubscriptionUsage(String accountId, String subscriptionId,
            Date startDate, Date endDate, List<String> resources,
            String category, int start, int count);
    
    void deleteSubscription(String accountId, String subscriptionId);
    
    void unassignAccountIdentity(String accountId, String identityId);

}
