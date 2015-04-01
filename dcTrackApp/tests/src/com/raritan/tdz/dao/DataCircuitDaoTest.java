
/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class DataCircuitDaoTest extends TestBase {
	DataCircuitDAO circuitDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		circuitDAO = (DataCircuitDAO)ctx.getBean("dataCircuitDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		DataCircuit rec = circuitDAO.read(3081L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getCircuitTrace());
		}		
	}

	@Test
	public final void tesGetDestinationItemsForItemData() throws Throwable {
		HashMap<Long, PortInterface> portMap = circuitDAO.getDestinationItemsForItem(325);
				
		if(portMap == null){
			System.out.println("testing");
		}
		else{
			for(PortInterface r:portMap.values()){
				DataPortDTO p = (DataPortDTO)r;
				System.out.println(p.getConnectedItemName() + ":" + p.getConnectedPortName() + " Next Node Class = " + p.getNextNodeClassValueCode());
			}
		}		
	}


	@Test
	public final void testGetProposedCircuitIdForItem() throws Throwable {
		HashMap<Long, PortInterface> portMap = circuitDAO.getProposedCircuitIdsForItem(325);
		
		if(portMap == null){
			System.out.println("testing");
		}
		else{
			System.out.println("\n\n");
			for(PortInterface r:portMap.values()){
				DataPortDTO p = (DataPortDTO)r;
				System.out.println(p.getPortId() + " ==> " + p.getProposedCircuitId() );
			}
			System.out.println("\n\n");
		}		
	}

	@Test
	public final void testLoadCircuit() throws Throwable {
		DataCircuit rec = circuitDAO.getDataCircuit(6178L);
	}
	
	@Test
	public final void isLogicalConnectionsExist() throws Throwable {
		System.out.println("Return Value = " + circuitDAO.isLogicalConnectionsExist(6102L, 6099L));
		System.out.println("Return Value = " + circuitDAO.isLogicalConnectionsExist(6102L, null));
		System.out.println("Return Value = " + circuitDAO.isLogicalConnectionsExist(6099L, null));
		System.out.println("Return Value = " + circuitDAO.isLogicalConnectionsExist(610L, 99L));
		System.out.println("Return Value = " + circuitDAO.isLogicalConnectionsExist(610L, null));
	}
}
