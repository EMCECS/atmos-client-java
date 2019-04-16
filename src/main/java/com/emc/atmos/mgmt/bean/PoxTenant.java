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

@XmlRootElement(name = "tenant")
public class PoxTenant {
    private String id;
    private String name;
    private Status status;
    private AuthenticationSource authenticationSource;
    private List<PoxAdminUser> tenantAdminList = new ArrayList<PoxAdminUser>();
    private String policyDistributionStatus;
    private List<PoxAccessNode> accessNodeList = new ArrayList<PoxAccessNode>();
    private List<PoxNfsCifsNode> nfsCifsNodeList = new ArrayList<PoxNfsCifsNode>();
    private long capacity;
    private List<PoxSubtenant> subtenantList = new ArrayList<PoxSubtenant>();
    private List<PoxPolicy> policyList = new ArrayList<PoxPolicy>();
    private List<PoxPolicySelector> policySelectorList = new ArrayList<PoxPolicySelector>();
    private List<PoxHandler> handlerList = new ArrayList<PoxHandler>();
    private List<PoxExport> exportList = new ArrayList<PoxExport>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @XmlElement(name = "authentication_source")
    public AuthenticationSource getAuthenticationSource() {
        return authenticationSource;
    }

    public void setAuthenticationSource(AuthenticationSource authenticationSource) {
        this.authenticationSource = authenticationSource;
    }

    @XmlElementWrapper(name = "tenant_admin_list")
    @XmlElement(name = "tenant_admin")
    public List<PoxAdminUser> getTenantAdminList() {
        return tenantAdminList;
    }

    public void setTenantAdminList(List<PoxAdminUser> tenantAdminList) {
        this.tenantAdminList = tenantAdminList;
    }

    @XmlElement(name = "policy_distribution_status")
    public String getPolicyDistributionStatus() {
        return policyDistributionStatus;
    }

    public void setPolicyDistributionStatus(String policyDistributionStatus) {
        this.policyDistributionStatus = policyDistributionStatus;
    }

    @XmlElementWrapper(name = "access_node_list")
    @XmlElement(name = "access_node")
    public List<PoxAccessNode> getAccessNodeList() {
        return accessNodeList;
    }

    public void setAccessNodeList(List<PoxAccessNode> accessNodeList) {
        this.accessNodeList = accessNodeList;
    }

    @XmlElementWrapper(name = "nfs_cifs_node_list")
    @XmlElement(name = "nfs_cifs_node")
    public List<PoxNfsCifsNode> getNfsCifsNodeList() {
        return nfsCifsNodeList;
    }

    public void setNfsCifsNodeList(List<PoxNfsCifsNode> nfsCifsNodeList) {
        this.nfsCifsNodeList = nfsCifsNodeList;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    @XmlElementWrapper(name = "sub_tenant_list")
    @XmlElement(name = "sub_tenant")
    public List<PoxSubtenant> getSubtenantList() {
        return subtenantList;
    }

    public void setSubtenantList(List<PoxSubtenant> subtenantList) {
        this.subtenantList = subtenantList;
    }

    @XmlElementWrapper(name = "policy_list")
    @XmlElement(name = "policy")
    public List<PoxPolicy> getPolicyList() {
        return policyList;
    }

    public void setPolicyList(List<PoxPolicy> policyList) {
        this.policyList = policyList;
    }

    @XmlElementWrapper(name = "policy_selector_list")
    @XmlElement(name = "policy_selector")
    public List<PoxPolicySelector> getPolicySelectorList() {
        return policySelectorList;
    }

    public void setPolicySelectorList(List<PoxPolicySelector> policySelectorList) {
        this.policySelectorList = policySelectorList;
    }

    @XmlElementWrapper(name = "handler_list")
    @XmlElement(name = "handler")
    public List<PoxHandler> getHandlerList() {
        return handlerList;
    }

    public void setHandlerList(List<PoxHandler> handlerList) {
        this.handlerList = handlerList;
    }

    @XmlElementWrapper(name = "export_list")
    @XmlElement(name = "export")
    public List<PoxExport> getExportList() {
        return exportList;
    }

    public void setExportList(List<PoxExport> exportList) {
        this.exportList = exportList;
    }
}
