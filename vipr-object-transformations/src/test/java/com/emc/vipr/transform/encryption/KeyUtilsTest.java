package com.emc.vipr.transform.encryption;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(KeyUtilsTest.class);

    private Properties keyprops;
    private KeyPair masterKey;

    @Before
    public void setUp() throws Exception {
        // Load some keys.
        keyprops = new Properties();
        keyprops.load(this.getClass().getClassLoader()
                .getResourceAsStream("keys.properties"));

        masterKey = KeyUtils.rsaKeyPairFromBase64(
                keyprops.getProperty("masterkey.public"),
                keyprops.getProperty("masterkey.private"));
    }

    @Test
    public void testGetRsaPublicKeyFingerprint()
            throws NoSuchAlgorithmException {
        assertEquals("Key fingerprint invalid",
                "000317457b5645b7b5c4daf4cf6780c05438effd",
                KeyUtils.getRsaPublicKeyFingerprint((RSAPublicKey) masterKey
                        .getPublic()));
    }

    @Test
    public void testToHexPadded() {
        byte[] dataWithoutZeroes = new byte[] { 0x11, 0x22, 0x33 };
        assertEquals("Without zeroes incorrect", "112233",
                KeyUtils.toHexPadded(dataWithoutZeroes));
        byte[] dataWithLeadingZero = new byte[] { 0x01, 0x22, 0x33 };
        assertEquals("With leading zero incorrect", "012233",
                KeyUtils.toHexPadded(dataWithLeadingZero));
        byte[] dataWithLeadingZeroBytes = new byte[] { 0x00, 0x00, 0x11, 0x22,
                0x33 };
        assertEquals("Data with leading zero bytes incorrect", "0000112233",
                KeyUtils.toHexPadded(dataWithLeadingZeroBytes));

    }

    @Test
    public void testEncryptDecryptKey() throws NoSuchAlgorithmException {
        // Make an AES secret key
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey sk = kg.generateKey();
        logger.info("AES Key: " + KeyUtils.toHexPadded(sk.getEncoded()));
        
        String encryptedKey = KeyUtils.encryptKey(sk, null, masterKey);
        
        SecretKey sk2 = KeyUtils.decryptKey(encryptedKey, "AES", null, masterKey);
        
        assertEquals("Keys not equal", sk, sk2);
        assertArrayEquals("Key data not equal", sk.getEncoded(), sk2.getEncoded());
        
    }


}
