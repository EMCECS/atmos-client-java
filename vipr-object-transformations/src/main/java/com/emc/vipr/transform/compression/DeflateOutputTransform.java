/**
 * 
 */
package com.emc.vipr.transform.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformConstants.CompressionMode;

/**
 * @author cwikj
 * 
 */
public class DeflateOutputTransform extends CompressionOutputTransform {

    private DeflateOutputStream deflater;

    /**
     * @param streamToEncode
     * @param metadataToEncode
     * @throws IOException
     */
    public DeflateOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode, int compressionLevel)
            throws IOException {
        super(streamToEncode, metadataToEncode,
                TransformConstants.COMPRESSION_CLASS + ":"
                        + CompressionMode.Deflate + "/" + compressionLevel);

        if (compressionLevel > 9 || compressionLevel < 0) {
            throw new IllegalArgumentException(
                    "Invalid Deflate compression level: " + compressionLevel);
        }

        deflater = new DeflateOutputStream(streamToEncode, compressionLevel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.vipr.transform.OutputTransform#getEncodedOutputStream()
     */
    @Override
    public OutputStream getEncodedOutputStream() {
        return deflater;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.vipr.transform.OutputTransform#getEncodedMetadata()
     */
    @Override
    public Map<String, String> getEncodedMetadata() {
        Map<String, String> metadata = new HashMap<String, String>();

        // Merge stream metadata
        metadata.putAll(deflater.getStreamMetadata());

        // Merge original metadata
        metadata.putAll(metadataToEncode);

        return metadata;
    }

}
