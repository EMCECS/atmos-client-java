/**
 * 
 */
package com.emc.vipr.transform.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.emc.vipr.transform.TransformConstants;
import com.emc.vipr.transform.encryption.KeyUtils;
import com.emc.vipr.transform.util.CountingOutputStream;

import SevenZip.Compression.LZMA.Encoder;

/**
 * @author cwikj
 *
 */
public class LZMAOutputStream extends OutputStream implements CompressionOutputStream, Runnable {
    private CountingOutputStream compressedOutput;
    private Thread compressionThread;
    private InputStream inputPipe;
    private CountingOutputStream uncompressedSize;
    private DigestOutputStream outputPipe;
    private boolean closed;
    private Encoder lzma;
    private Exception compressionFailure;
    private byte[] uncompressedDigest;
    
    /**
     * Map LZMA compression parameters into the standard 0-9 compression levels.
     */
    private static LzmaProfile COMPRESSION_PROFILE[] = { 
        new LzmaProfile(16*1024, 5, Encoder.EMatchFinderTypeBT2), // 0
        new LzmaProfile(64*1024, 64, Encoder.EMatchFinderTypeBT2), // 1
        new LzmaProfile(512*1024, 128, Encoder.EMatchFinderTypeBT2), // 2
        new LzmaProfile(1024*1024, 128, Encoder.EMatchFinderTypeBT2), // 3
        new LzmaProfile(8*1024*1024, 128, Encoder.EMatchFinderTypeBT2), // 4
        new LzmaProfile(16*1024*1024, 128, Encoder.EMatchFinderTypeBT2), // 5
        new LzmaProfile(24*1024*1024, 192, Encoder.EMatchFinderTypeBT2), // 6
        new LzmaProfile(32*1024*1024, 224, Encoder.EMatchFinderTypeBT4), // 7
        new LzmaProfile(48*1024*1024, 256, Encoder.EMatchFinderTypeBT4), // 8
        new LzmaProfile(64*1024*1024, 273, Encoder.EMatchFinderTypeBT4) // 9
    };
    
    private static ThreadGroup tg = new ThreadGroup("LZMACompress");
    
    public static long memoryRequired(int compressionLevel) {
        return memoryRequired(COMPRESSION_PROFILE[compressionLevel]);
    }
    
    public static long memoryRequired(LzmaProfile profile) {
        return (long)(profile.dictionarySize * 11.5);
    }
    
    public LZMAOutputStream(OutputStream out, LzmaProfile compressionProfile) throws IOException {
        compressedOutput = new CountingOutputStream(out);
        closed = false;
        uncompressedDigest = new byte[0];
        
        // The LZMA Encoder requires an input stream and thus does not make a good
        // filter.  We need to create a pipe and and use an auxiliary thread to compress
        // the data.
        inputPipe = new PipedInputStream();
        uncompressedSize = new CountingOutputStream(new PipedOutputStream((PipedInputStream) inputPipe));
        try {
            outputPipe = new DigestOutputStream(uncompressedSize, MessageDigest.getInstance("SHA1"));
        } catch (NoSuchAlgorithmException e) {
           throw new IOException("Could not create LZMAOutputStream", e);
        }
        lzma = new Encoder();
        lzma.SetDictionarySize(compressionProfile.dictionarySize);
        lzma.SetNumFastBytes(compressionProfile.fastBytes);
        lzma.SetMatchFinder(compressionProfile.matchFinder);
        lzma.SetLcLpPb(compressionProfile.lc, compressionProfile.lp, compressionProfile.pb);
        lzma.SetEndMarkerMode(true);
        
        lzma.WriteCoderProperties(compressedOutput);
        
        compressionThread = new Thread(tg, this);
        
        compressionThread.start();
    }

    public LZMAOutputStream(OutputStream out, int compressionLevel) throws IOException {
        this(out, COMPRESSION_PROFILE[compressionLevel]);
    }

    /*
     * @see com.emc.vipr.transform.compression.CompressionOutputStream#getStreamMetadata()
     */
    @Override
    public Map<String, String> getStreamMetadata() {
        if(!closed) {
            throw new IllegalStateException("Stream must be closed before getting metadata");
        }
        
        Map<String,String> metadata = new HashMap<String, String>();
        
        long compSize = compressedOutput.getByteCount();
        long uncompSize = uncompressedSize.getByteCount();
        String compRatioString = String.format("%.1f%%", 100.0 - (compSize*100.0/uncompSize));
        
        metadata.put(TransformConstants.META_COMPRESSION_UNCOMP_SIZE, ""+uncompSize);
        metadata.put(TransformConstants.META_COMPRESSION_COMP_SIZE, ""+compSize);
        metadata.put(TransformConstants.META_COMPRESSION_COMP_RATIO, ""+compRatioString);
        metadata.put(TransformConstants.META_COMPRESSION_UNCOMP_SHA1, KeyUtils.toHexPadded(uncompressedDigest));
        
        return metadata;
    }

    @Override
    public void write(int b) throws IOException {
        outputPipe.write(b);
    }

    @Override
    public void close() throws IOException {
        if(closed) { return; }
        outputPipe.flush();
        outputPipe.close();

        closed = true;
        
        // Wait for encoder to finish
        try {
            compressionThread.join();
        } catch (InterruptedException e) {
            throw new IOException("Error waiting for compression thread to exit", e);
        }
        
        compressedOutput.close();
        uncompressedDigest = outputPipe.getMessageDigest().digest();
        // Free the encoder
        lzma = null;
        
    }

    @Override
    public void flush() throws IOException {
        writeCheck();
        outputPipe.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeCheck();
        outputPipe.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        writeCheck();
        write(b, 0, b.length);
    }

    @Override
    public void run() {
        // Start compressing data
        try {
            lzma.Code(inputPipe, compressedOutput, -1, -1, null);
        } catch(Exception e) {
            compressionFailure(e);
        }

    }
    
    private synchronized void compressionFailure(Exception e) {
        compressionFailure = e;
        e.printStackTrace();
    }
    
    private synchronized void writeCheck() throws IOException {
        if(compressionFailure != null) {
            throw new IOException("Error during stream compression", compressionFailure);
        }
        if(closed) {
            throw new IOException("Stream closed");
        }
    }
    
    public static class LzmaProfile {
        private int dictionarySize;
        private int fastBytes;
        private int matchFinder;
        private int lc;
        private int lp;
        private int pb;

        public LzmaProfile(int dictionarySize, int fastBytes, int matchFinder) {
            this(dictionarySize, fastBytes, matchFinder, 3, 0, 2);
        }
        
        public LzmaProfile(int dictionarySize, int fastBytes, int matchFinder, int lc, int lp, int pb) {
            this.dictionarySize = dictionarySize;
            this.fastBytes = fastBytes;
            this.matchFinder = matchFinder;
            this.lc = lc;
            this.lp = lp;
            this.pb = pb;
        }
    }
    

}
