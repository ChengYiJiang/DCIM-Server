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

import com.raritan.tdz.circuit.dao.PowerConnDAO;
//JB import com.raritan.tdz.circuit.dao.PowerConnFinderDAO;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.location.dao.LocationFinderDAO;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class PowerConnDaoTest extends TestBase {
	PowerConnDAO connDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		connDAO = (PowerConnDAO)ctx.getBean("powerConnectionDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		PowerConnection rec = connDAO.read(3081L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getSourcePortName());
		}		
	}

	/* JB @Test
	public final void tesfindSourcePort() throws Throwable {
		PowerConnection rec = connDAO.read(3081L);
		
		PowerConnFinderDAO finderDAO = (PowerConnFinderDAO) connDAO;
		
		List<PowerPort> recList = finderDAO.findDestinationPort(rec.getSourcePortId());
		
		if (recList != null && recList.size() == 1){
			PowerPort port = recList.get(0);	
			
			if(port != null){
				System.out.println(port.getPortName());
			}
		}
	} */
}
