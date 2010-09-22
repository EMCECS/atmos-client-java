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

/**
 * An extent specifies a portion of an object to read or write.  It contains
 * a starting offset and a number of bytes to read or write.
 */
public class Extent {
    /**
     * A static instance representing an entire object's content.
     */
    public static final Extent ALL_CONTENT = new Extent( -1, -1 );
    
    private long offset;
    private long size;
    
    /**
     * Creates a new extent
     * @param offset the starting offset in the object in bytes, 
     * starting with 0.  Use -1 to represent the entire object.
     * @param size the number of bytes to transfer.  Use -1 to represent
     * the entire object.
     */
    public Extent( long offset, long size ) {
            this.offset = offset;
            this.size = size;
    }
    
    /**
     * Returns the size of the extent.
     * @return the extent's size
     */
    public long getSize() {
            return this.size;
    }
    
    /**
     * Returns the starting offset of the extent
     * @return the extent's starting offset
     */
    public long getOffset() {
            return this.offset;
    }

    /**
     * Compares two extents.  Returns true if they are equal.
     */
    public boolean equals(Object obj) {
        if( !(obj instanceof Extent ) ) {
            return false;
        }
        
        Extent b = (Extent)obj;
        return b.getOffset() == offset && b.getSize() == size;
    }
    
    public String toString() {
        long end = offset + (size-1);
        return "bytes=" + offset + "-" + end;
    }

    public String getHeaderName() {
        return "Range";
    }
    

}
