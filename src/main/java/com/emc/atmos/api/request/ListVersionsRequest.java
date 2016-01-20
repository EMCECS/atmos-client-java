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

import com.emc.atmos.api.ObjectId;

/**
 * Represents a request to list the versions of an object. Note that version lists are paged by default at 4096 results
 * per page.
 */
public class ListVersionsRequest extends ListRequest<ListVersionsRequest> {
    private ObjectId objectId;

    @Override
    public String getServiceRelativePath() {
        return objectId.getRelativeResourcePath();
    }

    @Override
    public String getQuery() {
        return "versions";
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    protected ListVersionsRequest me() {
        return this;
    }

    /**
     * Builder method for {@link #setObjectId(com.emc.atmos.api.ObjectId)}
     */
    public ListVersionsRequest objectId( ObjectId objectId ) {
        setObjectId( objectId );
        return this;
    }

    /**
     * Gets the target object ID for which to list versions.
     */
    public ObjectId getObjectId() {
        return objectId;
    }

    /**
     * Sets the target object ID for which to list versions.
     */
    public void setObjectId( ObjectId objectId ) {
        this.objectId = objectId;
    }
}
