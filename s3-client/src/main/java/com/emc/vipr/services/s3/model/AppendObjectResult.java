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
package com.emc.vipr.services.s3.model;

public class AppendObjectResult extends UpdateObjectResult {
	private long appendOffset;

	/**
	 * Gets the starting offset inside the object where the data was
	 * appended.
	 * @return the append offset
	 */
	public long getAppendOffset() {
		return appendOffset;
	}

	/**
	 * Sets the starting offset inside the object where the data was
	 * appended.
	 * @param appendOffset the append offset
	 */
	public void setAppendOffset(long appendOffset) {
		this.appendOffset = appendOffset;
	}

}
