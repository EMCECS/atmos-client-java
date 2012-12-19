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
package com.emc.threadeddownload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Extent;
import com.emc.esu.api.ObjectPath;

public class DownloadBlock implements Runnable {
	private Extent extent;
	public Extent getExtent() {
		return extent;
	}

	public void setExtent(Extent extent) {
		this.extent = extent;
	}

	public FileChannel getChannel() {
		return channel;
	}

	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}

	public EsuApi getEsu() {
		return esu;
	}

	public void setEsu(EsuApi esu) {
		this.esu = esu;
	}

	public ObjectPath getPath() {
		return path;
	}

	public void setPath(ObjectPath path) {
		this.path = path;
	}

	public ProgressListener getListener() {
		return listener;
	}

	public void setListener(ProgressListener listener) {
		this.listener = listener;
	}

	private FileChannel channel;
	private EsuApi esu;
	private ObjectPath path;
	private ProgressListener listener;

	@Override
	public void run() {
		//System.err.println( "Thread: " + Thread.currentThread() + " read: " + extent);
		
		try {
			byte[] data = null;
			while(true) {
				try {
					data = esu.readObject(path, extent, null);
					break;
				} catch(EsuException e) {
					System.err.println( "Read failed: " + e + "( " + e.getCause() + "), retrying" );
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			//System.err.println( "Thread: " + Thread.currentThread() + " write: " + extent);
			//System.out.flush();
			channel.write(ByteBuffer.wrap(data), extent.getOffset());
			//System.err.println( "Thread: " + Thread.currentThread() + " complete: " + extent);
			//System.out.flush();
			listener.complete(this);
		} catch (IOException e) {
			listener.error(e);
		}
		
	}

}
