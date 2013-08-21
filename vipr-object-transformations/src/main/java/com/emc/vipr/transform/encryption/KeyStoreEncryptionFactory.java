package com.emc.vipr.transform.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

public class KeyStoreEncryptionFactory extends
        EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> {

    public KeyStore keyStore;

    public String masterEncryptionKeyAlias;

    public char[] keyStorePassword;

    public KeyStoreEncryptionFactory(KeyStore keyStore,
            String masterEncryptionKeyAlias, char[] keyStorePassword) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        super();
        this.keyStore = keyStore;
        this.masterEncryptionKeyAlias = masterEncryptionKeyAlias;
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public Map<String, String> rekey(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTransformClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionOutputTransform getOutputTransform(
            OutputStream streamToEncode, Map<String, String> metadataToEncode)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionInputTransform getInputTransform(
            String transformConfig, InputStream streamToDecode,
            Map<String, String> metadata) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    
}