package com.emc.vipr.transform.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicEncryptionTransformFactory
        extends
        EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> {
    Logger logger = LoggerFactory
            .getLogger(BasicEncryptionTransformFactory.class);

    public KeyPair masterEncryptionKey;
    private String masterEncryptionKeyFingerprint;
    private Map<String, KeyPair> masterDecryptionKeys;

    public BasicEncryptionTransformFactory() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        super();
        masterDecryptionKeys = new HashMap<String, KeyPair>();
    }
    
    public BasicEncryptionTransformFactory(String encryptionTransform,
            int keySize, Provider provider) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        super(encryptionTransform, keySize, provider);
    }

    public void setMasterEncryptionKey(KeyPair pair) {
        if(!(pair.getPublic() instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("Only RSA KeyPairs are allowed, not " + pair.getPublic().getAlgorithm());
        }
        checkKeyLength(pair);
        this.masterEncryptionKey = pair;
        try {
            this.masterEncryptionKeyFingerprint = KeyUtils
                    .getRsaPublicKeyFingerprint((RSAPublicKey) pair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error adding master key", e);
        }
        addMasterDecryptionKey(pair);
    }

    public void addMasterDecryptionKey(KeyPair pair) {
        try {
            String fingerprint = KeyUtils
                    .getRsaPublicKeyFingerprint((RSAPublicKey) pair.getPublic());
            masterDecryptionKeys.put(fingerprint, pair);
            masterEncryptionKeyFingerprint = fingerprint;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error adding master key", e);
        }
    }

    @Override
    public Map<String, String> rekey(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionOutputTransform getOutputTransform(
            OutputStream streamToEncode, Map<String, String> metadataToEncode)
            throws IOException {
        return new BasicEncryptionOutputTransform(streamToEncode, 
                metadataToEncode, masterEncryptionKeyFingerprint, masterEncryptionKey, 
                encryptionTransform, keySize, provider);
    }

    @Override
    public BasicEncryptionInputTransform getInputTransform(
            String transformConfig, InputStream streamToDecode,
            Map<String, String> metadata) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Check for acceptable RSA key lengths. 1024-bit keys are not secure anymore
     * and 512-bit keys are unacceptable. Newer JDKs have already removed
     * support for the 512-bit keys and the 1024-bit keys may be removed in the
     * future:
     * http://mail.openjdk.java.net/pipermail/security-dev/2012-December/
     * 006195.html
     * 
     * @param pair
     */
    private void checkKeyLength(KeyPair pair) {
        // RSA key length is defined as the modulus of the public key
        int keySize = ((RSAPublicKey) pair.getPublic()).getModulus()
                .bitLength();
        if (keySize < 1024) {
            throw new IllegalArgumentException(
                    "The minimum RSA key size supported is 1024 bits. Your key is "
                            + keySize + " bits");
        } else if (keySize == 1024) {
            logger.warn("1024-bit RSA key detected. Support for 1024-bit RSA keys may soon be removed from the JDK. Please upgrade to a stronger key (e.g. 2048-bit).");
        }
    }

}