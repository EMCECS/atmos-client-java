// Copyright (c) 2012, EMC Corporation.
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

package com.emc.acdp.api.request;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.AcdpResponse;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public abstract class AcdpRequest<T extends AcdpResponse> implements
        Callable<T> {
    private static final Logger l4j = Logger.getLogger(AcdpRequest.class);

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String PUT_METHOD = "PUT";
    public static final String HEAD_METHOD = "HEAD";
    public static final String DELETE_METHOD = "DELETE";

    /**
     * This is the package that contains the JAXB classes for XML binding. See
     * rest_model-1.0.jar
     */
    public static final String CDP_JAXB_CONTEXT = "com.emc.cdp.services.rest.model";

    public static final String DATE_HEADER = "Date";
    public static final DateFormat HEADER_FORMAT = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    public static final String LOCATION_FIELD = "Location";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String XML_CONTENT_TYPE = "text/xml";

    // Params
    public static final String CDP_SESSION_PARAM = "cdp_session";
    public static final String CDP_START_PARAM = "start";
    public static final String CDP_COUNT_PARAM = "count";

    protected String endpoint;

    protected Map<String, String> requestHeaders;

    public AcdpRequest() {
        requestHeaders = new HashMap<String, String>();
    }

    public abstract String getRequestPath();

    public abstract String getRequestQuery();

    public abstract String getMethod();

    public abstract long getRequestSize();

    public abstract byte[] getRequestData();

    public abstract boolean hasResponseBody();

    public abstract T parseResponse(int responseCode, String responseLine,
            Map<String, List<String>> headerFields, InputStream in);

    public abstract T parseResponse(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields);

    public abstract T parseError(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields, byte[] errorBody);

    public abstract T parseError(Throwable e);

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint
     *            the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    protected byte[] readErrorResponse(HttpURLConnection con)
            throws IOException {
        InputStream in = null;
        in = con.getErrorStream();
        if (in == null) {
            // could not get stream
            return new byte[0];
        }
        try {
            byte[] output;
            int contentLength = con.getContentLength();
            // If we know the content length, read it directly into a buffer.
            if (contentLength != -1) {
                output = new byte[con.getContentLength()];

                int c = 0;
                while (c < contentLength) {
                    int read = in.read(output, c, contentLength - c);
                    if (read == -1) {
                        // EOF!
                        throw new EOFException(
                                "EOF reading response at position " + c
                                        + " size " + (contentLength - c));
                    }
                    c += read;
                }

                return output;
            } else {
                // Content length is indeterminate.
                l4j.debug("Content length is unknown.  Buffering output.");
                // use a ByteArrayOutputStream to collect the response.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int c = 0;
                while ((c = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, c);
                }
                baos.close();

                l4j.debug("Buffered " + baos.size() + " response bytes");

                return baos.toByteArray();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    protected URL buildURL() throws URISyntaxException, MalformedURLException {
        String path = getRequestPath();
        String query = getRequestQuery();
        URI u;
        if (query != null) {
            u = new URI(endpoint + path + "?" + query);
        } else {
            u = new URI(endpoint + path);
        }

        return new URL(u.toASCIIString());
    }

    protected HttpURLConnection executeRequest() throws URISyntaxException,
            IOException {
        URL u = buildURL();

        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod(getMethod());

        Map<String, String> requestHeaders = getRequestHeaders();

        for (String key : requestHeaders.keySet()) {
            con.setRequestProperty(key, requestHeaders.get(key));
        }

        if (getRequestSize() > -1) {
            if (getRequestSize() > Integer.MAX_VALUE) {
                throw new EsuException(
                        "Use the ApacheTransport to send data > 2GB");
            }
            con.setFixedLengthStreamingMode((int) getRequestSize());
            con.setDoOutput(true);
        }

        if (hasResponseBody()) {
            con.setDoInput(true);
        }

        con.connect();

        if (getRequestSize() > -1) {
            // post data
            OutputStream out = null;
            byte[] data = getRequestData();
            try {
                out = con.getOutputStream();
                out.write(data);
            } catch (IOException e) {
                con.disconnect();
                throw new EsuException("Error posting data", e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }

                } catch (IOException e) {
                    // ignore.
                }
            }

        }

        return con;

    }

    @Override
    public T call() throws Exception {
        try {
            HttpURLConnection con = executeRequest();

            // Check response
            if (con.getResponseCode() > 299) {
                return parseError(con.getResponseCode(),
                        con.getResponseMessage(), con.getHeaderFields(),
                        readErrorResponse(con));
            }

            if (hasResponseBody()) {
                return parseResponse(con.getResponseCode(),
                        con.getResponseMessage(), con.getHeaderFields(),
                        con.getInputStream());
            } else {
                return parseResponse(con.getResponseCode(),
                        con.getResponseMessage(), con.getHeaderFields());
            }

        } catch (Exception e) {
            return parseError(e);
        }
    }

    protected byte[] serialize(Object obj) throws JAXBException,
            UnsupportedEncodingException {
        // Note we explicitly use the class loader here to ensure JAXB can find
        // everything it needs when running in a multiple classloader
        // environment like Tomcat or Axis.
        JAXBContext jc = JAXBContext.newInstance(CDP_JAXB_CONTEXT,
                ObjectFactory.class.getClassLoader());
        Marshaller m = jc.createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(obj, sw);
        if (l4j.isDebugEnabled()) {
            l4j.debug("Request XML: " + sw.toString());
        }
        return sw.toString().getBytes("UTF-8");
    }

    protected Object deserialize(InputStream data) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(CDP_JAXB_CONTEXT,
                ObjectFactory.class.getClassLoader());
        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal(data);
        return o;
    }

    protected String getContentType(Map<String, List<String>> headerFields) {
        return getSingleHeader(headerFields, CONTENT_TYPE);
    }

    protected String getSingleHeader(Map<String, List<String>> headerFields,
            String key) {
        List<String> values = headerFields.get(key);
        if (values == null || values.size() < 1) {
            return null;
        }
        return values.get(0);
    }

}
