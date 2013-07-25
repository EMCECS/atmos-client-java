package com.emc.vipr.transform;

import java.security.KeyPair;
import java.util.Map;

public class BasicEncryptionTransformFactory extends
        EncryptionTransformFactory<BasicEncryptionTransformer> {

    public KeyPair masterEncryptionKey;

    public Map<String, KeyPair> masterDecryptionKeys;

    public void setMasterEncryptionKey(KeyPair pair) {
    }

    public void addMasterDecryptionKey(KeyPair pair) {
    }

    @Override
    public Map<String, String> rekey(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionTransformer getTransformer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BasicEncryptionTransformer getTransformer(String transformClass,
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