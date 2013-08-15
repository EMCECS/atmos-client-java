package com.emc.vipr.transform;

import java.io.OutputStream;
import java.util.Map;

public abstract class OutputTransform {

    protected OutputStream streamToEncode;
    protected Map<String, String> metadataToEncode;
    private String transformConfig;

    public OutputTransform(OutputStream streamToEncode, Map<String, String> metadataToEncode, String transformConfig) {
        this.streamToEncode = streamToEncode;
        this.metadataToEncode = metadataToEncode;
        this.transformConfig = transformConfig;
    }
    
    /**
     * Wraps the output stream with an encoder that will apply this transformation to
     * the stream.
     * @return a new output stream object that encodes the source stream.
     */
    public abstract OutputStream getEncodedOutputStream();

    /**
     * Encodes the object's metadata. Usually, this is called
     * after the output stream has been closed to get the updated object
     * metadata but could also somehow transform existing metadata like encrypting it.
     * @return the "encoded" metadata
     */
    public abstract Map<String, String> getEncodedMetadata();

    /**
     * Gets this transformation's mode string, e.g. "COMP:LZMA/3"
     * @return the mode string.
     */
    public String getTransformConfig() {
        return transformConfig;
    }

}
