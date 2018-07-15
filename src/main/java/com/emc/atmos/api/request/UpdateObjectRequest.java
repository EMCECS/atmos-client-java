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
package com.emc.atmos.api.request;

import com.emc.atmos.api.Range;
import com.emc.atmos.api.RestUtil;
import com.emc.util.HttpUtil;

import java.util.List;
import java.util.Map;

/**
 * Represents an update object request.
 */
public class UpdateObjectRequest extends PutObjectRequest<UpdateObjectRequest> {
    protected Range range;

    @Override
    public String getServiceRelativePath() {
        return identifier.getRelativeResourcePath();
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public Map<String, List<Object>> generateHeaders( boolean encodeUtf8 ) {
        Map<String, List<Object>> headers = super.generateHeaders( encodeUtf8 );

        if ( range != null )
            RestUtil.addValue( headers, HttpUtil.HEADER_RANGE, "bytes=" + range );

        return headers;
    }

    @Override
    protected UpdateObjectRequest me() {
        return this;
    }

    /**
     * Builder method for {@link #setRange(com.emc.atmos.api.Range)}
     */
    public UpdateObjectRequest range( Range range ) {
        setRange( range );
        return this;
    }

    /**
     * Returns the byte range for this update request.
     */
    public Range getRange() {
        return range;
    }

    /**
     * Sets the byte range for this update request (the range of bytes to update in the target object).
     */
    public void setRange( Range range ) {
        this.range = range;
    }
}
