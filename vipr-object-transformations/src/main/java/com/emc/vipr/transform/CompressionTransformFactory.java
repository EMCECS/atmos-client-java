package com.emc.vipr.transform;

import java.util.Map;

public class CompressionTransformFactory extends
        TransformFactory<CompressionTransformer> {

    public String compressMode;

    public Integer compressionLevel;

    @Override
    public CompressionTransformer getTransformer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompressionTransformer getTransformer(String transformClass,
            String config, Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTransformClass() {
        // TODO Auto-generated method stub
        return null;
    }

}