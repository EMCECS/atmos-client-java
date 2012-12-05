package com.emc.atmos.api.multipart;

import com.emc.atmos.api.Range;

public class MultipartPart {
    private String contentType;
    private Range contentRange;
    private byte[] data;

    public MultipartPart( String contentType, Range contentRange, byte[] data ) {
        this.contentType = contentType;
        this.contentRange = contentRange;
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public Range getContentRange() {
        return contentRange;
    }

    public byte[] getData() {
        return data;
    }
}
