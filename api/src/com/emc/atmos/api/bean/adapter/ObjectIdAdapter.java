package com.emc.atmos.api.bean.adapter;

import com.emc.atmos.api.ObjectId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ObjectIdAdapter extends XmlAdapter<String, ObjectId> {

    @Override
    public ObjectId unmarshal( String s ) throws Exception {
        return new ObjectId( s );
    }

    @Override
    public String marshal( ObjectId objectId ) throws Exception {
        return objectId.toString();
    }
}
