/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.atmos.api.encryption;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformConstants.CompressionMode;
import com.emc.vipr.transform.compression.CompressionTransformFactory;

/**
 * Contains the configuration for compression transformation in the Atmos client.  Note
 * that while LZMA compression produces high compression ratios it requires large 
 * amounts of RAM.  In particular, even level 5 can exceed the default Java heap size.
 * @see CompressionTransformFactory#memoryRequiredForLzma(int) to estimate the amount
 * of RAM required for LZMA.
 * 
 */
public class CompressionConfig {
    private CompressionTransformFactory factory;
    
    /**
     * Creates the default compression configuration (Deflate/5)
     */
    public CompressionConfig() {
        this(TransformConstants.DEFAULT_COMPRESSION_MODE, 
                TransformConstants.DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Creates the compression configuration.
     * @param mode the compression mode (e.g. LZMA or Deflate)
     * @param level the compression level 1-9.
     */
    public CompressionConfig(CompressionMode mode, int level) {
        factory = new CompressionTransformFactory();
        factory.setCompressMode(mode);
        factory.setCompressionLevel(level);
    }
    
    /**
     * Returns the {@link CompressionTransformFactory} with the current compression
     * configuration.
     * @return a configured {@link CompressionTransformFactory}
     */
    public CompressionTransformFactory getFactory() {
        return factory;
    }

}
