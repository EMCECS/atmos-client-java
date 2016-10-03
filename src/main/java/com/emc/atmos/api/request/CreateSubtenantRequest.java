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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emc.atmos.api.RestUtil;

/**
 * Represents a request to create a new Atmos Subtenant in EMC ECS.
 */
public class CreateSubtenantRequest extends Request {
    private String objectVirtualPoolId;
    private String customSubtenantId;

    public CreateSubtenantRequest() {
    }

    @Override
    public String getServiceRelativePath() {
        return "/subtenant";
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public Map<String, List<Object>> generateHeaders() {
        Map<String, List<Object>> headers = new HashMap<String, List<Object>>();
        if(objectVirtualPoolId != null) {
            headers.put(RestUtil.XHEADER_OBJECT_VPOOL, 
                    Arrays.asList(new Object[]{ objectVirtualPoolId }));
            
        }
        if(customSubtenantId != null) {
            headers.put(RestUtil.XHEADER_SUBTENANT_ID, Arrays.asList(new Object[]{ customSubtenantId }));
        }
        return headers;
    }

    /**
     * Gets the virtual pool ID for this request.  May be null.
     */
    public String getObjectVirtualPoolId() {
        return objectVirtualPoolId;
    }

    /**
     * Sets the ECS Object Virtual Pool ID for the new subtenant.  If null, the default
     * object virtual pool for the user's namespace will be used.
     * @param objectVirtualPoolId the ID (a URN) of the Object Virtual Pool for the new 
     * subtenant.
     */
    public void setObjectVirtualPoolId(String objectVirtualPoolId) {
        this.objectVirtualPoolId = objectVirtualPoolId;
    }

    /**
     * Gets the custom subtenant ID for this request.  May be null.
     */
    public String getCustomSubtenantId() {
        return customSubtenantId;
    }

    /**
     * Sets a custom subtenant ID for the new subtenant.  Used to migrate legacy Atmos subtenants from an existing
     * installation.  Do not use unless migrating from a legacy subtenant!
     */
    public void setCustomSubtenantId(String customSubtenantId) {
        this.customSubtenantId = customSubtenantId;
    }
}
