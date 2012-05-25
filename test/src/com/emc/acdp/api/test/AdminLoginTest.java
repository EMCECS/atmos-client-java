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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.emc.acdp.api.impl.AcdpAdminApiClient;
import com.emc.acdp.api.request.AdminLoginRequest;
import com.emc.acdp.api.response.AdminLoginResponse;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class AdminLoginTest {
    AcdpAdminApiClient acdp;
    private String acdpEndpoint;
    private String acdpAdmin;
    private String acdpAdminPassword;

    public AdminLoginTest() {
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
        acdpAdmin = System.getProperty("acdp.admin");
        if (acdpAdmin == null) {
            throw new RuntimeException(
                    "acdp.admin is null.  Set in atmos.properties or on command line with -Dacdp.admin");
        }
        acdpAdminPassword = System.getProperty("acdp.password");
        if (acdpEndpoint == null) {
            throw new RuntimeException(
                    "acdp.password is null.  Set in atmos.properties or on command line with -Dacdp.password");
        }

    }

    @Before
    public void setUp() {
        acdp = new AcdpAdminApiClient(acdpEndpoint);
    }

    @Test
    public void testAdminLogin() {
        acdp.adminLogin(acdpAdmin, acdpAdminPassword);
    }

    @Test
    public void testAdminLoginObj() {
        AdminLoginRequest req = new AdminLoginRequest(acdpAdmin,
                acdpAdminPassword);
        req.setEndpoint(acdpEndpoint);

        AdminLoginResponse r;
        try {
            r = req.call();
        } catch (Exception e) {
            if (e instanceof EsuException) {
                throw (EsuException) e;
            } else {
                throw new EsuException("Error executing request: "
                        + e.getMessage(), e);
            }
        }

        Assert.assertTrue("Success should be true", r.isSuccessful());
        Assert.assertNotNull("Admin session null", r.getAdminSessionId());

    }
}
