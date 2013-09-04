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
package com.emc.esu.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.Test;

import com.emc.esu.sysmgmt.ListHostsResponse;
import com.emc.esu.sysmgmt.ListRmgResponse;
import com.emc.esu.sysmgmt.SysMgmtApi;
import com.emc.esu.sysmgmt.SysMgmtResponse;
import com.emc.esu.sysmgmt.pox.GetUidResponse;
import com.emc.esu.sysmgmt.pox.ListRmgResponsePox;

/**
 * @author cwikj
 *
 */
public class EsuSysMgmtApiTest {
	private static final Logger l4j = Logger.getLogger(EsuSysMgmtApiTest.class);
	
	private String proto;
	private String host;
	private int port;
	private String username;
	private String password;
	private SysMgmtApi api;

	public EsuSysMgmtApiTest() {
    	InputStream in = ClassLoader.getSystemResourceAsStream("atmos.properties");
    	if( in != null ) {
    		try {
				System.getProperties().load(in);
			} catch (IOException e) {
				throw new RuntimeException( "Could not load atmos.properties", e);
			}
    	}
    	
    	proto = System.getProperty("atmos.sysmgmt.proto");
    	Assume.assumeTrue("atmos.sysmgmt.proto is null", proto != null);
//    	if( proto == null ) {
//    		throw new RuntimeException( "atmos.sysmgmt.proto is null.  Set in atmos.properties or on command line with -Datmos.sysmgmt.proto" );
//    	}
    	host = System.getProperty( "atmos.sysmgmt.host" );
    	if( host == null ) {
    		throw new RuntimeException( "atmos.sysmgmt.host is null.  Set in atmos.properties or on command line with -Datmos.sysmgmt.host" );
    	}
    	port = Integer.parseInt( System.getProperty( "atmos.sysmgmt.port" ) );
    	
    	username = System.getProperty("atmos.sysmgmt.username");
    	password = System.getProperty("atmos.sysmgmt.password");
    	
    	api = new SysMgmtApi(proto, host, port, username, password);
    	
    	try {
			SysMgmtApi.disableCertificateValidation();
		} catch (Exception e) {
			throw new RuntimeException("Could not disable certificate validation");
		}
	}
	
	@Test
	public void testListRmgs() {
		ListRmgResponse resp = api.listRmgs();
		
		checkResponse(resp);
		
		Assert.assertNotNull("RMG list null", resp.getRmgs());
		Assert.assertTrue("Expected at least 1 RMG", resp.getRmgs().size()>0);
		for(ListRmgResponse.Rmg r : resp.getRmgs()) {
			l4j.debug("RMG:" + r);
			Assert.assertNotNull("RMG name null", r.getName());
			Assert.assertNotNull("RMG localtime null", r.getLocalTime());
			Assert.assertNotNull("RMG tostring null", r.toString());
		}
	}
	
	@Test
	public void testListHosts() {
		ListRmgResponse resp = api.listRmgs();
		
		checkResponse(resp);
		
		Assert.assertNotNull("RMG list null", resp.getRmgs());
		Assert.assertTrue("Expected at least 1 RMG", resp.getRmgs().size()>0);
		for(ListRmgResponse.Rmg r : resp.getRmgs()) {
			ListHostsResponse hresp = api.listHosts(r.getName());
			checkResponse(hresp);
			Assert.assertTrue("Expected at least two hosts in an RMG", hresp.getHosts().size()>1);
			for(ListHostsResponse.Host h : hresp.getHosts()) {
				l4j.debug("Host: " + h);
				Assert.assertNotNull("Host name null", h.getName());
				Assert.assertNotNull("location null", h.getLocation());
			}
		}
	}
	
	@Test
	public void testPoxLogin() throws Exception {
		api.poxLogin();
	}
	
	@Test
	public void testListRmgsPox() throws Exception {
		api.poxLogin();
		ListRmgResponsePox resp = api.listRmgsPox();
		
		Assert.assertNotNull("RMG list null", resp.getRmgs());
		Assert.assertTrue("Expected at least 1 RMG", resp.getRmgs().size()>0);
		
	}
	
	@Test
	public void testGetUidPox() throws Exception {
		api.poxLogin("Tenant1", "TenantAdmin", "password");
		GetUidResponse resp = api.getUidPox("zimbra", "zimbra");
		
		Assert.assertNotNull("UID response null", resp);
	}

	private void checkResponse(SysMgmtResponse resp) {
		Assert.assertNotNull("Response was null", resp);
		Assert.assertNotNull("Server date was null", resp.getServerDate());
		Assert.assertNotNull("Sysmgmt version was null", resp.getAtmosSysMgmtVersion());
	}
}
