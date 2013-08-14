/**
 * 
 */
package com.emc.vipr.transform.compression;

import java.util.Map;

/**
 * @author cwikj
 *
 */
public interface CompressionOutputStream {

    public abstract Map<String,String> getStreamMetadata();

}
