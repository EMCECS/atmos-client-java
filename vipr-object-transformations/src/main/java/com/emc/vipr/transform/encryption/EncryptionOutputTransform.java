/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.vipr.transform.OutputTransform;
import com.emc.vipr.transform.TransformConstants;

/**
 * @author cwikj
 * 
 */
public abstract class EncryptionOutputTransform extends OutputTransform {
    private static final Logger logger = LoggerFactory
            .getLogger(EncryptionOutputTransform.class);
    protected Provider provider;

    public EncryptionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode, String transformConfig,
            Provider provider) {
        super(streamToEncode, metadataToEncode, transformConfig);
        this.provider = provider;
    }

    /**
     * 
     * @param metadata
     * @return
     */
    public String signMetadata(Map<String, String> metadata,
            RSAPrivateKey privateKey) {
        // Get the set of keys to sign and sort them.
        List<String> keys = new ArrayList<String>();

        for (String key : metadata.keySet()) {
            if (key.startsWith(TransformConstants.METADATA_PREFIX)) {
                keys.add(key);
            }
        }

        Collections.sort(keys, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                if (s1 == null && s2 == null) {
                    return 0;
                }
                if (s1 == null) {
                    return -s2.toLowerCase().compareTo(s1);
                }

                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }

        });

        StringBuffer canonicalString = new StringBuffer();
        for (String key : keys) {
            canonicalString.append(key.toLowerCase() + ":" + metadata.get(key)
                    + "\n");
        }

        logger.debug("Canonical string: '%s'", canonicalString);
        byte[] bytes;
        try {
            bytes = canonicalString.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen since UTF-8 is required.
            throw new RuntimeException("Could not render string to bytes");
        }

        Signature sig = null;
        try {
            if (provider != null) {
                sig = Signature.getInstance(
                        TransformConstants.METADATA_SIGNATURE_ALGORITHM,
                        provider);
            } else {
                sig = Signature
                        .getInstance(TransformConstants.METADATA_SIGNATURE_ALGORITHM);
            }
            sig.initSign(privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Could not initialize signature algorithm: " + e, e);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(
                    "Could not initialize signature algorithm: " + e, e);
        }
        
        // Sign it!
        try {
            sig.update(bytes);
            byte[] signature = sig.sign();
            
            // Base-64
            byte[] b64sig = Base64.encodeBase64(signature);
            
            return new String(b64sig, "US-ASCII");
        } catch (SignatureException e) {
            throw new RuntimeException("Could not compute metadata signature: " + e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode metadata signature: " + e);
        }

    }

}
