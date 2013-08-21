/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.util.CloseCallback;
import com.emc.vipr.transform.util.CloseNotifyOutputStream;
import com.emc.vipr.transform.util.CountingOutputStream;

/**
 * @author cwikj
 * 
 */
public class BasicEncryptionOutputTransform extends EncryptionOutputTransform implements CloseCallback {
    byte[] iv;
    SecretKey k;
    private CipherOutputStream cipherStream;
    private DigestOutputStream digestStream;
    private CountingOutputStream counterStream;
    private byte[] digest;
    private CloseNotifyOutputStream notifyStream;
    private String masterEncryptionKeyFingerprint;
    private KeyPair masterKey;

    /**
     * @param streamToEncode
     * @param metadataToEncode
     * @param masterEncryptionKeyFingerprint
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public BasicEncryptionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode,
            String masterEncryptionKeyFingerprint, KeyPair asymmetricKey,
            String encryptionTransform, int keySize, Provider provider) {
        super(streamToEncode, metadataToEncode,
                TransformConstants.ENCRYPTION_CLASS + ":" + encryptionTransform,
                provider);

        this.masterEncryptionKeyFingerprint = masterEncryptionKeyFingerprint;
        this.masterKey = asymmetricKey;
        
        try {
            Cipher cipher = null;
            if (provider != null) {
                cipher = Cipher.getInstance(encryptionTransform, provider);
            } else {
                cipher = Cipher.getInstance(encryptionTransform);
            }

            // Generate a secret key
            String[] algParts = encryptionTransform.split("/");
            KeyGenerator keygen = null;
            if (provider != null) {
                keygen = KeyGenerator.getInstance(algParts[0], provider);
            } else {
                keygen = KeyGenerator.getInstance(algParts[0]);
            }

            keygen.init(keySize);
            k = keygen.generateKey();

            cipher.init(Cipher.ENCRYPT_MODE, k);
            iv = cipher.getIV();

            MessageDigest sha1 = null;
            if (provider != null) {
                sha1 = MessageDigest.getInstance("SHA1", provider);
            } else {
                sha1 = MessageDigest.getInstance("SHA1");
            }

            // Build streams
            // CloseNotifyOutputStream->CountingOutputStream->DigestOutputStream->
            // CipherOutputStream->parent OutputStream
            cipherStream = new CipherOutputStream(streamToEncode, cipher);
            digestStream = new DigestOutputStream(cipherStream, sha1);
            counterStream = new CountingOutputStream(digestStream);
            notifyStream = new CloseNotifyOutputStream(counterStream, this);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error initializing output transform: "
                    + e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.vipr.transform.OutputTransform#getEncodedOutputStream()
     */
    @Override
    public OutputStream getEncodedOutputStream() {
        return notifyStream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.vipr.transform.OutputTransform#getEncodedMetadata()
     */
    @Override
    public Map<String, String> getEncodedMetadata() {
        if(!notifyStream.isClosed()) {
            throw new IllegalStateException("Cannot get encoded metadata until stream is closed");
        }
        Map<String, String> encodedMetadata = new HashMap<String, String>();
        
        encodedMetadata.putAll(metadataToEncode);
        
        // Add x-emc fields
        String encodedIv;
        try {
            encodedIv = new String(Base64.encodeBase64(iv), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode IV", e);
        }
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_IV, encodedIv);
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_KEY_ID, masterEncryptionKeyFingerprint);
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_OBJECT_KEY, KeyUtils.encryptKey(k, provider, masterKey));
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_UNENC_SHA1, KeyUtils.toHexPadded(digest));
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_UNENC_SIZE, ""+counterStream.getByteCount());
        
        // Sign x-emc fields.
        encodedMetadata.put(TransformConstants.META_ENCRYPTION_META_SIG, 
                signMetadata(encodedMetadata, (RSAPrivateKey) masterKey.getPrivate()));
        
        return encodedMetadata;
    }

    @Override
    public void closed(Object what) {
        // Grab the digest (can only do this once)
        digest = digestStream.getMessageDigest().digest();
    }
}
