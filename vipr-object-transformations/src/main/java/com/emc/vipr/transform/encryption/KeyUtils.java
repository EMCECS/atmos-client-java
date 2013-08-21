package com.emc.vipr.transform.encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

/**
 * @author cwikj
 *
 */
public class KeyUtils {
    
    /**
     * Computes the fingerprint of an RSA public key.  This should be equivalent to the
     * Subject Key Identifier (SKI) of a key pair stored in an X.509 certificate.  This
     * is done by DER encoding the public key and computing the SHA1 digest of the
     * encoded key.
     * @param pubKey the RSA public key to fingerprint
     * @return the key's fingerprint as a string of hexadecimal characters.
     * @throws NoSuchAlgorithmException if the SHA1 algorithm could not be initialized.
     */
    public static String getRsaPublicKeyFingerprint(RSAPublicKey pubKey) 
            throws NoSuchAlgorithmException {
        byte[] pubkeyEnc = derEncodeRSAPublicKey(pubKey);
        MessageDigest sha1 = MessageDigest.getInstance("sha1");
        byte[] pubkeyDigest = sha1.digest(pubkeyEnc);
        
        return toHexPadded(pubkeyDigest);
    }

    
    /**
     * Transforms a byte sequence into a sequence of hex digits, MSB first.  The value
     * will be padded with zeroes to the proper number of digits.
     * @param data the bytes to encode into hex
     * @return the bytes as a string of hexadecimal characters.
     */
    public static String toHexPadded(byte[] data) {
        BigInteger bi = new BigInteger(1, data);
        String s = bi.toString(16);
        while(s.length() < (data.length*2)) {
            s = "0" + s;
        }
        
        return s;
    }

    /**
     * Encodes a {@link BigInteger} in DER format
     * @param value the value to encode
     * @return the byte sequence representing the number in DER encoding.
     */
    public static byte[] derEncodeBigInteger(BigInteger value) {
        return derEncodeValue((byte)0x02, value.toByteArray());
    }
    
    /**
     * Encodes a DER value with the proper length specifier.
     * @param type the DER type specifier byte
     * @param bytes the bytes to encode
     * @return the input bytes prefixed with the DER type and length.
     */
    public static byte[] derEncodeValue(byte type, byte[] bytes) {
        if(bytes.length < 128) {
            byte[] der = new byte[bytes.length + 2];
            der[0] = type; // Integer
            der[1] = (byte) bytes.length;
            System.arraycopy(bytes, 0, der, 2, bytes.length);
            return der;
        } else {
            BigInteger bigLength = BigInteger.valueOf(bytes.length);
            byte[] lengthBytes = bigLength.toByteArray();
            byte[] der = new byte[bytes.length + lengthBytes.length + 2];
            der[0] = type; // Integer
            der[1] = (byte) ((lengthBytes.length) | 0x80); // Length of Length
            System.arraycopy(lengthBytes, 0, der, 2, lengthBytes.length);
            System.arraycopy(bytes, 0, der, 2 + lengthBytes.length, bytes.length);
            return der;
        }
    }
    
    /**
     * Encodes an RSA public key in DER format.
     * @param pubkey the RSA public key to encode.
     * @return the public key's data in DER format.
     */
    public static byte[] derEncodeRSAPublicKey(RSAPublicKey pubkey) {
        List<byte[]> sequence = new ArrayList<byte[]>();
        sequence.add(derEncodeBigInteger(pubkey.getModulus()));
        sequence.add(derEncodeBigInteger(pubkey.getPublicExponent()));
        return derEncodeSequence(sequence);
    }
    
    /**
     * Encodes a list of objects into a DER "sequence".
     * @param objects the DER encoded objects to sequence.
     * @return the bytes representing the DER sequence.
     */
    public static byte[] derEncodeSequence(List<byte[]> objects) {
        int totalSize = 0;
        for(byte[] obj : objects) {
            totalSize += obj.length;
        }
        
        byte[] objectData = new byte[totalSize];
        int p = 0;
        for(byte[] obj : objects) {
            System.arraycopy(obj, 0, objectData, p, obj.length);
            p+=obj.length;
        }
        
        return derEncodeValue((byte)0x30, objectData);
    }
    
    /**
     * Constructs an RSA KeyPair from base-64 encoded key material.
     * @param publicKey The Base-64 encoded RSA public key in X.509 format.
     * @param privateKey The Base-64 encoded RSA private key in PKCS#8 format.
     * @return the KeyPair object containing both keys.
     */
    public static KeyPair rsaKeyPairFromBase64(String publicKey, String privateKey) {
        try {
            byte[] pubKeyBytes = Base64.decodeBase64(publicKey.getBytes("US-ASCII"));
            byte[] privKeyBytes = Base64.decodeBase64(privateKey.getBytes("US-ASCII"));
            
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            
            PublicKey pubKey;
            PrivateKey privKey;
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(pubKeySpec);
            privKey = keyFactory.generatePrivate(privKeySpec);
            
            return new KeyPair(pubKey, privKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not load key pair: " + e, e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not load key pair: " + e, e);
        }

    }

}
