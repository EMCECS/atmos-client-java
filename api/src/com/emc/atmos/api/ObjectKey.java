package com.emc.atmos.api;

public class ObjectKey implements ObjectIdentifier {
    private String bucket;
    private String key;

    public ObjectKey( String bucket, String key ) {
        this.bucket = bucket;
        this.key = key;
    }

    public String getBucket() {
        return this.bucket;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getRelativeResourcePath() {
        return "namespace/" + key;
    }

    @Override
    public String toString() {
        return getBucket() + "/" + getKey();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ObjectKey objectKey = (ObjectKey) o;

        if ( !bucket.equals( objectKey.bucket ) ) return false;
        if ( !key.equals( objectKey.key ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bucket.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}
