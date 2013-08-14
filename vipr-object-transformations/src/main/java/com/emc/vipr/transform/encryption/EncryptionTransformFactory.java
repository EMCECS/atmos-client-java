package com.emc.vipr.transform.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformFactory;

/**
 * Base class for encryption transformation factories.
 * @author cwikj
 *
 * @param <T> the class of EncryptionTransformer that the factory will produce.
 */
public abstract class EncryptionTransformFactory<T extends EncryptionTransformer> extends TransformFactory<T> {
    protected Provider provider;
    protected String algorithm;
    
    /**
     * 
     * @param metadata
     * @return
     */
    public abstract Map<String, String> rekey(Map<String, String> metadata);

    public void setCryptoProvider(java.security.Provider provider) {
        this.provider = provider;
    }

    @Override
    public String getTransformClass() {
        return TransformConstants.ENCRYPTION_CLASS;
    }
    
    protected SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = null;
        if(provider != null) {
            kg = KeyGenerator.getInstance(algorithm, provider);
        } else {
            kg = KeyGenerator.getInstance(algorithm);
        }
        
        return kg.generateKey();
    }
}