/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.InputStream;
import java.util.Map;

import com.emc.vipr.transform.InputTransform;

/**
 * @author cwikj
 *
 */
public abstract class EncryptionInputTransform extends InputTransform {

    public EncryptionInputTransform(InputStream streamToDecode,
            Map<String, String> metadataToDecode) {
        super(streamToDecode, metadataToDecode);
        // TODO Auto-generated constructor stub
    }

}
