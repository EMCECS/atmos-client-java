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

import com.emc.acdp.api.AcdpAdminConfig;
import com.emc.acdp.api.AcdpMgmtConfig;
import com.emc.acdp.api.jersey.AcdpAdminApiClient;
import com.emc.acdp.api.jersey.AcdpMgmtApiClient;
import com.emc.cdp.services.rest.model.Identity;
import com.emc.cdp.services.rest.model.IdentityList;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.cdp.services.rest.model.Profile;
import com.emc.util.PropertiesUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

/**
 * @author cwikj
 */
public class IdentityTest {
    AcdpMgmtApiClient mgmt;
    AcdpAdminApiClient admin;

    @Before
    public void setUp() throws Exception {
        try {
            mgmt = new AcdpMgmtApiClient( loadMgmtConfig( "acdp.properties" ) );
            admin = new AcdpAdminApiClient( loadAdminConfig( "acdp.properties" ) );
        } catch(Exception e) {
            Assume.assumeNoException("Loading acdp.properties failed", e);
        }

    }

    @Test
    public void testCreateIdentityNullProfile() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId( generateEmail() );
        ident.setPassword( rand8char() + "!" );
        try {
            mgmt.createIdentity( ident );
            Assert.fail( "Should have failed with profile required" );
        } catch ( Exception e ) {
            // OK
        }
    }

    @Test
    public void testListAllIdentities() {
        IdentityList list = admin.listIdentities( true, true, 1, 1000 );
        Assert.assertNotNull( list );
        Assert.assertTrue( list.getTotalResults() > 0 );
    }

    @Test
    public void testCreateIdentityPartialProfile() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId( rand8char() );
        ident.setPassword( rand8char() + "!" );
        Profile p = of.createProfile();
        p.setFirstName( rand8char() );
        p.setLastName( rand8char() );
        p.setEmail( generateEmail() );
        ident.setProfile( p );
        mgmt.createIdentity( ident );
    }

    @Test
    public void testCreateIdentityNoPassword() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId( generateEmail() );
        try {
            mgmt.createIdentity( ident );
            Assert.fail( "Should have failed with password required" );
        } catch ( Exception e ) {
            // OK
        }
    }

    @Test
    public void testCreateGetIdentity() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();

        String email = generateEmail();
        String id = rand8char();
        String firstName = rand8char();
        String lastName = rand8char();

        ident.setId( id );
        ident.setPassword( rand8char() + "!" );
        Profile p = of.createProfile();
        p.setFirstName( firstName );
        p.setLastName( lastName );
        p.setEmail( email );
        ident.setProfile( p );
        mgmt.createIdentity( ident );

        Identity ident2 = admin.getIdentity( id );

        Assert.assertNotNull( "Identity read back null", ident2 );
        Assert.assertEquals( "ID mismatch", id, ident2.getId() );
        Assert.assertEquals( "Name mismatch", firstName, ident2.getProfile()
                                                               .getFirstName() );
        Assert.assertEquals( "Last name mismatch", lastName, ident2.getProfile()
                                                                   .getLastName() );
        Assert.assertEquals( "email mismatch", email, ident2.getProfile()
                                                            .getEmail() );
    }

    @Test
    public void testGetIdentity() {
        String id = "christopher.arnett@emc.com";
        Identity ident = admin.getIdentity( id );
        Assert.assertNotNull( "Identity is null", ident );
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

    private AcdpMgmtConfig loadMgmtConfig( String fileName ) throws URISyntaxException {
        URI endpoint = new URI( PropertiesUtil.getProperty( fileName, "acdp.mgmt.endpoint" ) );
        String username = PropertiesUtil.getProperty( fileName, "acdp.mgmt.username" );
        String password = PropertiesUtil.getProperty( fileName, "acdp.mgmt.password" );

        return new AcdpMgmtConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
    }

    private AcdpAdminConfig loadAdminConfig( String fileName ) throws URISyntaxException {
        URI endpoint = new URI( PropertiesUtil.getProperty( fileName, "acdp.admin.endpoint" ) );
        String username = PropertiesUtil.getProperty( fileName, "acdp.admin.username" );
        String password = PropertiesUtil.getProperty( fileName, "acdp.admin.password" );

        return new AcdpAdminConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
    }
}
