package com.emc.vipr.transform.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformFactory;

/**
 * Base class for encryption transformation factories.
 * 
 * @author cwikj
 * 
 * @param <T>
 *            the class of EncryptionTransformer that the factory will produce.
 */
public abstract class EncryptionTransformFactory<T extends EncryptionOutputTransform, U extends EncryptionInputTransform>
        extends TransformFactory<T, U> {
    protected Provider provider;

    protected String encryptionTransform;
    protected int keySize;

    public EncryptionTransformFactory() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        this(TransformConstants.DEFAULT_ENCRYPTION_TRANSFORM,
                TransformConstants.DEFAULT_ENCRYPTION_KEY_SIZE, null);
    }

    public EncryptionTransformFactory(String encryptionTransform, int keySize,
            Provider provider) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        setEncryptionSettings(encryptionTransform, keySize, provider);
    }

    public void setEncryptionSettings(String transform, int keySize,
            Provider provider) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Check it first.
        if (provider != null) {
            Cipher.getInstance(transform, provider);
        } else {
            Cipher.getInstance(transform);
        }

        if (keySize > Cipher.getMaxAllowedKeyLength(transform)) {
            throw new InvalidKeyException("Key size of " + keySize
                    + " bits is larger than the maximum allowed of "
                    + Cipher.getMaxAllowedKeyLength(transform));
        }
        
        // OK, accept settings.
        this.encryptionTransform = transform;
        this.keySize = keySize;
        if(provider != null) {
            this.provider = provider;
        }
    }

    public static int getMaxKeySize(String transform)
            throws NoSuchAlgorithmException {
        return Cipher.getMaxAllowedKeyLength(transform);
    }

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
        if (provider != null) {
            kg = KeyGenerator.getInstance(getEncryptionAlgorithm(), provider);
        } else {
            kg = KeyGenerator.getInstance(getEncryptionAlgorithm());
        }

        return kg.generateKey();
    }

    protected String getEncryptionAlgorithm() {
        return encryptionTransform.split("/")[0];
    }

}