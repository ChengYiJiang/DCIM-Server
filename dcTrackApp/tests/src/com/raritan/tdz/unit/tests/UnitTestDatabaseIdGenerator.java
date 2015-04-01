/**
 * 
 */
package com.raritan.tdz.unit.tests;

import java.util.Random;

/**
 * @author prasanna
 *
 */
public class UnitTestDatabaseIdGenerator {
	
	//private long id = new Random(100).nextLong();
	private long id = 1;
	
	public long nextId(){
		return id++;
	}

}
