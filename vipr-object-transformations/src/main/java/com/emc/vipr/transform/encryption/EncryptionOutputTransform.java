/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.OutputStream;
import java.security.Provider;
import java.util.Map;

import com.emc.vipr.transform.OutputTransform;

/**
 * @author cwikj
 * 
 */
public abstract class EncryptionOutputTransform extends OutputTransform {
    protected Provider provider;

    public EncryptionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode, String transformConfig,
            Provider provider) {
        super(streamToEncode, metadataToEncode, transformConfig);
        this.provider = provider;
    }


}
