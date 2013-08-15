package com.emc.vipr.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Abstract base class that produces both "output" transformers that encode data and
 * "output" transforms that decode data.
 *
 * @param <T> the class of transform that this class produces.
 */
public abstract class TransformFactory<T extends OutputTransform, U extends InputTransform> {
    
    private int priority;

    /** 
     * Gets an "output" transform for the factory in its current
     * state.  This will be used to transform raw data on its way "out" to the server.
     * @return a transform that can encode the outbound object stream.
     * @throws IOException 
     */
    public abstract T getOutputTransform(OutputStream streamToEncode, Map<String,String> metadataToEncode) throws IOException;

    /**
     * Gets the "input" transform for the given class and metadata.
     * @param transformClass the class of transform (in case a particular factory can
     * handle multiple transform types)
     * @param config the configuration of the transformClass.
     * @param metadata metadata extracted from the inbound object (used to fine-tune
     * the transformation and/or provide metadata to also be transformed).
     * @return a transform that can decode the inbound object stream.
     * @throws IOException 
     */
    public abstract U getInputTransform(String transformConfig, 
            InputStream streamToDecode, Map<String, String> metadata) throws IOException;

    /**
     * Gets the high-level class of transform that this factory provides.  The
     * transform engine will use this to test whether the registered factory can decode
     * the given object.
     * @return the transformation class, e.g. "COMP" for compression or "ENC" for 
     * encryption.
     */
    public abstract String getTransformClass();
    
    /**
     * Checks whether this class can decode the given transformation configuration.
     * @param transformClass the transformation class to check, e.g. "COMP"
     * @param config the configuration for the transformation, e.g. "LZMA/9"
     * @param metadata the additional metadata from the object in case additional fields
     * need to be checked.
     * @return true if this factory can decode the given object stream.
     */
    public boolean canDecode(String transformConfig, Map<String,String> metadata) {
        //
        return getTransformClass().equals(splitTransformConfig(transformConfig)[0]);
    }
    
    protected String[] splitTransformConfig(String transformConfig) {
        String[] configTuple = transformConfig.split(":", 2);
        
        if(configTuple.length != 2) {
            throw new IllegalArgumentException("Invalid transform config string: " + transformConfig);
        }
        
        return configTuple;
    }

    /**
     * Gets the priority of this factory.  For output configurations, higher priority
     * transformations will be applied first (e.g. compression should be applied before
     * encryption and therefore higher priority).  For input configurations, there may
     * be multiple factories that can handle an object and the one with higher priority
     * will take precedence.
     * @return this factory's priority.
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Sets the priority for this factory.
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }


}