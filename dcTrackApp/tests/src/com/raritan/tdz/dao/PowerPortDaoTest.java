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

import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class PowerPortDaoTest extends TestBase {
	PowerPortDAO portDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		portDAO = (PowerPortDAO)ctx.getBean("powerPortDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		//Create a new LKU record
		//LkuData lku = genericDAO.read(1L);
		PowerPort port = portDAO.read(7870L);
		
		if(port == null){
			System.out.println("testing");
		}
		else{
			System.out.println(port.getPortName());
		}
		
		for(PowerPort p:portDAO.getPortsForItem(625L)){
			System.out.println(p.getPortName());
		}
	}
	
	@Test
	public final void tesLoadPort() throws Throwable {
		PowerPort port = portDAO.loadPort(7870L);
		
		if(port == null){
			System.out.println("testing");
		}
		else{
			System.out.println(port.getPortName());
		}		
	}
}
