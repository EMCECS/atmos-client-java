package com.emc.vipr.transform.encryption;

import java.security.KeyStore;
import java.util.Map;

public class KeyStoreEncryptionFactory extends
        EncryptionTransformFactory<KeyStoreEncryptionTransformer> {

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
    public KeyStoreEncryptionTransformer getTransformer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStoreEncryptionTransformer getTransformer(String transformClass,
            String config, Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTransformClass() {
        // TODO Auto-generated method stub
        return null;
    }

}