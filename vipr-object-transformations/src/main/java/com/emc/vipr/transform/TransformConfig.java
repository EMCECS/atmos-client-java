package com.emc.vipr.transform;

import java.io.InputStream;
import java.util.Map;
import java.io.OutputStream;

public abstract class TransformConfig {

  public abstract InputStream wrapInputStream(InputStream in);

  public abstract OutputStream wrapOutputStream(OutputStream out);

  /** 
   *  Gets the metadata associated with this transform.  Note that these values are likely not valid until after the output stream has been closed.
   */
  public abstract Map<String, String> getMetadata();

}