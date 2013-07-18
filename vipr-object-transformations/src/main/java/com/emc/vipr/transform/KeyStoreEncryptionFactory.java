package com.emc.vipr.transform;

import char[];
import String;
import KeyStore;

public class KeyStoreEncryptionFactory extends EncryptionFactory {

  public KeyStore keyStore;

  public String masterEncryptionKeyAlias;

  public char[] keyStorePassword;

  public void KeyStoreEncryptionFactory(KeyStore keyStore, String masterEncryptionKeyAlias, char[] keyStorePassword) {
  }

}