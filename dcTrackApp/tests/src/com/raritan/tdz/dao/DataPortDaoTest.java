/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class DataPortDaoTest extends TestBase {
	DataPortDAO portDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		portDAO = (DataPortDAO)ctx.getBean("dataPortDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		//Create a new LKU record
		//LkuData lku = genericDAO.read(1L);
		DataPort port = portDAO.read(2L);
		
		if(port == null){
			System.out.println("testing");
		}
		
		for(DataPort p:portDAO.getPortsForItem(341L)){
			System.out.println(p.getPortName());
		}
	}
	
	//@Test 
	public final void testSetIpAddressUsingIpAddressTable(){
		DataPort port = (DataPort)session.get(DataPort.class, 8057L);

		System.out.println(port.getPortName());
		
		System.out.println("Before : " + port.getIpAddress());
		
		//port.setIpAddressUsingIpAddressTable(session);
		
		System.out.println("After: " + port.getIpAddress());
		
	}
	
	@Test
	public final void testFindUsedDataPorts(){
		DataPortFinderDAO dataPortFinderDAO = (DataPortFinderDAO)portDAO;
		
		List<Long> portIds = dataPortFinderDAO.findUsedPorts(342L);
		
		Assert.assertTrue(portIds.size() > 0);
	}
	
	@Test
	public final void testFindUnusedDataPorts(){
		DataPortFinderDAO dataPortFinderDAO = (DataPortFinderDAO)portDAO;
		
		List<Long> portIds = dataPortFinderDAO.findUsedPorts(1230L);
		
		Assert.assertTrue(portIds.size() == 0);
	}

}
