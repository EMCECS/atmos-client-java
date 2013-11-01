/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.atmos.api.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.emc.atmos.AtmosException;
import com.emc.atmos.api.Acl;
import com.emc.atmos.api.AtmosApi;
import com.emc.atmos.api.BufferSegment;
import com.emc.atmos.api.ObjectId;
import com.emc.atmos.api.ObjectIdentifier;
import com.emc.atmos.api.ObjectPath;
import com.emc.atmos.api.Range;
import com.emc.atmos.api.bean.BasicResponse;
import com.emc.atmos.api.bean.CreateAccessTokenResponse;
import com.emc.atmos.api.bean.CreateObjectResponse;
import com.emc.atmos.api.bean.GenericResponse;
import com.emc.atmos.api.bean.GetAccessTokenResponse;
import com.emc.atmos.api.bean.ListAccessTokensResponse;
import com.emc.atmos.api.bean.ListDirectoryResponse;
import com.emc.atmos.api.bean.ListObjectsResponse;
import com.emc.atmos.api.bean.ListVersionsResponse;
import com.emc.atmos.api.bean.Metadata;
import com.emc.atmos.api.bean.ObjectInfo;
import com.emc.atmos.api.bean.ObjectMetadata;
import com.emc.atmos.api.bean.ReadObjectResponse;
import com.emc.atmos.api.bean.ServiceInformation;
import com.emc.atmos.api.request.CreateAccessTokenRequest;
import com.emc.atmos.api.request.CreateObjectRequest;
import com.emc.atmos.api.request.CreateSubtenantRequest;
import com.emc.atmos.api.request.ListAccessTokensRequest;
import com.emc.atmos.api.request.ListDirectoryRequest;
import com.emc.atmos.api.request.ListObjectsRequest;
import com.emc.atmos.api.request.ListVersionsRequest;
import com.emc.atmos.api.request.PreSignedRequest;
import com.emc.atmos.api.request.ReadObjectRequest;
import com.emc.atmos.api.request.Request;
import com.emc.atmos.api.request.UpdateObjectRequest;
import com.emc.vipr.transform.InputTransform;
import com.emc.vipr.transform.OutputTransform;
import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.TransformException;
import com.emc.vipr.transform.TransformFactory;
import com.emc.vipr.transform.encryption.DoesNotNeedRekeyException;
import com.emc.vipr.transform.encryption.EncryptionTransformFactory;

/**
 * @author cwikj
 *
 */
public class AtmosEncryptionClient implements AtmosApi {
    private static final String UNSUPPORTED_MSG = "This operation is not supported by "
            + "the encryption client";
    private static final String PARTIAL_UPDATE_MSG = "Partial object updates and/or "
            + "appends are not supported by the encryption client";
    private static final String PARTIAL_READ_MSG = "Partial object reads are not "
            + "supported by the encryption client"; 
    private static final String UNSUPPORTED_TYPE_MSG = "Only InputStream, String, and "
            + "byte[] content are supported";

    private static final int DEFAULT_BUFFER_SIZE = 4096*1024;
    
    private AtmosApi delegate;
    private TreeSet<TransformFactory<?, ?>> factories;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * 
     */
    public AtmosEncryptionClient(AtmosApi delegate, EncryptionConfig encryptionConfig, CompressionConfig compressionConfig) {
        this.delegate = delegate;
        
        factories = new TreeSet<TransformFactory<?,?>>();
        if(encryptionConfig != null) {
            factories.add(encryptionConfig.getFactory());
        }
        if(compressionConfig != null) {
            factories.add(compressionConfig.getFactory());
        }
    }
    
    public AtmosEncryptionClient(AtmosApi delegate, Collection<TransformFactory<OutputTransform, InputTransform>> transformations) {
        this.delegate = delegate;
        
        factories = new TreeSet<TransformFactory<?,?>>();
        for(TransformFactory<OutputTransform, InputTransform> f : transformations) {
            factories.add(f);
        }
    }
    
    public void rekey(ObjectIdentifier identifier) throws DoesNotNeedRekeyException {
        Map<String,Metadata> umeta = delegate.getUserMetadata(identifier, (String[])null);
        Map<String,String> rawMeta = metaToMap(umeta.values());
        
        // Look for transform mode(s)
        String transformModes = rawMeta.get(TransformConstants.META_TRANSFORM_MODE);
        
        if(transformModes == null) {
            throw new DoesNotNeedRekeyException("Object is not encrypted");
        }
        
        // Split
        String[] modes = transformModes.split("\\|");
        
        // During decode, we process transforms in reverse order.
        List<String> revModes = new ArrayList<String>();
        revModes.addAll(Arrays.asList(modes));
        Collections.reverse(revModes);
        
        // Process transforms and look for encryption transforms.  It's theoretically
        // possible for an object to be encrypted more than once...
        boolean rekeyed = false;
        for(String mode : revModes) {
            if(!mode.startsWith(TransformConstants.ENCRYPTION_CLASS)) {
                continue;
            }
            
            boolean found = false;
            for(TransformFactory<?, ?> f : factories) {
                if(f instanceof EncryptionTransformFactory<?,?> && f.canDecode(mode, rawMeta)) {
                    EncryptionTransformFactory<?,?> ef = (EncryptionTransformFactory<?, ?>) f;
                    try {
                        rawMeta = ef.rekey(rawMeta);
                        rekeyed = true;
                    } catch (DoesNotNeedRekeyException e) {
                        throw e;
                    } catch (TransformException e) {
                        throw new AtmosException("Error rekeying object: " + e, e);
                    }
                    found = true;
                    break;
                }
            }
            if(!found) {
                throw new AtmosException("No transformation found to handle '" + mode + "'");
            }
        }
        
        if(!rekeyed) {
            throw new DoesNotNeedRekeyException("Object was not rekeyed");
        }
        
        // Update metadata.
        Collection<Metadata> updatedMeta = updateMetadata(rawMeta, umeta.values());
        delegate.setUserMetadata(identifier, updatedMeta.toArray(new Metadata[updatedMeta.size()]));
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getServiceInformation()
     */
    @Override
    public ServiceInformation getServiceInformation() {
        return delegate.getServiceInformation();
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#calculateServerClockSkew()
     */
    @Override
    public long calculateServerClockSkew() {
        return delegate.calculateServerClockSkew();
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#createObject(java.lang.Object, java.lang.String)
     */
    @Override
    public ObjectId createObject(Object content, String contentType) {
        CreateObjectRequest req = new CreateObjectRequest();
        req.setContent(content);
        req.setContentType(contentType);
        CreateObjectResponse res = createObject(req);
        
        return res.getObjectId();
     }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#createObject(com.emc.atmos.api.ObjectIdentifier, java.lang.Object, java.lang.String)
     */
    @Override
    public ObjectId createObject(ObjectIdentifier identifier, Object content,
            String contentType) {
        CreateObjectRequest req = new CreateObjectRequest();
        req.setIdentifier(identifier);
        req.setContent(content);
        req.setContentType(contentType);
        CreateObjectResponse res = createObject(req);
        
        return res.getObjectId();        
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#createObject(com.emc.atmos.api.request.CreateObjectRequest)
     */
    @Override
    public CreateObjectResponse createObject(CreateObjectRequest request) {
        // We can only handle input streams since we need to transform
        InputStream in = null;
        if(request.getContent() != null) {
            Object content = request.getContent();
            if(content instanceof InputStream) {
                in = (InputStream)content;
            } else if(content instanceof String) {
                in = new ByteArrayInputStream(((String)content).getBytes());
            } else if(content instanceof byte[]) {
                in = new ByteArrayInputStream((byte[])content);
            } else {
                throw new IllegalArgumentException(UNSUPPORTED_TYPE_MSG);
            }
        } else {
            // If there was no content, create an empty InputStream
            in = new ByteArrayInputStream(new byte[0]);
        }
        
        // Make metadata into a Map.
        Map<String, String> mMeta = null;
        if(request.getUserMetadata() != null) {
            mMeta = metaToMap(request.getUserMetadata());
        } else {
            // Empty
            mMeta = new HashMap<String, String>();
        }
        
        // Apply transforms
        List<OutputTransform> appliedTransforms = new ArrayList<OutputTransform>();
        
        // The TreeSet will store the objects in ascending order (priority).  Reverse it 
        // so highest priority comes first.
        List<TransformFactory<?, ?>> revFactories = new ArrayList<TransformFactory<?,?>>();
        revFactories.addAll(factories);
        Collections.reverse(revFactories);
        
        try {
            for(TransformFactory<?, ?> t : revFactories) {
                OutputTransform ot = t.getOutputTransform(in, mMeta);
                appliedTransforms.add(ot);
                in = ot.getEncodedInputStream();
            }
        } catch(TransformException e) {
            throw new AtmosException("Could not transform data: " + e, e);
        } catch (IOException e) {
            throw new AtmosException("Error transforming data: " + e, e);
        }
        
        // Create the object
        int c = 0;
        int pos = 0;
        byte[] buffer = new byte[bufferSize];
        
        // Read the first chunk and send it with the create request.
        try {
            c = fillBuffer(buffer, in);
        } catch (IOException e) {
            throw new AtmosException("Error reading input data: " + e, e);
        }
        if(c == -1) {
            // EOF already
            request.setContent(null);
            
            // Optmization -- send metadata now with create request and return
            try {
                in.close();
            } catch (IOException e) {
                throw new AtmosException("Error closing input: " + e, e);
            }
            for(OutputTransform ot : appliedTransforms) {
                mMeta.putAll(ot.getEncodedMetadata());
            }
            Set<Metadata> metadata = request.getUserMetadata();
            if(metadata == null) {
                metadata = new HashSet<Metadata>();
            }
            updateMetadata(mMeta, metadata);
            request.setUserMetadata(metadata);
            
            return delegate.createObject(request);
        } else {
            request.setContent(new BufferSegment(buffer, 0, c));
        }
        CreateObjectResponse resp = delegate.createObject(request);
        
        pos = c;
        
        // Append until EOF.
        try {
            while((c = fillBuffer(buffer, in)) != -1) {
                UpdateObjectRequest uor = new UpdateObjectRequest();
                uor.setIdentifier(resp.getObjectId());
                uor.setContentType(request.getContentType());
                uor.setRange(new Range(pos, pos+c-1));
                uor.setContent(new BufferSegment(buffer, 0, c));
                pos += c;
                delegate.updateObject(uor);
            }
        } catch (IOException e) {
            throw new AtmosException("Error reading input data: " + e, e);
        }
        
        try {
            in.close();
        } catch (IOException e) {
            throw new AtmosException("Error closing stream: " + e, e);
        }
        
        String transformConfig = "";
        // Update the object with the transformed metadata.
        for(OutputTransform ot : appliedTransforms) {
            mMeta.putAll(ot.getEncodedMetadata());
            if(transformConfig.length() != 0) {
                transformConfig += "|";
            }
            transformConfig += ot.getTransformConfig();
        }
        mMeta.put(TransformConstants.META_TRANSFORM_MODE, transformConfig);
        
        Set<Metadata> metadata = request.getUserMetadata();
        if(metadata == null) {
            metadata = new HashSet<Metadata>();
        }
        Collection<Metadata> updatedMetadata = updateMetadata(mMeta, metadata);
        delegate.setUserMetadata(resp.getObjectId(), 
                updatedMetadata.toArray(new Metadata[updatedMetadata.size()]));

        return resp;
    }

    /**
     * Reading from a cipher stream only returns one block at a time.  Keep reading
     * until the buffer is full.
     * @param buffer the buffer to fill
     * @param in the input stream to read from
     * @return a buffer as full as possible
     * @throws IOException if an error occurs reading from the stream.
     */
    private int fillBuffer(byte[] buffer, InputStream in) throws IOException{
        int read = 0;
        while(read < buffer.length) {
            int c = in.read(buffer, read, buffer.length-read);
            if(c == -1 && read == 0) {
                // EOF on first read
                return -1;
            } else if(c == -1) {
                // EOF
                return read;
            }
            read += c;
        }
        return read;
    }

    private Map<String, String> metaToMap(Collection<Metadata> userMetadata) {
        Map<String, String> meta = new HashMap<String, String>();
        for(Metadata m : userMetadata) {
            meta.put(m.getName(), m.getValue());
        }
        
        return meta;
    }
    
    private Collection<Metadata> updateMetadata(Map<String,String> meta, Collection<Metadata> collection) {
        Map<String,Metadata> updatedMetadata = new HashMap<String, Metadata>();
        
        // First, add all the old metadata.
        for(Metadata m : collection) {
            updatedMetadata.put(m.getName(), m);
        }
        
        // Apply updates and adds
        for(String key : meta.keySet()) {
            Metadata m = updatedMetadata.get(key);
            updatedMetadata.put(key, new Metadata(key, meta.get(key), 
                    m == null?false:m.isListable()));
        }
        
        return updatedMetadata.values();
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#readObject(com.emc.atmos.api.ObjectIdentifier, java.lang.Class)
     */
    @Override
    public <T> T readObject(ObjectIdentifier identifier, Class<T> objectType)
            throws IOException {
        return readObject( new ReadObjectRequest().identifier( identifier ), objectType ).getObject();
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#readObject(com.emc.atmos.api.ObjectIdentifier, com.emc.atmos.api.Range, java.lang.Class)
     */
    @Override
    public <T> T readObject(ObjectIdentifier identifier, Range range,
            Class<T> objectType) throws IOException {
        return readObject( new ReadObjectRequest().identifier( identifier ).ranges( range ), objectType ).getObject();
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#readObject(com.emc.atmos.api.request.ReadObjectRequest, java.lang.Class)
     */
    @Override
    public <T> ReadObjectResponse<T> readObject(ReadObjectRequest request,
            Class<T> objectType) throws IOException {
        
        // Partial reads not currently supported.
        if(request.getRanges() != null && request.getRanges().size() > 0) {
            throw new UnsupportedOperationException(PARTIAL_READ_MSG);            
        }
        
        // Validate we have an object type we can handle.
        Set<Class<?>> validTypes = new HashSet<Class<?>>(Arrays.asList(new Class<?>[] {
                String.class, byte[].class, InputStream.class
        }));
        if(!validTypes.contains(objectType)) {
            throw new IllegalArgumentException(UNSUPPORTED_TYPE_MSG);
        }
        
        // Execute the request, getting it as an InputStream
        ReadObjectResponse<InputStream> rawResponse = null;
        try {
            rawResponse = delegate.readObjectStream(request.getIdentifier(), null);
            
            // Process metadata.
            Map<String,String> rawMeta = metaToMap(rawResponse.getMetadata().getMetadata().values());
            
            // Look for transform mode(s)
            String transformModes = rawMeta.get(TransformConstants.META_TRANSFORM_MODE);
            
            if(transformModes == null) {
                // Object is not encoded.
                return rewrap(rawResponse, objectType);
            }
            
            // Split
            String[] modes = transformModes.split("\\|");
            
            // During decode, we process transforms in reverse order.
            List<String> revModes = new ArrayList<String>();
            revModes.addAll(Arrays.asList(modes));
            Collections.reverse(revModes);
            
            // Process transforms.
            InputStream streamToDecode = rawResponse.getObject();
            for(String mode : revModes) {
                boolean found = false;
                for(TransformFactory<?, ?> f : factories) {
                    if(f.canDecode(mode, rawMeta)) {
                        try {
                            InputTransform trans = f.getInputTransform(mode, streamToDecode, rawMeta);
                            streamToDecode = trans.getDecodedInputStream();
                            rawMeta = trans.getDecodedMetadata();
                            found = true;
                        } catch (TransformException e) {
                            throw new AtmosException("Error transforming object data: " + e, e);
                        }
                        continue;
                    }
                }
                if(!found) {
                    throw new AtmosException("No transformation found to handle '" + mode + "'");
                }
            }
            
            // Update response with decoded data
            rawResponse.setObject(streamToDecode);
            updateMetadata(rawMeta, rawResponse.getMetadata().getMetadata().values());
            
            // If a non-InputStream was requested, refactor the response.
            return rewrap(rawResponse, objectType);
        } finally {
            rawResponse.getObject().close();
        }

    }

    @SuppressWarnings("unchecked")
    private <T> ReadObjectResponse<T> rewrap(ReadObjectResponse<InputStream> rawResponse,
            Class<T> objectType) throws IOException {
        
        ReadObjectResponse<?> wrapped = null;
        
        if(InputStream.class.equals(objectType)) {
            return (ReadObjectResponse<T>) rawResponse;
        } else if(byte[].class.equals(objectType) || String.class.equals(objectType)) {
            InputStream in = rawResponse.getObject();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int c = 0;
            while((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            in.close();
            out.close();
            
            if(byte[].class.equals(objectType)) {
                wrapped = new ReadObjectResponse<byte[]>();
                ((ReadObjectResponse<byte[]>)wrapped).setObject(out.toByteArray());
            } else {
                wrapped = new ReadObjectResponse<String>();
                ((ReadObjectResponse<String>)wrapped).setObject(
                        new String(out.toByteArray()));
            }
       
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_TYPE_MSG);
        }
        
        wrapped.setContentLength(rawResponse.getContentLength());
        wrapped.setContentType(rawResponse.getContentType());
        wrapped.setDate(rawResponse.getDate());
        wrapped.setHeaders(rawResponse.getHeaders());
        wrapped.setHttpMessage(rawResponse.getHttpMessage());
        wrapped.setHttpStatus(rawResponse.getHttpStatus());
        wrapped.setLastModified(rawResponse.getLastModified());
        wrapped.setLocation(rawResponse.getLocation());

        return (ReadObjectResponse<T>) wrapped;
    }
    /*
     * @see com.emc.atmos.api.AtmosApi#readObjectStream(com.emc.atmos.api.ObjectIdentifier, com.emc.atmos.api.Range)
     */
    @Override
    public ReadObjectResponse<InputStream> readObjectStream(
            ObjectIdentifier identifier, Range range) {
        
        // Partial reads not currently supported.
        if(range != null) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        
        ReadObjectRequest request = new ReadObjectRequest().identifier(identifier);
        try {
            return readObject(request, InputStream.class);
        } catch (IOException e) {
            throw new AtmosException("Error getting response stream: " + e, e);
        }
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#updateObject(com.emc.atmos.api.ObjectIdentifier, java.lang.Object)
     */
    @Override
    public void updateObject(ObjectIdentifier identifier, Object content) {
        UpdateObjectRequest uor = new UpdateObjectRequest().identifier(identifier)
                .content(content);
        
        updateObject(uor);
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#updateObject(com.emc.atmos.api.ObjectIdentifier, java.lang.Object, com.emc.atmos.api.Range)
     */
    @Override
    public void updateObject(ObjectIdentifier identifier, Object content,
            Range range) {
        UpdateObjectRequest uor = new UpdateObjectRequest().identifier(identifier)
                .content(content).range(range);
        
        updateObject(uor);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#updateObject(com.emc.atmos.api.request.UpdateObjectRequest)
     */
    @Override
    public BasicResponse updateObject(UpdateObjectRequest request) {
        if(request.getRange() != null) {
            throw new UnsupportedOperationException(PARTIAL_UPDATE_MSG);
        }
        
        // We can only handle input streams since we need to transform
        InputStream in = null;
        if(request.getContent() != null) {
            Object content = request.getContent();
            if(content instanceof InputStream) {
                in = (InputStream)content;
            } else if(content instanceof String) {
                in = new ByteArrayInputStream(((String)content).getBytes());
            } else if(content instanceof byte[]) {
                in = new ByteArrayInputStream((byte[])content);
            } else {
                throw new IllegalArgumentException(UNSUPPORTED_TYPE_MSG);
            }
        } else {
            // If there was no content, create an empty InputStream
            in = new ByteArrayInputStream(new byte[0]);
        }

        // Get the original list of metadata names.  This is used later to delete tags
        // in case the transform mode changed and some tags are no longer relevant.
        Map<String,Boolean> metaNameMap = delegate.getUserMetadataNames(
                request.getIdentifier());
        Set<String> metaNames = new HashSet<String>();
        metaNames.addAll(metaNameMap.keySet());
        for(Iterator<String> i = metaNames.iterator(); i.hasNext();) {
            String s = i.next();
            if(!s.startsWith(TransformConstants.METADATA_PREFIX)) {
                i.remove();
            }
        }
        
        // Make metadata into a Map.
        Map<String, String> mMeta = null;
        if(request.getUserMetadata() != null) {
            mMeta = metaToMap(request.getUserMetadata());
        } else {
            // Empty
            mMeta = new HashMap<String, String>();
        }        
        
        // Apply transforms
        List<OutputTransform> appliedTransforms = new ArrayList<OutputTransform>();
        
        // The TreeSet will store the objects in ascending order (priority).  Reverse it 
        // so highest priority comes first.
        List<TransformFactory<?, ?>> revFactories = new ArrayList<TransformFactory<?,?>>();
        revFactories.addAll(factories);
        Collections.reverse(revFactories);
        
        try {
            for(TransformFactory<?, ?> t : revFactories) {
                OutputTransform ot = t.getOutputTransform(in, mMeta);
                appliedTransforms.add(ot);
                in = ot.getEncodedInputStream();
            }
        } catch(TransformException e) {
            throw new AtmosException("Could not transform data: " + e, e);
        } catch (IOException e) {
            throw new AtmosException("Error transforming data: " + e, e);
        }

        // Overwrite the object
        int c = 0;
        int pos = 0;
        byte[] buffer = new byte[bufferSize];
        
        // Read the first chunk and send it with the create request.
        try {
            c = fillBuffer(buffer, in);
        } catch (IOException e) {
            throw new AtmosException("Error reading input data: " + e, e);
        }
        if(c == -1) {
            // EOF already
            request.setContent(null);
            
            // Optmization -- send metadata now with create request and return
            try {
                in.close();
            } catch (IOException e) {
                throw new AtmosException("Error closing input: " + e, e);
            }
            for(OutputTransform ot : appliedTransforms) {
                mMeta.putAll(ot.getEncodedMetadata());
            }
            Set<Metadata> metadata = request.getUserMetadata();
            if(metadata == null) {
                metadata = new HashSet<Metadata>();
            }
            updateMetadata(mMeta, metadata);
            request.setUserMetadata(metadata);
            
            return delegate.updateObject(request);
        } else {
            request.setContent(new BufferSegment(buffer, 0, c));
        }
        BasicResponse resp = delegate.updateObject(request);
        
        pos = c;
        
        // Append until EOF.
        try {
            while((c = fillBuffer(buffer, in)) != -1) {
                UpdateObjectRequest uor = new UpdateObjectRequest();
                uor.setIdentifier(request.getIdentifier());
                uor.setContentType(request.getContentType());
                uor.setRange(new Range(pos, pos+c-1));
                uor.setContent(new BufferSegment(buffer, 0, c));
                pos += c;
                delegate.updateObject(uor);
            }
        } catch (IOException e) {
            throw new AtmosException("Error reading input data: " + e, e);
        }
        
        try {
            in.close();
        } catch (IOException e) {
            throw new AtmosException("Error closing stream: " + e, e);
        }
        
        String transformConfig = "";
        // Update the object with the transformed metadata.
        for(OutputTransform ot : appliedTransforms) {
            mMeta.putAll(ot.getEncodedMetadata());
            if(transformConfig.length() != 0) {
                transformConfig += "|";
            }
            transformConfig += ot.getTransformConfig();
        }
        mMeta.put(TransformConstants.META_TRANSFORM_MODE, transformConfig);
        
        Set<Metadata> metadata = request.getUserMetadata();
        if(metadata == null) {
            metadata = new HashSet<Metadata>();
        }
        Collection<Metadata> updatedMetadata = updateMetadata(mMeta, metadata);
        delegate.setUserMetadata(request.getIdentifier(), 
                updatedMetadata.toArray(new Metadata[updatedMetadata.size()]));
        
        metaNames.removeAll(mMeta.keySet());
        
        // Delete any unused tags
        if(metaNames.size() > 0) {
            delegate.deleteUserMetadata(request.getIdentifier(), 
                    metaNames.toArray(new String[metaNames.size()]));
        }
        
        return resp;
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#delete(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public void delete(ObjectIdentifier identifier) {
        delegate.delete(identifier);
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#createDirectory(com.emc.atmos.api.ObjectPath)
     */
    @Override
    public ObjectId createDirectory(ObjectPath path) {
        return delegate.createDirectory(path);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#createDirectory(com.emc.atmos.api.ObjectPath, com.emc.atmos.api.Acl, com.emc.atmos.api.bean.Metadata[])
     */
    @Override
    public ObjectId createDirectory(ObjectPath path, Acl acl,
            Metadata... metadata) {
        return delegate.createDirectory(path, acl, metadata);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#listDirectory(com.emc.atmos.api.request.ListDirectoryRequest)
     */
    @Override
    public ListDirectoryResponse listDirectory(ListDirectoryRequest request) {
        return delegate.listDirectory(request);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#move(com.emc.atmos.api.ObjectPath, com.emc.atmos.api.ObjectPath, boolean)
     */
    @Override
    public void move(ObjectPath oldPath, ObjectPath newPath, boolean overwrite) {
        delegate.move(oldPath, newPath, overwrite);
    }

    /* (non-Javadoc)
     * @see com.emc.atmos.api.AtmosApi#getUserMetadataNames(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public Map<String, Boolean> getUserMetadataNames(ObjectIdentifier identifier) {
        return delegate.getUserMetadataNames(identifier);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getUserMetadata(com.emc.atmos.api.ObjectIdentifier, java.lang.String[])
     */
    @Override
    public Map<String, Metadata> getUserMetadata(ObjectIdentifier identifier,
            String... metadataNames) {
        return getUserMetadata(identifier, metadataNames);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getSystemMetadata(com.emc.atmos.api.ObjectIdentifier, java.lang.String[])
     */
    @Override
    public Map<String, Metadata> getSystemMetadata(ObjectIdentifier identifier,
            String... metadataNames) {
        return delegate.getSystemMetadata(identifier, metadataNames);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getObjectMetadata(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public ObjectMetadata getObjectMetadata(ObjectIdentifier identifier) {
        return delegate.getObjectMetadata(identifier);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#setUserMetadata(com.emc.atmos.api.ObjectIdentifier, com.emc.atmos.api.bean.Metadata[])
     */
    @Override
    public void setUserMetadata(ObjectIdentifier identifier,
            Metadata... metadata) {
        delegate.setUserMetadata(identifier, metadata);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#deleteUserMetadata(com.emc.atmos.api.ObjectIdentifier, java.lang.String[])
     */
    @Override
    public void deleteUserMetadata(ObjectIdentifier identifier, String... names) {
        // TODO: Filter reserved names for transforms.
        delegate.deleteUserMetadata(identifier, names);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#listMetadata(java.lang.String)
     */
    @Override
    public Set<String> listMetadata(String metadataName) {
        return delegate.listMetadata(metadataName);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#listObjects(com.emc.atmos.api.request.ListObjectsRequest)
     */
    @Override
    public ListObjectsResponse listObjects(ListObjectsRequest request) {
        return delegate.listObjects(request);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getAcl(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public Acl getAcl(ObjectIdentifier identifier) {
        return delegate.getAcl(identifier);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#setAcl(com.emc.atmos.api.ObjectIdentifier, com.emc.atmos.api.Acl)
     */
    @Override
    public void setAcl(ObjectIdentifier identifier, Acl acl) {
        delegate.setAcl(identifier, acl);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#getObjectInfo(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public ObjectInfo getObjectInfo(ObjectIdentifier identifier) {
        // TODO: enhance with transform info ?
        return delegate.getObjectInfo(identifier);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#createVersion(com.emc.atmos.api.ObjectIdentifier)
     */
    @Override
    public ObjectId createVersion(ObjectIdentifier identifier) {
        return delegate.createVersion(identifier);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#listVersions(com.emc.atmos.api.request.ListVersionsRequest)
     */
    @Override
    public ListVersionsResponse listVersions(ListVersionsRequest request) {
        return delegate.listVersions(request);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#restoreVersion(com.emc.atmos.api.ObjectId, com.emc.atmos.api.ObjectId)
     */
    @Override
    public void restoreVersion(ObjectId objectId, ObjectId versionId) {
        delegate.restoreVersion(objectId, versionId);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#deleteVersion(com.emc.atmos.api.ObjectId)
     */
    @Override
    public void deleteVersion(ObjectId versionId) {
        delegate.deleteVersion(versionId);
    }

    /**
     * Getting shareable URLs for transformed content is not supported since there is
     * no way to transform the stream since it's streamed directly from Atmos and does
     * not go through the SDK.
     * @see com.emc.atmos.api.AtmosApi#getShareableUrl(com.emc.atmos.api.ObjectIdentifier, java.util.Date)
     */
    @Override
    public URL getShareableUrl(ObjectIdentifier identifier, Date expirationDate)
            throws MalformedURLException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Getting shareable URLs for transformed content is not supported since there is
     * no way to transform the stream since it's streamed directly from Atmos and does
     * not go through the SDK.
     * @see com.emc.atmos.api.AtmosApi#getShareableUrl(com.emc.atmos.api.ObjectIdentifier, java.util.Date, java.lang.String)
     */
    @Override
    public URL getShareableUrl(ObjectIdentifier identifier,
            Date expirationDate, String disposition)
            throws MalformedURLException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#createAccessToken(com.emc.atmos.api.request.CreateAccessTokenRequest)
     */
    @Override
    public CreateAccessTokenResponse createAccessToken(
            CreateAccessTokenRequest request) throws MalformedURLException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#getAccessToken(java.net.URL)
     */
    @Override
    public GetAccessTokenResponse getAccessToken(URL url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#getAccessToken(java.lang.String)
     */
    @Override
    public GetAccessTokenResponse getAccessToken(String accessTokenId) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#deleteAccessToken(java.net.URL)
     */
    @Override
    public void deleteAccessToken(URL url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);

    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#deleteAccessToken(java.lang.String)
     */
    @Override
    public void deleteAccessToken(String accessTokenId) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Access tokens are unsupported for transformed content since there is no way for
     * the SDK to transform the content since communication goes directly to and from
     * Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#listAccessTokens(com.emc.atmos.api.request.ListAccessTokensRequest)
     */
    @Override
    public ListAccessTokensResponse listAccessTokens(
            ListAccessTokensRequest request) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Presigned requests are unsupported for transformed content since there is no way
     * for the SDK to transform the content since communication may go directly to and 
     * from Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#preSignRequest(com.emc.atmos.api.request.Request, java.util.Date)
     */
    @Override
    public PreSignedRequest preSignRequest(Request request, Date expiration)
            throws MalformedURLException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * Presigned requests are unsupported for transformed content since there is no way
     * for the SDK to transform the content since communication may go directly to and 
     * from Atmos.
     * 
     * @see com.emc.atmos.api.AtmosApi#execute(com.emc.atmos.api.request.PreSignedRequest, java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> GenericResponse<T> execute(PreSignedRequest request,
            Class<T> resultType, Object content) throws URISyntaxException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /*
     * @see com.emc.atmos.api.AtmosApi#createSubtenant(com.emc.atmos.api.request.CreateSubtenantRequest)
     */
    @Override
    public String createSubtenant(CreateSubtenantRequest request) {
        return delegate.createSubtenant(request);
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize the bufferSize to set
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

}
