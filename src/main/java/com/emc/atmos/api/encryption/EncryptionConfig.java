/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.atmos.api.encryption;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformException;
import com.emc.vipr.transform.encryption.BasicEncryptionInputTransform;
import com.emc.vipr.transform.encryption.BasicEncryptionOutputTransform;
import com.emc.vipr.transform.encryption.BasicEncryptionTransformFactory;
import com.emc.vipr.transform.encryption.EncryptionTransformFactory;
import com.emc.vipr.transform.encryption.KeyStoreEncryptionFactory;

/**
 * Creates an encryption configuration for use with the {@link AtmosEncryptionClient}.
 * Both keystore keys and bare RSA KeyPairs are supported.
 */
public class EncryptionConfig {
    private EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> factory;

    /**
     * Creates a new EncryptionConfig object that will retrieve keys from a Keystore
     * object.
     * @param keystore the Keystore containing the master encryption key and any
     * additional decryption key(s).
     * @param masterKeyPassword password for the master keys.  Note that this
     * implementation assumes that all master keys use the same password.
     * @param masterKeyAlias name of the master encryption key in the Keystore object.
     * @param provider (optional) if not-null, the Provider object to use for all 
     * encryption operations.  If null, the default provider(s) will be used from your
     * java.security file.
     * @param keySize size of encryption key to use, either 128 or 256.  Note that to use
     * 256-bit AES keys, you will probably need the unlimited strength jurisdiction files
     * installed in your JRE. 
     * @throws InvalidKeyException if the master encryption key cannot be loaded.
     * @throws NoSuchAlgorithmException if the AES encryption algorithm is not available.
     * @throws NoSuchPaddingException if PKCS5Padding is not available.
     * @throws TransformException if some other error occurred initializing the encryption.
     */
    public EncryptionConfig(KeyStore keystore, char[] masterKeyPassword, 
            String masterKeyAlias, Provider provider, int keySize) 
                    throws InvalidKeyException, NoSuchAlgorithmException, 
                    NoSuchPaddingException, TransformException {
        if(provider == null) {
            factory = new KeyStoreEncryptionFactory(keystore, masterKeyAlias, masterKeyPassword);
        } else {
            factory = new KeyStoreEncryptionFactory(keystore, masterKeyAlias, masterKeyPassword, provider);
        }
        factory.setEncryptionSettings(TransformConstants.DEFAULT_ENCRYPTION_TRANSFORM, keySize, provider);
    }
    
    /**
     * Creates a new EncryptionConfig object that uses bare KeyPair objects.
     * @param masterEncryptionKey the KeyPair to use for encryption.
     * @param decryptionKeys (optional) additional KeyPair objects available to 
     * decrypt objects.
     * @param provider (optional) if not-null, the Provider object to use for all 
     * encryption operations.  If null, the default provider(s) will be used from your
     * java.security file.
     * @param keySize size of encryption key to use, either 128 or 256.  Note that to use
     * 256-bit AES keys, you will probably need the unlimited strength jurisdiction files
     * installed in your JRE. 
     * @throws InvalidKeyException if the master encryption key is not valid
     * @throws NoSuchAlgorithmException if the AES encryption algorithm is not available.
     * @throws NoSuchPaddingException if PKCS5Padding is not available.
     * @throws TransformException if some other error occurred initializing the encryption.
     */
    public EncryptionConfig(KeyPair masterEncryptionKey, Set<KeyPair> decryptionKeys, 
            Provider provider, int keySize)
                    throws InvalidKeyException, NoSuchAlgorithmException, 
                    NoSuchPaddingException, TransformException {

        if(provider == null) {
            factory = new BasicEncryptionTransformFactory(masterEncryptionKey, decryptionKeys);
        } else {
            factory = new BasicEncryptionTransformFactory(masterEncryptionKey, decryptionKeys, provider);
        }
        factory.setEncryptionSettings(TransformConstants.DEFAULT_ENCRYPTION_TRANSFORM, keySize, provider);
    }

    /**
     * Returns the configured EncryptionTransformFactory.
     * @return the configured EncryptionTransformFactory.
     */
    public EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> getFactory() {
        return factory;
    }

}
