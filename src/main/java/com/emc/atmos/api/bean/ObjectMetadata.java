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
package com.emc.atmos.api.bean;

import com.emc.atmos.api.Acl;
import com.emc.atmos.api.ChecksumValue;

import java.util.Map;

public class ObjectMetadata {
    private Map<String, Metadata> metadata;
    private Acl acl;
    private String contentType;
    private Long retentionPeriod;
    private String retentionPolicy;
    private ChecksumValue wsChecksum;
    private ChecksumValue serverChecksum;

    public ObjectMetadata() {
    }

    public ObjectMetadata(Map<String, Metadata> metadata, Acl acl, String contentType,
                          ChecksumValue wsChecksum, ChecksumValue serverChecksum) {
        this.metadata = metadata;
        this.acl = acl;
        this.contentType = contentType;
        this.wsChecksum = wsChecksum;
        this.serverChecksum = serverChecksum;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Metadata> metadata) {
        this.metadata = metadata;
    }

    /**
     * Note: this feature is only available on ECS 2.2+
     */
    public Long getRetentionPeriod() {
        return retentionPeriod;
    }

    /**
     * Note: this feature is only available on ECS 2.2+
     */
    public void setRetentionPeriod(Long retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    /**
     * Note: this feature is only available on ECS 2.2+
     */
    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    /**
     * Note: this feature is only available on ECS 2.2+
     */
    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    /**
     * Returns the wschecksum if that feature was enabled for this object.
     */
    public ChecksumValue getWsChecksum() {
        return wsChecksum;
    }

    public void setWsChecksum(ChecksumValue wsChecksum) {
        this.wsChecksum = wsChecksum;
    }

    /**
     * Returns the last server-generated checksum for a single update to this object.
     *
     * @see com.emc.atmos.api.request.PutObjectRequest#setServerGeneratedChecksumAlgorithm(com.emc.atmos.api.ChecksumAlgorithm)
     */
    public ChecksumValue getServerChecksum() {
        return serverChecksum;
    }

    public void setServerChecksum(ChecksumValue serverChecksum) {
        this.serverChecksum = serverChecksum;
    }
}
