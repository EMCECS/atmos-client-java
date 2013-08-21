package com.emc.vipr.transform.encryption;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.NoSuchPaddingException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.vipr.transform.TransformConstants;

public class BasicEncryptionTransformFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(BasicEncryptionTransformFactoryTest.class);

    private Properties keyprops;
    private KeyPair masterKey;
    private KeyPair oldKey;

    @Before
    public void setUp() throws Exception {
        // Load some keys.
        keyprops = new Properties();
        keyprops.load(this.getClass().getClassLoader()
                .getResourceAsStream("keys.properties"));
        
        masterKey = KeyUtils.rsaKeyPairFromBase64(keyprops.getProperty("masterkey.public"), keyprops.getProperty("masterkey.private"));
        oldKey = KeyUtils.rsaKeyPairFromBase64(keyprops.getProperty("oldkey.public"), keyprops.getProperty("oldkey.private"));
    }
    
    @Test
    public void testRejectSmallKey() throws Exception {
        // An RSA key < 1024 bits should be rejected as a master key.
        BasicEncryptionTransformFactory factory = new BasicEncryptionTransformFactory();
        KeyPair smallKey = null;
        try {
            smallKey = KeyUtils.rsaKeyPairFromBase64(keyprops.getProperty("smallkey.public"), keyprops.getProperty("smallkey.private"));
        } catch(Exception e) {
            // Good!
            logger.info("Key was properly rejected by JVM: " + e);
            return;
        }
        
        try {
            factory.setMasterEncryptionKey(smallKey);
        } catch(Exception e) {
            // Good!
            logger.info("Key was properly rejected by factory: " + e);
            return;
        }
        
        fail("RSA key < 1024 bits should have been rejected by factory");
    }

    @Test
    public void testRekey() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetMasterEncryptionKey() throws Exception {
        BasicEncryptionTransformFactory factory = new BasicEncryptionTransformFactory();
        factory.setMasterEncryptionKey(masterKey);
    }

    @Test
    public void testAddMasterDecryptionKey() throws Exception {
        BasicEncryptionTransformFactory factory = new BasicEncryptionTransformFactory();
        factory.setMasterEncryptionKey(masterKey);
        factory.addMasterDecryptionKey(oldKey);
    }

    @Test
    public void testGetOutputTransform() throws Exception {
        BasicEncryptionTransformFactory factory = new BasicEncryptionTransformFactory();
        factory.setMasterEncryptionKey(masterKey);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("name1", "value1");
        metadata.put("name2", "value2");
        BasicEncryptionOutputTransform outTransform = 
                factory.getOutputTransform(out, metadata);
        
        // Get some data to encrypt.
        InputStream classin = this.getClass().getClassLoader()
                .getResourceAsStream("uncompressed.txt");
        ByteArrayOutputStream classByteStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int c = 0;
        while ((c = classin.read(buffer)) != -1) {
            classByteStream.write(buffer, 0, c);
        }
        byte[] uncompressedData = classByteStream.toByteArray();
        classin.close();

        OutputStream encryptedStream = outTransform.getEncodedOutputStream();
        encryptedStream.write(uncompressedData);
        
        // Should not allow this yet.
        try {
            outTransform.getEncodedMetadata();
            fail("Should not be able to get encoded metadata until stream is closed");
        } catch(IllegalStateException e) {
            // OK.
        }
        
        encryptedStream.close();
        
        Map<String, String> objectData = outTransform.getEncodedMetadata();
        
        assertEquals("Uncompressed digest incorrect", "027e997e6b1dfc97b93eb28dc9a6804096d85873",
                objectData.get(TransformConstants.META_ENCRYPTION_UNENC_SHA1));
        assertEquals("Uncompressed size incorrect", 2516125, Long.parseLong(objectData
                .get(TransformConstants.META_ENCRYPTION_UNENC_SIZE)));
        assertNotNull("Missing IV", objectData.get(TransformConstants.META_ENCRYPTION_IV));
        assertEquals("Incorrect master encryption key ID", 
                KeyUtils.getRsaPublicKeyFingerprint((RSAPublicKey) masterKey.getPublic()),
                objectData.get(TransformConstants.META_ENCRYPTION_KEY_ID));
        assertNotNull("Missing object key", 
                objectData.get(TransformConstants.META_ENCRYPTION_OBJECT_KEY));
        assertNotNull("Missing metadata signature", 
                objectData.get(TransformConstants.META_ENCRYPTION_META_SIG));
        assertEquals("name1 incorrect", "value1", objectData.get("name1"));
        assertEquals("name2 incorrect", "value2", objectData.get("name2"));

        
        String transformConfig = outTransform.getTransformConfig();
        assertEquals("Transform config string incorrect", "ENC:AES/CBC/PKCS5Padding", transformConfig);

        logger.info("Encoded metadata: " + objectData);
        
    }

    @Test
    public void testGetInputTransform() {
        fail("Not yet implemented");
    }
    
    /**
     * Test the rejection of a master KeyPair that's not an RSA key (e.g. a DSA key)
     */
    @Test
    public void testRejectNonRsaMasterKey() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("DSA");
        keyGenerator.initialize(512, new SecureRandom());
        KeyPair myKeyPair = keyGenerator.generateKeyPair();
        
        BasicEncryptionTransformFactory factory = new BasicEncryptionTransformFactory();
        
        try {
            factory.setMasterEncryptionKey(myKeyPair);
        } catch(Exception e) {
            // Good!
            logger.info("DSA key was properly rejected by factory: " + e);
            return;
        }
        fail("DSA keys should not be allowed.");
    }
    
    /**
     * Test encrypting with one key, changing the master encryption key, then decrypting.
     * The old key should be found and used as the decryption key.
     */
    @Test
    public void testKeyRotation() {
        
    }
    
    

}
