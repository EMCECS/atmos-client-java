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

import com.emc.acdp.api.AcdpAdminApi;
import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.jaxrs.AcdpAdminApiClient;
import com.emc.cdp.services.rest.model.Account;
import com.emc.cdp.services.rest.model.Attribute;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

/**
 * @author cwikj
 */
public class AccountTest {
    private static final Logger l4j = Logger.getLogger( AccountTest.class );

    AcdpAdminApi acdp;
    AcdpConfig config;

    public AccountTest() throws Exception {
        config = loadAcdpConfig( "acdp.properties" );
    }

    @Before
    public void setUp() {
        acdp = new AcdpAdminApiClient( config );

        // Create the identity if it doesn't exist.

    }

    @Test
    public void testCreateDeleteAccount() {
        ObjectFactory of = new ObjectFactory();
        Account acct = of.createAccount();
        acct.setName( "name1" );
        acct.setType( "web" );
        String accountId = acdp.createAccount( acct );
        l4j.debug( "Created account " + accountId );

        acdp.deleteAccount( accountId );
    }

    @Test
    public void testAccountCustomAttributes() {
        ObjectFactory of = new ObjectFactory();
        Account acct = of.createAccount();
        acct.setName( "name1" );
        acct.setType( "direct" );
        Attribute attr1 = of.createAttribute();
        attr1.setName( "myAccountType" );
        attr1.setValue( "enterprise" );
        Attribute attr2 = of.createAttribute();
        attr2.setName( "policyState" );
        attr2.setValue( "DR" );
        acct.getAttributes().add( attr1 );
        acct.getAttributes().add( attr2 );
        String accountId = acdp.createAccount( acct );

        Account acct2 = acdp.getAccount( accountId );

        Assert.assertEquals( "Account name wrong", acct.getName(), acct2.getName() );
        Assert.assertEquals( "Account type wrong", acct.getType(), acct2.getType() );
        validateAttribute( acct2.getAttributes(), attr1 );
        validateAttribute( acct2.getAttributes(), attr2 );

    }

    private void validateAttribute( List<Attribute> attributes, Attribute attr ) {
        for ( Attribute a : attributes ) {
            if ( a.getName().equals( attr.getName() ) ) {
                Assert.assertEquals( "Attribute " + a.getName() + " does not match", attr.getValue(), a.getValue() );
                return;
            }
        }
        Assert.fail( "Attribute " + attr.getName() + " does not exist" );
    }

    @Test
    public void testCreateDeleteAccountObj() throws Exception {
        // Create Account
        ObjectFactory of = new ObjectFactory();
        Account acct = of.createAccount();
        acct.setName( "name1" );
        acct.setType( "web" );

        String accountId = acdp.createAccount( acct );
        Assert.assertNotNull( "Account ID null", accountId );
        l4j.debug( "Created account " + accountId );

        // Delete account
        acdp.deleteAccount( accountId );
    }

    @Test
    public void testAssignAccountAdmin() throws Exception {
        // Create Account
        ObjectFactory of = new ObjectFactory();
        Account acct = of.createAccount();
        acct.setName( "name1" );
        acct.setType( "web" );

        String accountId = acdp.createAccount( acct );
        Assert.assertNotNull( "Account ID null", accountId );
        l4j.debug( "Created account " + accountId );

        // Assign the account admin
        // CreateAccountInvitationRequest cair = new
        // CreateAccountInvitationRequest(
        // r2.getAccountId(), accountAdmin, "account_manager",
        // r1.getAdminSessionId());
        // cair.setEndpoint(acdpEndpoint);
        // CreateAccountInvitationResponse r3 = cair.call();
        // Assert.assertTrue("Create account invite failed: " +
        // r3.getErrorMessage(),
        // r3.isSuccessful());
        // Assert.assertNotNull("Account invite ID null", r3.getInvitationId());
        // l4j.debug("Created account invite " + r3.getInvitationId());
        acdp.addAccountAssignee( accountId,
                                 rand8char(),
                                 rand8char() + "!",
                                 rand8char(),
                                 rand8char(),
                                 generateEmail(),
                                 "account_manager" );

//        EditAccountIdentityRequest eair = new EditAccountIdentityRequest(
//                r2.getAccountId(), accountAdmin, "account_manager",
//                r1.getAdminSessionId());
//        eair.setEndpoint(acdpEndpoint);
//        AcdpResponse r3 = eair.call();
//        Assert.assertTrue(
//                "Edit account idenitity failed: " + r3.getErrorMessage(),
//                r3.isSuccessful());

        // Delete account
        acdp.deleteAccount( accountId );
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
