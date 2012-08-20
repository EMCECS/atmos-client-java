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
package com.emc.acdp.api.test;

import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpAdminApi;
import com.emc.acdp.api.jersey.AcdpAdminApiClient;
import com.emc.cdp.services.rest.model.Account;
import com.emc.esu.api.EsuException;
import com.emc.util.PropertiesUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Random;

/**
 * This test case tests provisioning an ACDP account from front-to-back using
 * the ACDP Admin API.
 * Note: Requires CDP 1.1.2+ for addAccountAssignee.
 *
 * @author cwikj
 */
public class AdminProvisionTest {
    private AcdpConfig config;

    public AdminProvisionTest() throws Exception {
        config = loadAcdpConfig( "acdp.properties" );
    }

    @Test
    public void testProvisionSequence() {
        // Step 0: Login
        AcdpAdminApi api = new AcdpAdminApiClient( config );

        // Step 1: Create an Account
        Account acct = new Account();
        acct.setName( "Testcase Account" );
        acct.setDescription( "This account was created through JUnit" );
        acct.setType( "web" );

        String accountId = api.createAccount( acct );
        Assert.assertNotNull( "Empty AccountID", accountId );

        // Step 2: Create an Identity and assign it as the account admin
        String firstName = rand8char();
        String lastName = rand8char();
        String email = generateEmail();
        String password = rand8char() + "!";
        String role = "account_manager";
        String identityId = email;

        api.addAccountAssignee( accountId, identityId, password, firstName, lastName, email, role );

        // Step 3: Create a subscription for the account
        String subscriptionId = api.createSubscription( accountId, "storageservice" );
        Assert.assertNotNull( "Subscription ID is null!", subscriptionId );

        // Now do the reverse

        // Step 4: Delete the account subscription
        api.deleteSubscription( accountId, subscriptionId );

        // Step 5: Unassign the identity
        api.unassignAccountIdentity( accountId, identityId );

        // Step 6: Delete the identity
        api.deleteIdentity( identityId );

        // Step 7: Delete the account
        api.deleteAccount( accountId );

    }

    @Test
    public void testAssignIdentityError() {
        // Step 0: Login
        AcdpAdminApi api = new AcdpAdminApiClient( config );

        // Step 1: Create an Account
        Account acct = new Account();
        acct.setName( "Testcase Account" );
        acct.setDescription( "This account was created through JUnit" );
        acct.setType( "web" );

        String accountId = api.createAccount( acct );
        Assert.assertNotNull( "Empty AccountID", accountId );

        // Step 2: Create an Identity and assign it as the account admin
        String firstName = rand8char();
        String lastName = rand8char();
        String email = generateEmail();
        String password = rand8char() + "!";
        String role = "account_manager";
        String identityId = email;

        api.addAccountAssignee( accountId, identityId, password, firstName, lastName, email, role );

        try {
            // Do it again, should get error.
            api.addAccountAssignee( accountId, identityId, password, firstName, lastName, email, role );
            Assert.fail( "Expected Exception" );
        } catch ( EsuException e ) {
            Assert.assertEquals( "HTTP code wrong", 409, e.getHttpCode() );
            String msg = MessageFormat.format(
                    "The identity \"{0}\" is already assigned to an account",
                    identityId );
            Assert.assertEquals( "Error message incorrect", msg, e.getMessage() );
        }

        // Cleanup

        // Step 5: Unassign the identity
        api.unassignAccountIdentity( accountId, identityId );

        // Step 6: Delete the identity
        api.deleteIdentity( identityId );

        // Step 7: Delete the account
        api.deleteAccount( accountId );

    }

    private String generateEmail() {
        return rand8char() + "@" + rand8char() + ".com";
    }

    protected String rand8char() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer( 8 );
        for ( int i = 0; i < 8; i++ ) {
            sb.append( (char) ('a' + r.nextInt( 26 )) );
        }
        return sb.toString();
    }

    private AcdpConfig loadAcdpConfig( String fileName ) throws URISyntaxException {
        URI endpoint = new URI( PropertiesUtil.getProperty( fileName, "acdp.admin.endpoint" ) );
        String username = PropertiesUtil.getProperty( fileName, "acdp.admin.username" );
        String password = PropertiesUtil.getProperty( fileName, "acdp.admin.password" );

        return new AcdpConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
    }
}
