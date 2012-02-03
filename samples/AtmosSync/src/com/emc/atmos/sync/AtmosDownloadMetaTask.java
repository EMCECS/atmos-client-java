// Copyright (c) 2011, EMC Corporation.
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
package com.emc.atmos.sync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.emc.esu.api.Identifier;
import com.emc.esu.api.ObjectMetadata;
import com.google.gson.Gson;

/**
 * Downloads metadata from Atmos to a local file.
 */
public class AtmosDownloadMetaTask extends TaskNode {

	private Identifier id;
	private File file;
	private AtmosSync sync;

	public AtmosDownloadMetaTask(Identifier id, File file, AtmosSync sync) {
		this.id = id;
		this.file = file;
		this.sync = sync;
	}

	/**
	 * @see com.emc.atmos.sync.TaskNode#execute()
	 */
	@Override
	protected TaskResult execute() throws Exception {
		try {
			ObjectMetadata om = sync.getEsu().getAllMetadata(id);
			
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			Gson gson = new Gson();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			try {
				gson.toJson(om, writer);
			} finally {
				writer.close();
			}
		} catch(Exception e) {
			sync.failure(this, file, id, e);
			return new TaskResult(false);
		}
		
		return new TaskResult(true);
	}

}
