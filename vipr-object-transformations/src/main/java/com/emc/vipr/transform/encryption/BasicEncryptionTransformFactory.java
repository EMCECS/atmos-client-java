package com.emc.vipr.transform.encryption;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public class BasicEncryptionTransformFactory extends
        EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> {

    public KeyPair masterEncryptionKey;
    private String masterEncryptionKeyFingerprint;
    private Map<String, KeyPair> masterDecryptionKeys;

    public void setMasterEncryptionKey(KeyPair pair) {
        this.masterEncryptionKey = pair;
        try {
            this.masterEncryptionKeyFingerprint = KeyUtils.getRsaPublicKeyFingerprint((RSAPublicKey)pair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error adding master key", e);
        }
        addMasterDecryptionKey(pair);
    }

    public void addMasterDecryptionKey(KeyPair pair) {
        try {
            String fingerprint = KeyUtils.getRsaPublicKeyFingerprint((RSAPublicKey)pair.getPublic());
            masterDecryptionKeys.put(fingerprint, pair);
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