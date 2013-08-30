// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.cleanup;

import org.apache.log4j.Logger;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.ObjectPath;

public class DeleteFileTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger(DeleteFileTask.class);
	
	private ObjectPath filePath;
	private AtmosCleanup cleanup;

	@Override
	protected TaskResult execute() throws Exception {
		//l4j.debug("Deleting file " + filePath);
		try {
			cleanup.getEsu().deleteObject(filePath);
		} catch(EsuException e) {
			cleanup.failure(this, filePath, e);
			return new TaskResult(false);
		}

		cleanup.success(this, filePath);
		return new TaskResult(true);
	}

	public ObjectPath getFilePath() {
		return filePath;
	}

	public void setFilePath(ObjectPath filePath) {
		this.filePath = filePath;
	}

	public AtmosCleanup getCleanup() {
		return cleanup;
	}

	public void setCleanup(AtmosCleanup cleanup) {
		this.cleanup = cleanup;
	}

	@Override
	public String toString() {
		return "DeleteFileTask [filePath=" + filePath + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeleteFileTask other = (DeleteFileTask) obj;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}

}
