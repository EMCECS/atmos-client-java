package com.emc.vipr.services.s3.model;

import java.util.List;

public class GetFileAccessResult {
    private List<String> mountPoints;
    private boolean isTruncated;
    private List<FileAccessObject> objects;

    private String lastKey;

    /**
     * Returns all of the mount points providing NFS access to the objects in a
     * discrete list. These are provided as a convenience so that clients can
     * start mount operations before the entire list of objects is received.
     * Many objects may be hosted on a particular mount point.
     */
    public List<String> getMountPoints() {
        return mountPoints;
    }

    public void setMountPoints(List<String> mountPoints) {
        this.mountPoints = mountPoints;
    }

    /**
     * If true, the list of objects has been truncated based on the maxKeys
     * parameter.
     */
    public boolean isTruncated() {
        return isTruncated;
    }

    public void setTruncated(boolean truncated) {
        isTruncated = truncated;
    }

    /**
     * @return NFS details for all the objects accessible via NFS.
     */
    public List<FileAccessObject> getObjects() {
        return objects;
    }

    public void setObjects(List<FileAccessObject> objects) {
        this.objects = objects;
    }

    /**
     * @return the last key returned by this fileaccess response. if populated,
     *         the results in this response are truncated
     */
    public String getLastKey() {
        return lastKey;
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }
}
