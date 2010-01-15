// Copyright (c) 2008, EMC Corporation.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Helper class to download objects.  For large transfers, the content
 * generally needs to be transferred from the server in smaller chunks.  This
 * helper class reads an object's contents incrementally from the server and
 * writes it to a file or stream.
 */
public class DownloadHelper {

    private static final Logger l4j = Logger.getLogger(DownloadHelper.class);
    private static final int DEFAULT_BUFFSIZE = 4096*1024; // 4MB

    private EsuApi esu;
    private BufferSegment buffer;

    private long currentBytes;
    private long totalBytes;
    private boolean complete;
    private boolean failed;
    private Exception error;
    private boolean closeStream;
    private OutputStream stream;
    private List<ProgressListener> listeners;

    /**
     * Creates a new download helper.
     * @param esuApi the API connection object to use to communicate
     * with the server.
     * @param buffer the buffer to use for the transfers from the server.  If
     * null, a default 4MB buffer will be used.
     */
    public DownloadHelper(EsuApi esuApi, byte[] buffer) {
        this.esu = esuApi;
        this.buffer = new BufferSegment(
                buffer == null ? new byte[DEFAULT_BUFFSIZE] : buffer);
        listeners = new ArrayList<ProgressListener>();
    }

    /**
     * Downloads the given object's contents to a file.
     * @param id the identifier of the object to download
     * @param f the file to write the object's contents to.
     */
    public void readObject( Identifier id, File f) {
        OutputStream out;
        try {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            throw new EsuException("Error opening output file", e);
        }
        readObject(id, out, true);
    }

    /**
     * Downloads the given object's contents to a stream.
     * @param id the identifier of the object to download.
     * @param stream the stream to write the object's contents to.
     * @param closeStream specifies whether to close the stream after
     * the transfer is complete.  Defaults to true.
     */
    public void readObject( Identifier id, OutputStream stream, boolean closeStream) {

        this.currentBytes = 0;
        this.complete = false;
        this.failed = false;
        this.error = null;
        this.closeStream = closeStream;
        this.stream = stream;

        // Get the file size.  Set to -1 if unknown.
        MetadataList sMeta = this.esu.getAllMetadata(id).getMetadata();
        if (sMeta.getMetadata("size") != null) {
            String size = sMeta.getMetadata("size").getValue();
            if (size != null && size.length() > 0) {
                this.totalBytes = Long.parseLong(size);
            } else {
                this.totalBytes = -1;
            }
        } else {
            this.totalBytes = -1;
        }

        // We need to know how big the object is to download it.  Fail the
        // transfer if we can't determine the object size.
        if (this.totalBytes == -1) {
            throw new EsuException("Failed to get object size");
        }

        // Loop, downloading chunks until the transfer is complete.
        while (true) {
            try {
                Extent extent = null;

                // Determine how much data to download.  If we're at the last
                // request in the transfer, only request as many bytes as needed
                // to get to the end of the file.
                if (currentBytes + buffer.getBuffer().length > totalBytes) {
                    // Would go past end of file.  Request less bytes.                                      
                    extent = new Extent(this.currentBytes, totalBytes
                            - currentBytes);
                } else {
                    extent = new Extent(this.currentBytes,
                            buffer.getBuffer().length);
                }
                buffer.setSize((int) extent.getSize());

                // Read data from the server.
                byte[] obuffer = this.esu.readObject(id, extent, buffer.getBuffer());

                // See if they returned the buffer we're using.  In some
                // cases, this doesn't happen (when content length is not
                // defined in the response).
                if( obuffer != buffer.getBuffer() ) {
                    if( obuffer.length != extent.getSize() ) {
                        throw new EsuException( "Read size mismatch.  " +
                        		"Requested " + extent.getSize() + 
                        		" bytes but received " + 
                        		obuffer.length + " bytes" );
                    }
                    stream.write( obuffer, 0, obuffer.length );
                } else {
                    // Write to the stream
                    stream.write(buffer.getBuffer(), buffer.getOffset(), buffer
                            .getSize());
                }

                // Update progress
                this.progress(buffer.getSize());

                // See if we're done.
                if (this.currentBytes == this.totalBytes) {
                    this.complete();
                    return;
                }
            } catch (EsuException e) {
                this.fail(e);
                throw e;
            } catch (IOException e) {
                fail(e);
                throw new EsuException("Error downloading file", e);
            }
        }
    }

    /**
     * Gets the current number of bytes that have been downloaded.
     * @return the current number of bytes downloaded.
     */
    public long getCurrentBytes() {
        return currentBytes;
    }

    /**
     * Gets the total number of bytes to download.
     * @return the total number of bytes to download.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Returns true if the transfer has completed.
     * @return true if the transfer has completed, false otherwise.
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Returns true if the transfer has failed.
     * @return true if the transfer has failed, false otherwise.
     */
    public boolean isFailed() {
        return this.failed;
    }

    /**
     * If the transfer has failed, return the error that caused the failure.
     * @return the error that caused the transfer to fail.
     */
    public Exception getError() {
        return this.error;
    }

    /**
     * Sets a listener to provide feedback on the transfer's progress.
     * @param listener the listener to use for feedback.  Set
     * to null to disable progress notifications.
     */
    public void addListener(ProgressListener listener) {
        this.listeners.add(listener);
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Updates progress on the current transfer and notifies the listener if
     * required.
     * @param bytes the number of bytes transferred
     */
    private void progress(long bytes) {
        this.currentBytes += bytes;
        for (Iterator<ProgressListener> i = listeners.iterator(); i.hasNext();) {
            ProgressListener pl = i.next();
            pl.onProgress(currentBytes, totalBytes);
        }
    }

    /**
     * Marks the current transfer as complete, closes the stream if required,
     * and notifies the listener.
     */
    private void complete() {
        complete = true;

        if (closeStream) {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
                l4j.warn("Error closing output stream", e);
            }
        }

        for (Iterator<ProgressListener> i = listeners.iterator(); i.hasNext();) {
            ProgressListener pl = i.next();
            pl.onComplete();
        }
    }

    /**
     * Fails the current transfer.  Sets the failed flag and notifies the 
     * listener if required.
     * @param e the error that caused the transfer to fail.
     */
    private void fail(Exception e) {
        this.failed = true;
        this.error = e;
        for (Iterator<ProgressListener> i = listeners.iterator(); i.hasNext();) {
            ProgressListener pl = i.next();
            pl.onError(e);
        }

    }

}
