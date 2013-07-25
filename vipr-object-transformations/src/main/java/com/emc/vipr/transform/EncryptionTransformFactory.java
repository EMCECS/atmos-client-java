package com.emc.vipr.transform;

import java.security.Provider;
import java.util.Map;

public abstract class EncryptionTransformFactory<T extends EncryptionTransformer> extends TransformFactory<T> {
    protected Provider provider;

    public abstract Map<String, String> rekey(Map<String, String> metadata);

    public void setCryptoProvider(java.security.Provider provider) {
        this.provider = provider;
    }

}