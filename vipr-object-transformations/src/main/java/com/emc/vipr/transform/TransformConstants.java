package com.emc.vipr.transform;

public interface TransformConstants {
    // Some predefined transformation classes
    public static final String ENCRYPTION_CLASS = "ENC";
    public static final String COMPRESSION_CLASS = "COMP";

    public static final Integer DEFAULT_ENCRYPTION_PRIORITY = 100;
    public static final Integer DEFAULT_COMPRESSION_PRIORITY = 1000;
    
    public static final String METADATA_PREFIX = "x-emc-";
    
    //////////////////////////
    // Encryption Constants //
    //////////////////////////
    
    public static final String DEFAULT_ENCRYPTION_MODE = "AES256/CBC/PKCS5Padding";
    
    ///////////////////////////
    // Compression Constants //
    ///////////////////////////
    public enum CompressionMode { LZMA, Deflate };
    
    public static final CompressionMode DEFAULT_COMPRESSION_MODE = CompressionMode.Deflate;
    public static final int DEFAULT_COMPRESSION_LEVEL = 5;
    
    public static final String META_COMPRESSION_UNCOMP_SIZE = METADATA_PREFIX + "comp-uncompressed-size";
    public static final String META_COMPRESSION_COMP_SIZE = METADATA_PREFIX + "comp-compressed-size";
    public static final String META_COMPRESSION_COMP_RATIO = METADATA_PREFIX + "comp-compression-ratio";
    public static final String META_COMPRESSION_UNCOMP_SHA1 = METADATA_PREFIX + "comp-uncompressed-sha1";

}
