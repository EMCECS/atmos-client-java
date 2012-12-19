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
package com.emc.esu.api;

/**
 * The progress listener interface is used to report upload and download
 * progress.  Create a class that implements this interface and register it
 * with the helper by calling addListener().
 */
public interface ProgressListener {
    /**
     * This method will be called after a chunk has been transferred.
     * @param currentBytes the number of bytes transferred.  Note that
     * the value will be passed as a string since it may be >2GB.
     * @param bytesTotal the total number of bytes to transfer.  If
     * the total number of bytes is unknown, this value will be -1.  Note that
     * the value will be passed as a string since it may be >2GB.
     */
    public void onProgress( long currentBytes, long bytesTotal );
    
    /**
     * This callback will be invoked after the transfer has completed.
     */
    public void onComplete();
    
    /**
     * This callback will be invoked if there is an error during the transfer.
     * @param exception the exception that caused the transfer to
     * fail.
     */
    public void onError( Exception exception );

}
