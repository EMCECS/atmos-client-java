package com.emc.acdp.api.test;

import com.emc.acdp.api.AcdpConfig;
import com.emc.acdp.api.AcdpException;
import com.emc.acdp.api.jaxrs.AcdpAdminApiClient;
import com.emc.util.PropertiesUtil;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class ErrorTest {
    private static final String DO_NOT_CREATE_THIS_ACCOUNT = "delete_this_account_immediately";

    AcdpAdminApiClient admin;

    @Before
    public void setUp() throws Exception {
        AcdpConfig adminConfig = loadAdminConfig( "acdp.properties" );
        admin = new AcdpAdminApiClient( adminConfig );
    }

    @Test
    public void testErrorParsing() {
        try {
            admin.getAccount( DO_NOT_CREATE_THIS_ACCOUNT );
            Assert.fail( "Test account should not exist, but does!" );
        } catch ( AcdpException e ) {
            Assert.assertNotNull( "ACDP code is null", e.getAcdpCode() );
        }
    }

    private AcdpConfig loadAdminConfig( String fileName ) throws URISyntaxException {
        URI endpoint = new URI( PropertiesUtil.getProperty( fileName, "acdp.admin.endpoint" ) );
        String username = PropertiesUtil.getProperty( fileName, "acdp.admin.username" );
        String password = PropertiesUtil.getProperty( fileName, "acdp.admin.password" );

        return new AcdpConfig( endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), username, password );
    }
}
