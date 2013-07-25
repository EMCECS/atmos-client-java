package com.emc.vipr.transform;

import java.io.InputStream;
import java.util.Map;
import java.io.OutputStream;

/**
 * A transformer is the object that applies transformations to an inbound or
 * outbound object's data. The object's data stream is wrapped by the
 * transformer to encode or decode the data as appropriate. Also, the
 * transformer (usually on outbound transformations) can generate metadata about
 * the transformation (e.g. compression ratio, checksums, etc) that can be
 * merged with the object's metadata and can be reused for the inbound
 * transformation.
 */
public abstract class Transformer {

    /**
     * Wraps an input stream with another stream that will decode the inbound object
     * data stream.
     * @param in the inbound input stream.
     * @return the input stream wrapped with a decoder.
     */
    public abstract InputStream decodeInputStream(InputStream in);

    /**
     * Wraps the output stream with an encoder that will apply this transformation to
     * the stream.
     * @param out the output stream to encode.
     * @return a new output stream object that encodes the stream.
     */
    public abstract OutputStream encodeOutputStream(OutputStream out);

    /**
     * Encodes the object's metadata. Usually, this is called
     * after the output stream has been closed to get the updated object
     * metadata but could also somehow transform existing metadata like encrypting it.
     * @param metadata the input metadata to encode.
     * @return 
     */
    public abstract Map<String, String> encodeMetadata(Map<String, String> metadata);
    
    /**
     * Decodes the object's metadata.  Usually, this will simply return the object's
     * metadata.  However, in some circumstances this method could apply some
     * transformation to the metadata like decrypting it.
     * @param metadata the object's metadata
     * @return the decoded metadata.
     */
    public abstract Map<String, String> decodeMetadata(Map<String, String> metadata);

}