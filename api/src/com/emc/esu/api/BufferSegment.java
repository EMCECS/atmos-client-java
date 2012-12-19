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
 * A buffer segment is used to select a range of bytes from within an existing
 * byte array.
 */
public class BufferSegment {
    private byte[] buffer;
    private int offset;
    private int size;

    /**
     * Creates a new BufferSegment.
     * @param buffer the byte array
     * @param offset starting offset into the byte array in bytes
     * @param size the number of bytes in the segment
     */
    public BufferSegment( byte[] buffer, int offset, int size ) {
        this.buffer = buffer;
        this.offset = offset;
        this.size = size;
    }
    
    /**
     * Creates a BufferSegment that specifies the whole byte array (offset=0
     * and size=buffer.length).
     * @param buffer the byte array
     */
    public BufferSegment( byte[] buffer ) {
        this.buffer = buffer;
        this.offset = 0;
        this.size = buffer.length;
    }

    /**
     * @return the buffer
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }
}
