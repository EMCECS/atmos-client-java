package com.emc.vipr.transform.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformConstants.CompressionMode;
import com.emc.vipr.transform.TransformFactory;

public class CompressionTransformFactory extends
        TransformFactory<CompressionOutputTransform, CompressionInputTransform> {
    
    private static final Logger logger = LoggerFactory.getLogger(CompressionTransformFactory.class);

    public CompressionMode compressMode = TransformConstants.DEFAULT_COMPRESSION_MODE;
    public int compressionLevel = TransformConstants.DEFAULT_COMPRESSION_LEVEL;
    
    public CompressionTransformFactory() {
        setPriority(1000);
    }

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
    public CompressionOutputTransform getOutputTransform(
            OutputStream streamToEncodeTo, Map<String, String> metadataToEncode) throws IOException {
        switch(compressMode) {
        case Deflate:
            return new DeflateOutputTransform(streamToEncodeTo, metadataToEncode, compressionLevel);
        case LZMA:
            return new LZMAOutputTransform(streamToEncodeTo, metadataToEncode, compressionLevel);
        default:
            throw new IllegalArgumentException("Unsupported compression method " + compressMode); 
        }
    }

    @Override
    public CompressionInputTransform getInputTransform(String transformConfig, InputStream streamToDecode, Map<String, String> metadata) throws IOException {
        String[] transformTuple = splitTransformConfig(transformConfig);
        if(!TransformConstants.COMPRESSION_CLASS.equals(transformTuple[0])) {
            throw new IllegalArgumentException("Unsupported transform class: " + transformTuple[0]);
        }
        
        // Decode mode
        String[] configParams = transformTuple[1].split("/");
        
        if(configParams.length < 1) {
            throw new IllegalArgumentException("Could not decode configuration: " + configParams);
        }
        
        // First arg is mode.  Others are compression config and informational only.
        CompressionMode mode = CompressionMode.valueOf(configParams[0]);
        
        switch(mode) {
        case Deflate:
            return new DeflateInputTransform(streamToDecode, metadata);
        case LZMA:
            return new LZMAInputTransform(streamToDecode, metadata);
        default:
            throw new IllegalArgumentException("Unknown compression method " + mode);
        }
    }

    @Override
    public String getTransformClass() {
        return TransformConstants.COMPRESSION_CLASS;
    }
    
    /**
     * Checks whether this class can decode the given transformation configuration.
     * @param transformClass the transformation class to check, e.g. "COMP"
     * @param config the configuration for the transformation, e.g. "LZMA/9"
     * @param metadata the additional metadata from the object in case additional fields
     * need to be checked.
     * @return true if this factory can decode the given object stream.
     */
    public boolean canDecode(String transformClass, String config, Map<String,String> metadata) {
        // null?
        if(config == null) {
            logger.warn("Configuration string null");
            return false;
        }
        
        // Decode mode
        String[] configParams = config.split("/");
        
        if(configParams.length < 1) {
            logger.warn("Could not decode config string {}", config);
            return false;
        }
        
        // First arg is mode.  Others are compression config and informational only.
        try {
            CompressionMode.valueOf(configParams[0]);
        } catch(IllegalArgumentException e) {
            logger.warn("Invalid compression mode {}", configParams[0]);
            return false;
        }
        
        return getTransformClass().equals(transformClass);
    }


}