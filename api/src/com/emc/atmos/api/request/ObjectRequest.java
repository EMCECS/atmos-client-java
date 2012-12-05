package com.emc.atmos.api.request;

import com.emc.atmos.api.ObjectIdentifier;

public abstract class ObjectRequest<T extends ObjectRequest<T>> extends Request {
    protected ObjectIdentifier identifier;

    protected abstract T me();

    public T identifier( ObjectIdentifier identifier ) {
        setIdentifier( identifier );
        return me();
    }

    public ObjectIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier( ObjectIdentifier identifier ) {
        this.identifier = identifier;
    }
}
