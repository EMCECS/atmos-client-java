package com.emc.vipr.transform.encryption;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

public class KeyStoreEncryptionFactory extends
        EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> {

    public KeyStore keyStore;

    public String masterEncryptionKeyAlias;

    public char[] keyStorePassword;

    public KeyStoreEncryptionFactory(KeyStore keyStore,
            String masterEncryptionKeyAlias, char[] keyStorePassword) {
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
            InputStream streamToEncode, Map<String, String> metadataToEncode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionInputTransform getInputTransform(
            String transformClass, String config, Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

}