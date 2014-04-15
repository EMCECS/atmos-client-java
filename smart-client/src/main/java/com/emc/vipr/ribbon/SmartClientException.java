/*
 * Copyright 2014 EMC Corporation. All Rights Reserved.
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
package com.emc.vipr.ribbon;

public class SmartClientException extends RuntimeException {
    public SmartClientException() {
    }

    public SmartClientException(String message) {
        super(message);
    }

    public SmartClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartClientException(Throwable cause) {
        super(cause);
    }

    public SmartClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
