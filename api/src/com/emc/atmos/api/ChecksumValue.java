package com.emc.atmos.api;

public abstract class ChecksumValue {
    public abstract ChecksumAlgorithm getAlgorithm();

    public abstract long getOffset();

    public abstract String getValue();

    /**
     * Outputs this checksum in a format suitable for including in Atmos create/update calls.
     */
    @Override
    public String toString() {
        return toString( true );
    }

    public String toString( boolean includeByteCount ) {
        String out = this.getAlgorithm().toString();
        if ( includeByteCount ) out += "/" + this.getOffset();
        out += "/" + getValue();
        return out;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !(o instanceof ChecksumValue) ) return false;

        ChecksumValue that = (ChecksumValue) o;

        if ( getOffset() != that.getOffset() ) return false;
        if ( getAlgorithm() != that.getAlgorithm() ) return false;
        if ( !getValue().equals( that.getValue() ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getAlgorithm().hashCode();
        result = 31 * result + (int) (getOffset() ^ (getOffset() >>> 32));
        result = 31 * result + getValue().hashCode();
        return result;
    }
}
