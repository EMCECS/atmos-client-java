package com.emc.atmos.api.request;

import java.util.List;
import java.util.Map;

public abstract class Request {
    public abstract String getServiceRelativePath();

    /**
     * Override if a request requires a query string (i.e. "metadata/system" for getting system metadata)
     *
     * @return the URL query string for this request
     */
    public String getQuery() {
        return null;
    }

    public abstract String getMethod();

    public abstract Map<String, List<Object>> generateHeaders();
}
