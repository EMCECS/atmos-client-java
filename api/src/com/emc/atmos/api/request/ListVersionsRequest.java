package com.emc.atmos.api.request;

import com.emc.atmos.api.ObjectId;

public class ListVersionsRequest extends ListRequest<ListVersionsRequest> {
    private ObjectId objectId;

    @Override
    public String getServiceRelativePath() {
        return objectId.getRelativeResourcePath();
    }

    @Override
    public String getQuery() {
        return "versions";
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    protected ListVersionsRequest me() {
        return this;
    }

    public ListVersionsRequest objectId( ObjectId objectId ) {
        setObjectId( objectId );
        return this;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId( ObjectId objectId ) {
        this.objectId = objectId;
    }
}
