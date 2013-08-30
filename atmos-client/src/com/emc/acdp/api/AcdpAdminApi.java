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

import com.emc.cdp.services.rest.model.*;

import java.util.Date;
import java.util.List;

/**
 * Interface for Atmos Cloud Delivery Platform API
 *
 * @author cwikj
 */
public interface AcdpAdminApi {
    String createAccount( Account acct );

    void deleteAccount( String accountId );

    AccountList listAccounts( boolean includeSubscription );

    AccountList listAccounts( boolean includeSubscription, int start, int count );

    String createAccountInvitation( String accountId, String email, String accountRole );

    String createSubscription( String accountId, String serviceId );

    void provisionSubscription( String accountId, String subscriptionId, boolean sendEmail );

    Account getIdentityAccount( String identity, boolean includeSubscription );

    SubscriptionList getAccountSubscriptions( String accountId );

    Subtenant getSubtenant( String accountId, String subscriptionId );

    void adminAccountEvent( String accountId, LifecycleEventType adminSuspend );

    void deleteIdentity( String identityId );

    void addAccountAssignee( String accountId,
                             String identityId,
                             String password,
                             String firstName,
                             String lastName,
                             String email,
                             String role );

    AssigneeList listAccountAssignees( String accountId, boolean includeProfile );

    Assignee getAccountAssignee( String accountId, String identityId, boolean includeProfile );

    void editAccountAssignee( String accountId, String identityId, String newRole );

    void RemoveAccountAssignee( String accountId, String identityId );

    void updateIdentityProfile( String identityId, Profile p );

    IdentityList listIdentities( boolean listAllAccounts, boolean includeProfile );

    IdentityList listIdentities( boolean listAllAccounts, boolean includeProfile, int start, int count );

    Identity getIdentity( String identityId );

    Account getAccount( String accountId );

    MeteringUsageList getSubscriptionUsage( String accountId,
                                            String subscriptionId,
                                            Date startDate,
                                            Date endDate,
                                            List<String> resources,
                                            String category );

    MeteringUsageList getSubscriptionUsage( String accountId,
                                            String subscriptionId,
                                            Date startDate,
                                            Date endDate,
                                            List<String> resources,
                                            String category,
                                            int start,
                                            int count );

    void deleteSubscription( String accountId, String subscriptionId );

    void unassignAccountIdentity( String accountId, String identityId );

    TokenGroupList listTokenGroups( String accountId, String subscriptionId );

    TokenGroupList listTokenGroups( String accountId, String subscriptionId, int start, int count );

    MeteringUsageList getTokenGroupUsage( String accountId,
                                          String subscriptionId,
                                          String tokenGroupId,
                                          Date startDate,
                                          Date endDate,
                                          List<String> resources,
                                          String category );

    MeteringUsageList getTokenGroupUsage( String accountId,
                                          String subscriptionId,
                                          String tokenGroupId,
                                          Date startDate,
                                          Date endDate,
                                          List<String> resources,
                                          String category,
                                          int start,
                                          int count );

    TokenList listTokens( String accountId, String subscriptionId, String tokenGroupId );

    TokenList listTokens( String accountId, String subscriptionId, String tokenGroupId, int start, int count );

    Token getTokenInformation( String accountId,
                               String subscriptionId,
                               String tokenGroupId,
                               String tokenId,
                               boolean showFullInfo );
}
