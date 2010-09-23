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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.apache.log4j.Logger;
import org.concord.security.ccjce.cryptix.jce.provider.CryptixCrypto;

/**
 * The checksum class is used to store and compute partial checksums when uploading
 * files.
 */
public class Checksum {
	private static final Logger l4j = Logger.getLogger( Checksum.class );
	
	// Register SHA-0 provider
	static {
		Security.addProvider( new CryptixCrypto() );
	}

	/**
	 * The hash algorithm to use.  As of 1.4.0, only SHA0 is supported
	 */
	public static enum Algorithm { SHA0, SHA1, MD5 };
	
	private MessageDigest digest;
	private Algorithm alg;
	private long offset;
	private String expectedValue;
	
	public Checksum( Algorithm alg ) throws NoSuchAlgorithmException {
		switch( alg ) {
		case SHA0:
			digest = MessageDigest.getInstance( "SHA-0" );
			break;
		case SHA1:
			digest = MessageDigest.getInstance( "SHA-1" );
			break;
		case MD5:
			digest = MessageDigest.getInstance( "MD5" );
			break;
		}
		this.alg = alg;
		offset = 0;
	}
	
	public String getAlgorithmName() {
		switch( alg ) {
			case SHA0:
				return "SHA0";
			case SHA1:
				return "SHA1";
			case MD5:
				return "MD5";
		}
		throw new RuntimeException( "Unknown algorithm: " + alg );
		
	}
	
	/**
	 * Updates the checksum with the given buffer's contents
	 * @param buffer data to update
	 * @param offset start in buffer
	 * @param length number of bytes to use from buffer starting at offset
	 */
	public void update( byte[] buffer, int offset, int length ) {
		digest.update(buffer, offset, length);
		this.offset += length;
	}

	/**
	 * Outputs the current digest checksum in a format
	 * suitable for including in Atmos create/update calls.
	 */
	@Override
	public String toString() {
		
		String checksumData = getAlgorithmName()+"/"+offset+"/"+getHashValue();
		l4j.debug( "Checksum Value: '" + checksumData + "'" );
		
		return checksumData;
	}

	private String getHashValue() {
		// Clone the digest so we can pad current value for output
		MessageDigest tmpDigest;
		try {
			tmpDigest = (MessageDigest) digest.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( "Clone failed", e );
		}
		
		byte[] currDigest = tmpDigest.digest();
		
		BigInteger bigInt = new BigInteger(1, currDigest);
		return bigInt.toString(16);
	}

	/**
	 * Sets the expected value for this checksum.  Only used for read operations
	 * @param expectedValue the expectedValue to set
	 */
	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	/**
	 * Gets the expected value for this checksum.  Only used for read operations.
	 * @return the expectedValue
	 */
	public String getExpectedValue() {
		return expectedValue;
	}
	
	
	
}
