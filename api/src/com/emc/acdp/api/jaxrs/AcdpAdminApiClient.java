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

package com.emc.acdp.api.jaxrs;

import com.emc.acdp.api.AcdpAdminApi;
import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpException;
import com.emc.cdp.services.rest.model.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author cwikj
 */
public class AcdpAdminApiClient implements AcdpAdminApi {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

    private AcdpConfig config;
    private WebResource adminResource;

    public AcdpAdminApiClient( AcdpConfig config ) {
        this.config = config;
    }

    @Override
    public String createAccount( Account acct ) {
        ClientResponse response = getAdminResource().path( "accounts" )
                .type( MediaType.TEXT_XML )
                .post( ClientResponse.class,
                       acct );

        String location = response.getLocation().toString();
        if ( location == null ) throw new AcdpException( "Location missing from create account response" );

        return location.substring( location.lastIndexOf( "/" ) + 1 );
    }

    @Override
    public void deleteAccount( String accountId ) {
        getAdminResource().path( "accounts/" + accountId ).delete();
    }

    @Override
    public AccountList listAccounts( boolean includeSubscription ) {
        return listAccounts( includeSubscription, 1, 1000 );
    }

    @Override
    public AccountList listAccounts( boolean includeSubscription, int start, int count ) {
        WebResource resource = getAdminResource().path( "accounts" );
        if ( includeSubscription ) resource = resource.queryParam( "with_subscriptions", "true" );
        if ( start > -1 ) resource = resource.queryParam( "start", "" + start );
        if ( count > -1 ) resource = resource.queryParam( "count", "" + count );
        return resource.get( AccountList.class );
    }

    @Override
    public IdentityList listIdentities( boolean listAllAccounts, boolean includeProfile ) {
        return listIdentities( listAllAccounts, includeProfile, 1, 1000 );
    }

    @Override
    public IdentityList listIdentities( boolean listAllAccounts, boolean includeProfile, int start, int count ) {
        WebResource resource = getAdminResource().path( "identities" );
        if ( listAllAccounts ) resource = resource.queryParam( "show_all_identities", "true" );
        if ( includeProfile ) resource = resource.queryParam( "show_profile", "true" );
        if ( start > -1 ) resource = resource.queryParam( "start", "" + start );
        if ( count > -1 ) resource = resource.queryParam( "count", "" + count );
        return resource.get( IdentityList.class );
    }

    @Override
    public Identity getIdentity( String id ) {
        return getAdminResource().path( "identities/" + id ).get( Identity.class );
    }

    @Override
    public String createAccountInvitation( String accountId, String email, String accountRole ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "email", email );
        params.putSingle( "account_role", accountRole );

        ClientResponse response = getAdminResource().path( "accounts/" + accountId + "/invitations" )
                .type( MediaType.APPLICATION_FORM_URLENCODED )
                .post( ClientResponse.class, params );

        String location = response.getLocation().toString();
        if ( location == null ) throw new AcdpException( "Location missing from create account invitation response" );

        return location.substring( location.lastIndexOf( "/" ) + 1 );
    }

    @Override
    public String createSubscription( String accountId, String serviceId ) {
        Subscription subscription = new Subscription();
        subscription.setServiceId( serviceId );

        ClientResponse response = getAdminResource().path( "accounts/" + accountId + "/subscriptions" )
                .type( MediaType.TEXT_XML )
                .post( ClientResponse.class, subscription );

        String location = response.getLocation().toString();
        if ( location == null ) throw new AcdpException( "Location missing from create subscription response" );

        return location.substring( location.lastIndexOf( "/" ) + 1 );
    }

    @Override
    public void provisionSubscription( String accountId, String subscriptionId, boolean sendEmail ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "send_email", Boolean.toString( sendEmail ) );

        getAdminResource().path( "accounts/" + accountId + "/storage/" + subscriptionId )
                .type( MediaType.APPLICATION_FORM_URLENCODED )
                .post( params );
    }

    @Override
    public Account getIdentityAccount( String identityId, boolean includeSubscription ) {
        WebResource resource = getAdminResource().path( "identities/" + identityId + "/account" );
        if ( includeSubscription ) resource = resource.queryParam( "with_subscriptions", "true" );
        return resource.get( Account.class );
    }

    @Override
    public SubscriptionList getAccountSubscriptions( String accountId ) {
        return getAdminResource().path( "accounts/" + accountId + "/subscriptions" ).get( SubscriptionList.class );
    }

    @Override
    public Subtenant getSubtenant( String accountId, String subscriptionId ) {
        return getAdminResource().path( "accounts/" + accountId + "/storage/" + subscriptionId + "/subtenant" )
                .get( Subtenant.class );
    }

    @Override
    public void adminAccountEvent( String accountId, LifecycleEventType eventType ) {
        LifecycleEvent lifecycleEvent = new LifecycleEvent();
        lifecycleEvent.setTargetId( accountId );
        lifecycleEvent.setTargetType( LifecycleTargetType.ACCOUNT );
        lifecycleEvent.setEventType( eventType );

        getAdminResource().path( "events" ).type( MediaType.TEXT_XML ).post( lifecycleEvent );
    }

    @Override
    public void deleteIdentity( String identityId ) {
        getAdminResource().path( "identities/" + identityId ).delete();
    }

    @Override
    public void addAccountAssignee( String accountId,
                                    String identityId,
                                    String password,
                                    String firstName,
                                    String lastName,
                                    String email,
                                    String role ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "account_role", role );
        params.putSingle( "password", password );
        params.putSingle( "firstName", firstName );
        params.putSingle( "lastName", lastName );
        params.putSingle( "email", email );

        getAdminResource().path( "accounts/" + accountId + "/identities/" + identityId )
                .type( MediaType.APPLICATION_FORM_URLENCODED )
                .put( params );
    }

    @Override
    public AssigneeList listAccountAssignees( String accountId, boolean includeProfile ) {
        WebResource resource = getAdminResource().path( "accounts/" + accountId + "/identities" );
        if ( includeProfile ) resource = resource.queryParam( "show_profile", "true" );
        return resource.get( AssigneeList.class );
    }

    @Override
    public Assignee getAccountAssignee( String accountId, String identityId, boolean includeProfile ) {
        WebResource resource = getAdminResource().path( "accounts/" + accountId + "/identities/" + identityId );
        if ( includeProfile ) resource = resource.queryParam( "show_profile", "true" );
        return resource.get( Assignee.class );
    }

    @Override
    public void editAccountAssignee( String accountId, String identityId, String newRole ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "account_role", newRole );

        getAdminResource().path( "accounts/" + accountId + "/identities/" + identityId )
                .type( MediaType.APPLICATION_FORM_URLENCODED )
                .post( params );
    }

    @Override
    public void RemoveAccountAssignee( String accountId, String identityId ) {
        getAdminResource().path( "accounts/" + accountId + "/identities/" + identityId ).delete();
    }

    @Override
    public void updateIdentityProfile( String identityId, Profile profile ) {
        getAdminResource().path( "identities/" + identityId + "/profile" ).type( MediaType.TEXT_XML ).put( profile );
    }

    public Account getAccount( String accountId ) {
        return getAdminResource().path( "accounts/" + accountId ).get( Account.class );
    }

    @Override
    public MeteringUsageList getSubscriptionUsage( String accountId,
                                                   String subscriptionId,
                                                   Date startDate,
                                                   Date endDate,
                                                   List<String> resources,
                                                   String category ) {
        return getSubscriptionUsage( accountId, subscriptionId, startDate,
                                     endDate, resources, category, 1, 1000 );
    }

    @Override
    public MeteringUsageList getSubscriptionUsage( String accountId,
                                                   String subscriptionId,
                                                   Date startDate,
                                                   Date endDate,
                                                   List<String> resources,
                                                   String category,
                                                   int start,
                                                   int count ) {
        String resourceStr = "";
        for ( String resource : resources ) {
            resourceStr += resource + ",";
        }
        resourceStr = resourceStr.substring( 0, resourceStr.length() - 1 );

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "start_date", DATE_FORMAT.format( startDate ) );
        params.putSingle( "end_date", DATE_FORMAT.format( endDate ) );
        params.putSingle( "resources", resourceStr );
        params.putSingle( "cat", category );
        if ( start > -1 )
            params.putSingle( "start", "" + start );
        if ( count > -1 )
            params.putSingle( "count", "" + count );

        return getAdminResource().path( "accounts/" + accountId + "/storage/" + subscriptionId + "/usage" )
                .queryParams( params )
                .get( MeteringUsageList.class );
    }

    @Override
    public void deleteSubscription( String accountId, String subscriptionId ) {
        getAdminResource().path( "accounts/" + accountId + "/subscriptions/" + subscriptionId ).delete();
    }

    @Override
    public TokenGroupList listTokenGroups( String accountId, String subscriptionId ) {
        return listTokenGroups( accountId, subscriptionId, 1, 1000 );
    }

    @Override
    public TokenGroupList listTokenGroups( String accountId, String subscriptionId, int start, int count ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "start", "" + start );
        params.putSingle( "count", "" + count );

        return getAdminResource().path( "accounts/" + accountId + "/storage/" + subscriptionId + "/tokengroups" )
                .queryParams( params )
                .get( TokenGroupList.class );
    }

    @Override
    public MeteringUsageList getTokenGroupUsage( String accountId,
                                                 String subscriptionId,
                                                 String tokenGroupId,
                                                 Date startDate,
                                                 Date endDate,
                                                 List<String> resources,
                                                 String category ) {
        return getTokenGroupUsage( accountId, subscriptionId, tokenGroupId, startDate,
                                   endDate, resources, category, 1, 1000 );
    }

    @Override
    public MeteringUsageList getTokenGroupUsage( String accountId,
                                                 String subscriptionId,
                                                 String tokenGroupId,
                                                 Date startDate,
                                                 Date endDate,
                                                 List<String> resources,
                                                 String category,
                                                 int start,
                                                 int count ) {
        String resourceStr = "";
        for ( String resource : resources ) {
            resourceStr += resource + ",";
        }
        resourceStr = resourceStr.substring( 0, resourceStr.length() - 1 );

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "start_date", DATE_FORMAT.format( startDate ) );
        params.putSingle( "end_date", DATE_FORMAT.format( endDate ) );
        params.putSingle( "resources", resourceStr );
        params.putSingle( "cat", category );
        if ( start > -1 )
            params.putSingle( "start", "" + start );
        if ( count > -1 )
            params.putSingle( "count", "" + count );

        return getAdminResource().path(
                "accounts/" + accountId + "/storage/" + subscriptionId + "/tokengroups/" + tokenGroupId + "/usage" )
                .queryParams( params )
                .get( MeteringUsageList.class );
    }

    @Override
    public TokenList listTokens( String accountId, String subscriptionId, String tokenGroupId ) {
        return listTokens( accountId, subscriptionId, tokenGroupId, 1, 1000 );
    }

    @Override
    public TokenList listTokens( String accountId, String subscriptionId, String tokenGroupId, int start, int count ) {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle( "start", "" + start );
        params.putSingle( "count", "" + count );

        return getAdminResource()
                .path( "accounts/" + accountId + "/storage/" + subscriptionId + "/tokengroups/" + tokenGroupId
                       + "/tokens" )
                .queryParams( params )
                .get( TokenList.class );
    }

    @Override
    public Token getTokenInformation( String accountId,
                                      String subscriptionId,
                                      String tokenGroupId,
                                      String tokenId,
                                      boolean showFullInfo ) {
        WebResource resource = getAdminResource().path(
                "accounts/" + accountId + "/storage/" + subscriptionId + "/tokengroups/" + tokenGroupId + "/tokens/"
                + tokenId );
        if ( showFullInfo )
            resource = resource.queryParam( "show_full_info", "true" );
        return resource.get( Token.class );
    }

    @Override
    public void unassignAccountIdentity( String accountId, String identityId ) {
        getAdminResource().path( "accounts/" + accountId + "/identities/" + identityId ).delete();
    }

    private WebResource getAdminResource() {
        if ( adminResource == null ) {
            Client client = RestUtil.createClient( config );
            adminResource = client.resource( config.getBaseUri() + "/cdp-rest/v1/admin" );
        }
        return adminResource;
    }
}
