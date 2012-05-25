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

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.emc.acdp.api.impl.AcdpAdminApiClient;
import com.emc.cdp.services.rest.model.Identity;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.cdp.services.rest.model.Profile;

/**
 * @author cwikj
 * 
 */
public class IdentityTest {
    AcdpAdminApiClient acdp;
    AcdpAdminApiClient acdpAdmin;
    private String acdpEndpoint;
    private String acdpAdminUsername;
    private String acdpAdminPassword;
    private String acdpAdminEndpoint;

    public IdentityTest() {
        InputStream in = ClassLoader
                .getSystemResourceAsStream("atmos.properties");
        if (in != null) {
            try {
                System.getProperties().load(in);
            } catch (IOException e) {
                throw new RuntimeException("Could not load atmos.properties", e);
            }
        }

        acdpEndpoint = System.getProperty("acdp.endpoint");
        if (acdpEndpoint == null) {
            throw new RuntimeException(
                    "acdp.endpoint is null.  Set in atmos.properties or on command line with -Dacdp.endpoint");
        }
        
        acdpAdminUsername = System.getProperty("acdp.admin");
        if (acdpAdminUsername == null) {
            throw new RuntimeException(
                    "acdp.admin is null.  Set in atmos.properties or on command line with -Dacdp.admin");
        }
        acdpAdminPassword = System.getProperty("acdp.password");
        if (acdpEndpoint == null) {
            throw new RuntimeException(
                    "acdp.password is null.  Set in atmos.properties or on command line with -Dacdp.password");
        }
        acdpAdminEndpoint = System.getProperty("acdp.admin.endpoint");
        if (acdpAdminEndpoint == null) {
            throw new RuntimeException(
                    "acdp.admin.endpoint is null.  Set in acdp.admin.endpoint or on command line with -Dacdp.password");
        }
        


    }

    @Before
    public void setUp() {
        acdp = new AcdpAdminApiClient(acdpEndpoint);
        acdpAdmin = new AcdpAdminApiClient(acdpAdminEndpoint);
    }

    @Test
    public void testCreateIdentityNullProfile() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId(generateEmail());
        ident.setPassword(rand8char() + "!");
        try {
            acdp.createIdentity(ident);
            Assert.fail("Should have failed with profile required");
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void testCreateIdentityPartialProfile() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId(rand8char());
        ident.setPassword(rand8char() + "!");
        Profile p = of.createProfile();
        p.setFirstName(rand8char());
        p.setLastName(rand8char());
        p.setEmail(generateEmail());
        ident.setProfile(p);
        acdp.createIdentity(ident);
    }

    @Test
    public void testCreateIdentityNoPassword() {
        ObjectFactory of = new ObjectFactory();
        Identity ident = of.createIdentity();
        ident.setId(generateEmail());
        try {
            acdp.createIdentity(ident);
            Assert.fail("Should have failed with password required");
        } catch (Exception e) {
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

        ident.setId(id);
        ident.setPassword(rand8char() + "!");
        Profile p = of.createProfile();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        ident.setProfile(p);
        acdp.createIdentity(ident);
        
        acdpAdmin.adminLogin(acdpAdminUsername, acdpAdminPassword);

        Identity ident2 = acdpAdmin.adminGetIdentity(id);

        Assert.assertNotNull("Identity read back null", ident2);
        Assert.assertEquals("ID mismatch", id, ident2.getId());
        Assert.assertEquals("Name mismatch", firstName, ident2.getProfile()
                .getFirstName());
        Assert.assertEquals("Last name mismatch", lastName, ident2.getProfile()
                .getLastName());
        Assert.assertEquals("email mismatch", email, ident2.getProfile()
                .getEmail());
    }

    private String generateEmail() {
        return rand8char() + "@" + rand8char() + ".com";
    }

    protected String rand8char() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer(8);
        for (int i = 0; i < 8; i++) {
            sb.append((char) ('a' + r.nextInt(26)));
        }
        return sb.toString();
    }

}
