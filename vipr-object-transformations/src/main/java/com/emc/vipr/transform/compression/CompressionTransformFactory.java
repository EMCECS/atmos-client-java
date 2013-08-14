package com.emc.vipr.transform.compression;

import java.util.Map;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformFactory;
import com.emc.vipr.transform.TransformConstants.CompressionMode;

public class CompressionTransformFactory extends
        TransformFactory<CompressionTransformer> {

    public CompressionMode compressMode = TransformConstants.DEFAULT_COMPRESSION_MODE;
    public int compressionLevel = TransformConstants.DEFAULT_COMPRESSION_LEVEL;

    public CompressionMode getCompressMode() {
        return compressMode;
    }

    public void setCompressMode(CompressionMode compressMode) {
        this.compressMode = compressMode;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    @Override
    public CompressionTransformer getTransformer() {
        return new CompressionTransformer(compressMode, compressionLevel);
    }

    @Override
    public CompressionTransformer getTransformer(String transformClass,
            String config, Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTransformClass() {
        return TransformConstants.COMPRESSION_CLASS;
    }

}