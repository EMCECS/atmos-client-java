package com.emc.atmos.api.test;

import com.emc.atmos.api.AtmosConfig;
import com.emc.atmos.api.jersey.EsuRestApiJerseyAdapter;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.rest.AbstractEsuRestApi;
import com.emc.esu.test.EsuApiTest;
import com.emc.util.PropertiesUtil;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class EsuRestApiAdapterTest extends EsuApiTest {
    private AtmosConfig config;

    public EsuRestApiAdapterTest() throws Exception {
        config = loadAtmosConfig( "atmos.properties" );
        uid = config.getTokenId();
        config.setDisableSslValidation( true );
        esu = new EsuRestApiJerseyAdapter( config );
    }

    /**
     * Test handling signature failures.  Should throw an exception with
     * error code 1032.
     */
    @Test
    public void testSignatureFailure() throws Exception {
        byte[] goodSecret = config.getSecretKey();
        String secretStr = new String( Base64.encodeBase64( goodSecret ), "UTF-8" );
        byte[] badSecret = Base64.decodeBase64( secretStr.toUpperCase().getBytes( "UTF-8" ) );
        try {
            // Fiddle with the secret key
            config.setSecretKey( badSecret );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch ( EsuException e ) {
            Assert.assertEquals( "Expected error code 1032 for signature failure",
                                 1032, e.getAtmosCode() );
        } finally {
            config.setSecretKey( goodSecret );
        }
    }

    /**
     * Test general HTTP errors by generating a 404.
     */
    @Test
    public void testFourOhFour() throws Exception {
        String goodContext = config.getContext();
        try {
            // Fiddle with the context
            config.setContext( "/restttttttttt" );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch ( EsuException e ) {
            Assert.assertEquals( "Expected error code 404 for bad context root",
                                 404, e.getHttpCode() );
        } finally {
            config.setContext( goodContext );
        }
    }

    @Test
    public void testServerOffset() throws Exception {
        long offset = ((AbstractEsuRestApi) esu).calculateServerOffset();
        l4j.info( "Server offset: " + offset + " milliseconds" );
    }

    private AtmosConfig loadAtmosConfig( String fileName ) throws URISyntaxException {
        String[] endpoints = PropertiesUtil.getProperty( fileName, "atmos.endpoints" ).split( "," );
        List<URI> uris = new ArrayList<URI>();
        for ( String endpoint : endpoints ) {
            uris.add( new URI( endpoint ) );
        }
        String tokenId = PropertiesUtil.getProperty( fileName, "atmos.uid" );
        String secretKey = PropertiesUtil.getProperty( fileName, "atmos.secret" );

        return new AtmosConfig( tokenId, secretKey, uris.toArray( new URI[uris.size()] ) );
    }
}
