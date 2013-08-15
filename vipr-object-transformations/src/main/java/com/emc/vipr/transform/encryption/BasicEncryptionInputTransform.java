/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.InputStream;
import java.security.KeyPair;
import java.util.Map;

/**
 * @author cwikj
 *
 */
public class BasicEncryptionInputTransform extends EncryptionInputTransform {

    /**
     * @param streamToDecode
     * @param metadataToDecode
     */
    public BasicEncryptionInputTransform(InputStream streamToDecode,
            Map<String, String> metadataToDecode, KeyPair asymmetricKey) {
        super(streamToDecode, metadataToDecode);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.InputTransform#decodeInputStream(java.io.InputStream)
     */
    @Override
    public InputStream getDecodedInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.InputTransform#decodeMetadata(java.util.Map)
     */
    @Override
    public Map<String, String> getDecodedMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

}
