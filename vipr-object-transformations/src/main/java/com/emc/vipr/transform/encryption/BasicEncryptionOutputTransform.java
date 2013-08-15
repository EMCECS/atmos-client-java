/**
 * 
 */
package com.emc.vipr.transform.encryption;

import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Map;

/**
 * @author cwikj
 *
 */
public class BasicEncryptionOutputTransform extends EncryptionOutputTransform {

    /**
     * @param streamToEncode
     * @param metadataToEncode
     */
    public BasicEncryptionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode, KeyPair asymmetricKey) {
        super(streamToEncode, metadataToEncode);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.OutputTransform#getEncodedOutputStream()
     */
    @Override
    public OutputStream getEncodedOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.OutputTransform#getEncodedMetadata()
     */
    @Override
    public Map<String, String> getEncodedMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

}
