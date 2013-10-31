/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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

public class EncryptionConfig {
    private EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> factory;

    public EncryptionConfig(KeyStore keystore, char[] keystorePassword, 
            String masterKeyAlias, Provider provider, int keySize) 
                    throws InvalidKeyException, NoSuchAlgorithmException, 
                    NoSuchPaddingException, TransformException {
        if(provider == null) {
            factory = new KeyStoreEncryptionFactory(keystore, masterKeyAlias, keystorePassword);
        } else {
            factory = new KeyStoreEncryptionFactory(keystore, masterKeyAlias, keystorePassword, provider);
        }
        factory.setEncryptionSettings(TransformConstants.DEFAULT_ENCRYPTION_TRANSFORM, keySize, provider);
    }
    
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

    public EncryptionTransformFactory<BasicEncryptionOutputTransform, BasicEncryptionInputTransform> getFactory() {
        return factory;
    }

}
