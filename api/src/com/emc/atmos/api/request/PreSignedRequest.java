package com.emc.atmos.api.request;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class PreSignedRequest implements Serializable {
    private static final long serialVersionUID = -5841074558608401979L;

    private URL url;
    private String method;
    private String contentType;
    private Map<String, List<Object>> headers;

    public PreSignedRequest( URL url, String method, String contentType, Map<String, List<Object>> headers ) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
    }

    public URL getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, List<Object>> getHeaders() {
        return headers;
    }
}
