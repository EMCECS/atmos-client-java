@XmlSchema( namespace = "http://www.emc.com/cos/", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED )
@XmlJavaTypeAdapter( value = com.emc.atmos.api.bean.adapter.Iso8601Adapter.class, type = java.util.Date.class )
package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;