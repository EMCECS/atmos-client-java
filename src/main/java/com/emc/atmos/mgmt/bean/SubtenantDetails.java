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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "subtenant")
public class SubtenantDetails extends AbstractSubtenant {
    private int capacity;
    private String defaultPolicySpec;
    private List<ObjectUser> objectUsers;
    private boolean secCompliant;
    private List<? extends AdminUser> subtenantAdminList = new ArrayList<AdminUser>();

    @XmlElement
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @XmlElement
    public String getDefaultPolicySpec() {
        return defaultPolicySpec;
    }

    public void setDefaultPolicySpec(String defaultPolicySpec) {
        this.defaultPolicySpec = defaultPolicySpec;
    }

    @XmlElementWrapper(name = "uidSecretList")
    @XmlElement(name = "uidSecret")
    public List<ObjectUser> getObjectUsers() {
        return objectUsers;
    }

    public void setObjectUsers(List<ObjectUser> objectUsers) {
        this.objectUsers = objectUsers;
    }

    @XmlElement
    public boolean isSecCompliant() {
        return secCompliant;
    }

    public void setSecCompliant(boolean secCompliant) {
        this.secCompliant = secCompliant;
    }

    @XmlElementWrapper
    @XmlElement(name = "subtenantAdmin")
    public List<? extends AdminUser> getSubtenantAdminList() {
        return subtenantAdminList;
    }

    public void setSubtenantAdminList(List<? extends AdminUser> subtenantAdminList) {
        this.subtenantAdminList = subtenantAdminList;
    }
}
