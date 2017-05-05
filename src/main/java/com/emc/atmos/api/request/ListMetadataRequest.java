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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents common elements of list requests that may include object metadata.
 *
 * @param <T> Represents the implementation type. Allows a consistent builder interface throughout the request
 *            hierarchy. Parameterize concrete subclasses with their own type and implement {@link #me()} to return
 *            "this". In abstract subclasses, return me() in builder methods.
 */
public abstract class ListMetadataRequest<T extends ListMetadataRequest<T>> extends ListRequest<T> {
    protected List<String> userMetadataNames;
    protected List<String> systemMetadataNames;
    protected boolean includeMetadata;

    @Override
    public Map<String, List<Object>> generateHeaders( boolean encodeUtf8 ) {
        Map<String, List<Object>> headers = super.generateHeaders( encodeUtf8 );

        if ( includeMetadata ) {
            RestUtil.addValue( headers, RestUtil.XHEADER_INCLUDE_META, 1 );
            if ( userMetadataNames != null )
                for ( String name : userMetadataNames ) RestUtil.addValue( headers, RestUtil.XHEADER_USER_TAGS, name );
            if ( systemMetadataNames != null )
                for ( String name : systemMetadataNames )
                    RestUtil.addValue( headers, RestUtil.XHEADER_SYSTEM_TAGS, name );
        }

        return headers;
    }

    /**
     * Builder method for {@link #setUserMetadataNames(java.util.List)}
     */
    public T userMetadataNames( String... userMetadataNames ) {
        if ( userMetadataNames == null || (userMetadataNames.length == 1 && userMetadataNames[0] == null) )
            userMetadataNames = new String[0];
        setUserMetadataNames( Arrays.asList( userMetadataNames ) );
        return me();
    }

    /**
     * Builder method for {@link #setSystemMetadataNames(java.util.List)}
     */
    public T systemMetadataNames( String... systemMetadataNames ) {
        if ( systemMetadataNames == null || (systemMetadataNames.length == 1 && systemMetadataNames[0] == null) )
            systemMetadataNames = new String[0];
        setSystemMetadataNames( Arrays.asList( systemMetadataNames ) );
        return me();
    }

    /**
     * Builder method for {@link #setIncludeMetadata(boolean)}
     */
    public T includeMetadata( boolean includeMetadata ) {
        setIncludeMetadata( includeMetadata );
        return me();
    }

    /**
     * Gets the list of user metadata names that will be returned for each object in the list.
     */
    public List<String> getUserMetadataNames() {
        return userMetadataNames;
    }

    /**
     * Gets the list of system metadata names that will be returned for each object in the list.
     */
    public List<String> getSystemMetadataNames() {
        return systemMetadataNames;
    }

    /**
     * Gets whether the resulting list should include metadata for each object.
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the list of user metadata names that will be returned for each object in the list. If null, all user
     * metadata will be returned for each object in the list.
     */
    public void setUserMetadataNames( List<String> userMetadataNames ) {
        this.userMetadataNames = userMetadataNames;
    }

    /**
     * Sets the list of system metadata names that will be returned for each object in the list. If null, all system
     * metadata will be returned for each object in the list.
     */
    public void setSystemMetadataNames( List<String> systemMetadataNames ) {
        this.systemMetadataNames = systemMetadataNames;
    }

    /**
     * Sets whether the resulting list should include metadata for each object. Note that the default page size for
     * result lists changes when you include metadata in the results. Most Atmos systems default to 10k objects per
     * page without metadata and 500 with.
     */
    public void setIncludeMetadata( boolean includeMetadata ) {
        this.includeMetadata = includeMetadata;
    }
}
