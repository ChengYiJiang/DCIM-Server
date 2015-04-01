/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class PowerCircuitDaoTest extends TestBase {
	PowerCircuitDAO circuitDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		circuitDAO = (PowerCircuitDAO)ctx.getBean("powerCircuitDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		PowerCircuit rec = circuitDAO.read(3081L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getCircuitTrace());
		}		
	}

	@Test
	public final void tesGetDestinationItemsForItemPower() throws Throwable {
		HashMap<Long, PortInterface> portMap = circuitDAO.getDestinationItemsForItem(945);
				
		if(portMap == null){
			System.out.println("testing");
		}
		else{
			System.out.println("\n\n");
			for(PortInterface r:portMap.values()){
				PowerPortDTO p = (PowerPortDTO)r;
				System.out.println(p.getConnectedItemName() + ":" + p.getConnectedPortName());
			}
			System.out.println("\n\n");
		}		
	}

	@Test
	public final void tesgetNextNodeAmpsForItem() throws Throwable {
		HashMap<Long, PortInterface> portMap = circuitDAO.getNextNodeAmpsForItem(945);
				
		if(portMap == null){
			System.out.println("testing");
		}
		else{
			System.out.println("\n\n");
			for(PortInterface r:portMap.values()){
				PowerPortDTO p = (PowerPortDTO)r;
				System.out.println(p.getConnectedItemName() + ":" + p.getConnectedPortName());
			}
			System.out.println("\n\n");
		}		
	}

	@Test
	public final void testGetProposedCircuitIdForItem() throws Throwable {
		HashMap<Long, PortInterface> portMap = circuitDAO.getProposedCircuitIdsForItem(233);//325
		
		if(portMap == null){
			System.out.println("testing");
		}
		else{
			System.out.println("\n\n");
			for(PortInterface r:portMap.values()){
				PowerPortDTO p = (PowerPortDTO)r;
				System.out.println(p.getPortId() + " ==> " + p.getProposedCircuitId());
			}
			System.out.println("\n\n");
		}	
		
		Map<String, UiComponentDTO> x = this.itemService.getItemDetails(3001L);
		
		x = null;
	}

	@Test
	public final void testGetPowerWattUsedSummary() throws Throwable {
		List<PowerWattUsedSummary> xList = circuitDAO.getPowerWattUsedSummary(7805, null, null, null, false);
		
		for(PowerWattUsedSummary x:xList){
			System.out.println(x);
		}
		
		xList = circuitDAO.getPowerWattUsedSummary(7805, null, null, null, true);
		
		for(PowerWattUsedSummary x:xList){
			System.out.println(x);
		}		
	}
	
	@Test	
	public final void testGetPowerCircuit() throws Throwable {
		circuitDAO.getPowerCircuit(6176L);
	}
}
