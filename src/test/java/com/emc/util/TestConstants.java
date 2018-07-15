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
package com.emc.util;

public final class TestConstants {
    public static final String PROP_ATMOS_UID = "atmos.uid";
    public static final String PROP_ATMOS_SECRET = "atmos.secret";
    public static final String PROP_ATMOS_ENDPOINTS = "atmos.endpoints";
    public static final String PROP_ATMOS_IS_ECS = "atmos.is_ecs";

    public static final String PROP_MGMT_ENDPOINTS = "atmos.mgmt.endpoints";
    public static final String PROP_MGMT_SYSADMIN_USER = "atmos.mgmt.sysadmin.user";
    public static final String PROP_MGMT_SYSADMIN_PASS = "atmos.mgmt.sysadmin.password";
    public static final String PROP_MGMT_TENANT = "atmos.mgmt.tenant";
    public static final String PROP_MGMT_TENANTADMIN_USER = "atmos.mgmt.tenantadmin.user";
    public static final String PROP_MGMT_TENANTADMIN_PASS = "atmos.mgmt.tenantadmin.password";

    public static final String PROP_PROXY = "http.proxyUri";

    private TestConstants() {
    }
}
