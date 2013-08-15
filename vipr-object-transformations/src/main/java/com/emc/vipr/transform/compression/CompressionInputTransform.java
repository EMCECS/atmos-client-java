/**
 * 
 */
package com.emc.vipr.transform.compression;

import java.io.InputStream;
import java.util.Map;

import com.emc.vipr.transform.InputTransform;

/**
 * @author cwikj
 *
 */
public class CompressionInputTransform extends InputTransform {

    /**
     * @param streamToDecode
     * @param metadataToDecode
     */
    public CompressionInputTransform(InputStream streamToDecode,
            Map<String, String> metadataToDecode) {
        super(streamToDecode, metadataToDecode);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.InputTransform#decodeInputStream(java.io.InputStream)
     */
    @Override
    public InputStream decodeInputStream(InputStream in) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.emc.vipr.transform.InputTransform#decodeMetadata(java.util.Map)
     */
    @Override
    public Map<String, String> decodeMetadata(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

}
