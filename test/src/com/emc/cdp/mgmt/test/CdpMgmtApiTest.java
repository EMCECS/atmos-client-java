package com.emc.cdp.mgmt.test;

import com.emc.cdp.mgmt.CdpMgmtApi;
import com.emc.cdp.mgmt.bean.Account;
import com.emc.cdp.mgmt.bean.AccountList;
import com.emc.cdp.mgmt.bean.Assignee;
import com.emc.cdp.mgmt.bean.Subtenant;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class CdpMgmtApiTest {
    private static final String PROPERTIES_FILE = "cdp.properties";

    private CdpMgmtApi api;

    public CdpMgmtApiTest() {
        InputStream in = ClassLoader.getSystemResourceAsStream( PROPERTIES_FILE );
        if ( in != null ) {
            try {
                System.getProperties().load( in );
            } catch ( IOException e ) {
                throw new RuntimeException( "Could not load " + PROPERTIES_FILE, e );
            }
        }

        String protocol = getProperty( "cdp.mgmt.protocol" );
        String host = getProperty( "cdp.mgmt.host" );
        int port = Integer.parseInt( getProperty( "cdp.mgmt.port" ) );
        String username = getProperty( "cdp.mgmt.username" );
        String password = getProperty( "cdp.mgmt.password" );

        api = new CdpMgmtApi( protocol, host, port, username, password );
    }

    @Test
    public void testLogin() throws Exception {
        api.login();
    }

    @Test
    public void testListAccounts() {
        AccountList accountList = api.listAccounts( true ).getAccountList();
        Assert.assertNotNull( accountList );
        Assert.assertNotNull( accountList.getAccounts() );
        Assert.assertTrue( accountList.getAccounts().size() > 0 );
    }

    @Test
    public void testGetAccount() {
        Account account = api.getAccount( "A03650427926", true ).getAccount();
        Assert.assertNotNull( account );
    }

    @Test
    public void testGetAccountIdentity() {
        Assignee assignee = api.getAccountAssignee( "A03650427926", "christopher.arnett@emc.com", true ).getAssignee();
        Assert.assertNotNull( assignee );
    }

    @Test
    public void testGetSubtenant() {
        Subtenant subtenant = api.getSubtenant( "A03650427926", "A03650427926-storageservice01" ).getSubtenant();
        Assert.assertNotNull( subtenant );
    }

    private String getProperty( String key ) {
        String value = System.getProperty( key );
        if ( value == null )
            throw new RuntimeException( key + " is null.  Set in " + PROPERTIES_FILE + " or on command line with -D" + key );
        return value;
    }
}
