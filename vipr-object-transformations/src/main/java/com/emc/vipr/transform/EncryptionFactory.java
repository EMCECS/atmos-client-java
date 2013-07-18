package com.emc.vipr.transform;

import java.security.Provider;
import Map;

public abstract class EncryptionFactory extends TransformFactory {

  public abstract Map rekey(Map metadata);

  public void setCryptoProvider(java.security.Provider provider) {
  }

}