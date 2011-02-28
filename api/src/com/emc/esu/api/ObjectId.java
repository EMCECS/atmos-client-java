// Copyright (c) 2008, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification, 
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice, 
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright 
//       notice, this list of conditions and the following disclaimer in the 
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote 
//       products derived from this software without specific prior written 
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.esu.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Encapsulates a ESU object identifier.  Performs validation upon construction
 * to ensure that the identifier format is valid.  
 */
public class ObjectId implements Identifier {
    /**
     * Regular expression used to validate identifiers.
     */
    private static final Pattern ID_FORMAT = Pattern.compile( "^[0-9a-f]{44}$" );
    
    /**
     * Stores the string representation of the identifier
     */
    private String id;

    /**
     * Constructs a new object identifier
     * @param id the object ID as a string
     */
    public ObjectId( String id ) {
        if( !ID_FORMAT.matcher( id ).matches() ) {
            throw new EsuException( id + " is not a valid object id" );
        }
        this.id = id;
    }
    
    /**
     * Returns the identifier as a string
     * @return the identifier as a string
     */
    public String toString() {
        return id;
    }
    
    /**
     * Returns true if the object IDs are equal.
     */
    public boolean equals( Object obj ) {
    	if( obj instanceof ObjectResult ) {
    		return this.equals( ((ObjectResult)obj).getId() );
    	}
        if( !(obj instanceof ObjectId) ) {
            return false;
        }
        
        return id.equals( ((ObjectId)obj).toString() );
        
    }
    
    /**
     * Returns a hash code for this object id.
     */
    public int hashCode() {
        return id.hashCode();
    }

	public static Date parseXmlDate(String dateText) {
		if( dateText == null || dateText.length() < 1 ) {
			return null;
		}
		
		DateFormat xmlDate = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
		xmlDate.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
		
		try {
			Date d = xmlDate.parse( dateText );
			return d;
		} catch (ParseException e) {
			throw new EsuException( "Failed to parse date: " + dateText, e );
		}
	}
}
