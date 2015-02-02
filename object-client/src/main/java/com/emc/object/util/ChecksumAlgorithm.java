/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.object.util;

public enum ChecksumAlgorithm {
    SHA1("SHA-1"),
    MD5("MD5");

    private String digestName;

    private ChecksumAlgorithm(String digestName) {
        this.digestName = digestName;
    }

    public String getDigestName() {
        return this.digestName;
    }
}
