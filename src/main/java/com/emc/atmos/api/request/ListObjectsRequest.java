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

import com.emc.atmos.api.RestUtil;
import com.emc.util.HttpUtil;

import java.util.List;
import java.util.Map;

/**
 * Represents a request to list/query the objects of a specific listable metadata name (tag).
 */
public class ListObjectsRequest extends ListMetadataRequest<ListObjectsRequest> {
    private String metadataName;

    @Override
    public String getServiceRelativePath() {
        return "objects";
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = super.generateHeaders();

        RestUtil.addValue( headers, RestUtil.XHEADER_TAGS, HttpUtil.encodeUtf8( metadataName ) );

        return headers;
    }

    @Override
    protected ListObjectsRequest me() {
        return this;
    }

    /**
     * Builder method for {@link #setMetadataName(String)}
     */
    public ListObjectsRequest metadataName( String metadataName ) {
        setMetadataName( metadataName );
        return this;
    }

    /**
     * Gets the metadata name (tag) to query.
     */
    public String getMetadataName() {
        return metadataName;
    }

    /**
     * Sets the metadata name (tag) to query. Atmos will return all of the objects that are assigned this exact tag.
     */
    public void setMetadataName( String metadataName ) {
        this.metadataName = metadataName;
    }
}
