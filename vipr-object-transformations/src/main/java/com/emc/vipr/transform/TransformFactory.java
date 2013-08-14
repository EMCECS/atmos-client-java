package com.emc.vipr.transform;

import java.util.Map;

/**
 * Abstract base class that produces both "outbound" transformers that encode data and
 * "inbound" transformers that decode data.
 *
 * @param <T> the class of transformer that this class produces.
 */
public abstract class TransformFactory<T extends Transformer> {
    
    private Integer priority;

    /** 
     * Gets an "outbound" transformer for the factory in its current
     * state.  This will be used to transform raw data on its way "out".
     * @return a transformer that can encode the outbound object stream.
     */
    public abstract T getTransformer();

    /**
     * Gets the "inbound" transformer for the given object metadata.
     * @param metadata metadata extracted from the inbound object
     * @return a transformer that can decode the inbound object stream.
     */
    public abstract T getTransformer(String transformClass, String config, 
            Map<String, String> metadata);

    /**
     * Gets the high-level class of transformer that this factory provides.  The
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
    public boolean canDecode(String transformClass, String config, Map<String,String> metadata) {
        return getTransformClass().equals(transformClass);
    }

    /**
     * Gets the priority of this factory.  For outbound configurations, higher priority
     * transformations will be applied first (e.g. compression should be applied before
     * encryption and therefore higher priority).  For inbound configurations, there may
     * be multiple factories that can handle an object and the one with higher priority
     * will take precedence.
     * @return this factory's priority.
     */
    public Integer getPriority() {
        return priority;
    }
    
    /**
     * Sets the priority for this factory.
     * @param priority
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }


}