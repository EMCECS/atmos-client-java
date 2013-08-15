/**
 * 
 */
package com.emc.vipr.transform.compression;

import java.io.OutputStream;
import java.util.Map;

import com.emc.vipr.transform.OutputTransform;

/**
 * @author cwikj
 *
 */
public class CompressionOutputTransform extends OutputTransform {

    /**
     * @param streamToEncode
     * @param metadataToEncode
     */
    public CompressionOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode) {
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
