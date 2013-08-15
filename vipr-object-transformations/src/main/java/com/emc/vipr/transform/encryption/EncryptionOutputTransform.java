/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.OutputStream;
import java.util.Map;

import com.emc.vipr.transform.OutputTransform;

/**
 * @author cwikj
 *
 */
public abstract class EncryptionOutputTransform extends OutputTransform {

    public EncryptionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode) {
        super(streamToEncode, metadataToEncode);
    }

}
