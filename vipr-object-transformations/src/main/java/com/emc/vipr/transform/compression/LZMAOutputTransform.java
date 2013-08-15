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
public class LZMAOutputTransform extends CompressionOutputTransform {
    private LZMAOutputStream lzmaOutput;

    public LZMAOutputTransform(OutputStream streamToEncode,
            Map<String, String> metadataToEncode, int level) throws IOException {
        super(streamToEncode, metadataToEncode,
                TransformConstants.COMPRESSION_CLASS + ":"
                        + CompressionMode.LZMA + "/" + level);

        if (level > 9 || level < 0) {
            throw new IllegalArgumentException("Invalid compression level "
                    + level);
        }

        lzmaOutput = new LZMAOutputStream(streamToEncode, level);
    }

    @Override
    public OutputStream getEncodedOutputStream() {
        return lzmaOutput;
    }

    @Override
    public Map<String, String> getEncodedMetadata() {
        Map<String, String> outputMetadata = new HashMap<String, String>();
        outputMetadata.putAll(lzmaOutput.getStreamMetadata());

        // Merge original
        outputMetadata.putAll(metadataToEncode);

        return outputMetadata;
    }

}
