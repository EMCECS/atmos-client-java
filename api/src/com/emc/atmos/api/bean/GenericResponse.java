package com.emc.atmos.api.bean;

public class GenericResponse<T> extends BasicResponse {
    T content;

    public GenericResponse() {
    }

    public GenericResponse( T content ) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent( T content ) {
        this.content = content;
    }
}
