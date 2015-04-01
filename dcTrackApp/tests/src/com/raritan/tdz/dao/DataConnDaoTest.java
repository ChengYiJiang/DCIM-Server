
/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class DataConnDaoTest extends TestBase {
	DataConnDAO connDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		connDAO = (DataConnDAO)ctx.getBean("dataConnectionDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		DataConnection rec = connDAO.read(1L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getSourcePortName());
		}		
	}

}
