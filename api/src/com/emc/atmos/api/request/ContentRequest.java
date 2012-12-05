package com.emc.atmos.api.request;

public interface ContentRequest {
    String getContentType();

    Object getContent();

    long getContentLength();
}
