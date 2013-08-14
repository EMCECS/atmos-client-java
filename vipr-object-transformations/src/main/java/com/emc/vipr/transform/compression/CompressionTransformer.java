package com.emc.vipr.transform.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.emc.vipr.transform.TransformConstants.CompressionMode;
import com.emc.vipr.transform.Transformer;

public class CompressionTransformer extends Transformer {

    private CompressionMode compressMode;
    private int compressionLevel;
    
    private CompressionOutputStream outputStream;

    public CompressionTransformer(CompressionMode compressMode,
            int compressionLevel) {
        this.compressMode = compressMode;
        this.compressionLevel = compressionLevel;
    }

    @Override
    public InputStream decodeInputStream(InputStream in) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream encodeOutputStream(OutputStream out) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> encodeMetadata(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> decodeMetadata(Map<String, String> metadata) {
        // TODO Auto-generated method stub
        return null;
    }
}