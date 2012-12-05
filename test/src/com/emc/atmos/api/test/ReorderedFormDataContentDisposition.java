package com.emc.atmos.api.test;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.reader.HttpHeaderReader;

import java.text.ParseException;
import java.util.Date;

public class ReorderedFormDataContentDisposition extends FormDataContentDisposition {
    public ReorderedFormDataContentDisposition( String type,
                                                String name,
                                                String fileName,
                                                Date creationDate,
                                                Date modificationDate, Date readDate, long size ) {
        super( type, name, fileName, creationDate, modificationDate, readDate, size );
    }

    public ReorderedFormDataContentDisposition( String header ) throws ParseException {
        super( header );
    }

    public ReorderedFormDataContentDisposition( HttpHeaderReader reader )
            throws ParseException {
        super( reader );
    }

    @Override
    protected StringBuilder toStringBuffer() {
        StringBuilder sb = new StringBuilder();
        sb.append( getType() );
        addStringParameter( sb, "name", getName() );
        addStringParameter( sb, "filename", getFileName() );
        addDateParameter( sb, "creation-date", getCreationDate() );
        addDateParameter( sb, "modification-date", getModificationDate() );
        addDateParameter( sb, "read-date", getReadDate() );
        addLongParameter( sb, "size", getSize() );
        return sb;
    }
}
