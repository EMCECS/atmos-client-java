/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2013-2018, Dell EMC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.emc.atmos.mgmt.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.emc.util.BasicResponse;

@XmlRootElement(name="sharedSecret")
public class SharedSecret extends BasicResponse {

    @XmlAttribute
    private String keyCreateTime;

    @XmlAttribute
    private String keyExpireTime;

    @XmlValue
    private String sharedSecret;

    /**
     * @return the keyCreateTime
     */
    public String getKeyCreateTime() {
        return keyCreateTime;
    }

    /**
     * @param keyCreateTime the keyCreateTime to set
     */
    public void setKeyCreateTime(String keyCreateTime) {
        this.keyCreateTime = keyCreateTime;
    }

    /**
     * @return the keyExpireTime
     */
    public String getKeyExpireTime() {
        return keyExpireTime;
    }

    /**
     * @param keyExpireTime the keyExpireTime to set
     */
    public void setKeyExpireTime(String keyExpireTime) {
        this.keyExpireTime = keyExpireTime;
    }

    /**
     * @return the sharedSecret
     */
    public String getSharedSecret() {
        return sharedSecret;
    }

    /**
     * @param sharedSecret the sharedSecret to set
     */
    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

}
