package com.emc.esu.test;

import com.emc.esu.api.Checksum;
import com.emc.esu.api.Checksum.Algorithm;

public class HashTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Checksum ck = new Checksum(Algorithm.SHA0);
			byte[] hello = "hello".getBytes("US-ASCII");
			byte[] world = " world".getBytes("US-ASCII");
			
			ck.update(hello, 0, hello.length);
			System.out.println( "Hash after 'hello' " + ck );
			
			ck.update(world, 0, world.length);
			System.out.println(  "Hash after ' world'" + ck );
			
			byte[] helloworld = "hello world".getBytes("US-ASCII");
			ck = new Checksum( Algorithm.SHA0 );
			ck.update( helloworld, 0, helloworld.length );
			System.out.println( "full: " + ck );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		
	}

}
