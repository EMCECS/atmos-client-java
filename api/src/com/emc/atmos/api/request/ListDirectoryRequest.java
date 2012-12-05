package com.emc.atmos.api.request;

import com.emc.atmos.api.ObjectPath;

public class ListDirectoryRequest extends ListMetadataRequest<ListDirectoryRequest> {
    protected ObjectPath path;

    @Override
    public String getServiceRelativePath() {
        return path.getRelativeResourcePath();
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    protected ListDirectoryRequest me() {
        return this;
    }

    public ListDirectoryRequest path( ObjectPath path ) {
        setPath( path );
        return this;
    }

    public ObjectPath getPath() {
        return path;
    }

    public void setPath( ObjectPath path ) {
        this.path = path;
    }
}
