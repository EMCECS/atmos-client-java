/**
 * 
 */
package com.emc.vipr.transform;

import java.io.InputStream;
import java.util.Map;

/**
 * @author cwikj
 *
 */
public abstract class InputTransform {

    protected InputStream streamToDecode;
    protected Map<String, String> metadataToDecode;

    /**
     * 
     */
    public InputTransform(InputStream streamToDecode, Map<String,String> metadataToDecode) {
        this.streamToDecode = streamToDecode;
        this.metadataToDecode = metadataToDecode;
    }

    /**
     * Wraps an input stream with another stream that will decode the inbound object
     * data stream.
     * @param in the inbound input stream.
     * @return the input stream wrapped with a decoder.
     */
    public abstract InputStream decodeInputStream(InputStream in);

    /**
     * Decodes the object's metadata.  Usually, this will simply return the object's
     * metadata.  However, in some circumstances this method could apply some
     * transformation to the metadata like decrypting it.
     * @param metadata the object's metadata
     * @return the decoded metadata.
     */
    public abstract Map<String, String> decodeMetadata(Map<String, String> metadata);
}
