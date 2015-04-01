
package com.raritan.tdz.circuit.dao;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Circuit DAO tests
 * 
 * 
 * @author Santo Rosario
 */
public class CircuitRequestTest extends TestBase {

	private CircuitRequest circuitRequest;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		circuitRequest = (CircuitRequest)ctx.getBean("circuitRequest");
		log = Logger.getLogger(CircuitRequest.class);
	}
	
	@Test
	public void testGetCircuitIdsForWorkOrder(){
		List<Long> ids = circuitRequest.getPowerCircuitIdsForWorkOrder(366L);
		
		assertTrue(ids.size() == 0);
		
		ids = circuitRequest.getDataCircuitIdsForWorkOrder(366L);
		
		assertTrue(ids.size() == 0);
	}
		
	//@Test
	public void testNewConnectionRequest(){
		CircuitViewData circuit = createDummyDataCircuit(325L);
		
		Long requestId = circuitRequest.connect(circuit);
		
		assertTrue(requestId > 0);
		
		circuitRequest.delete(requestId, false);
	}	

	//@Test
	public void testNewConnectionRequestWithMove(){
		CircuitViewData circuit = createDummyDataCircuit(5107L);
		
		circuit.setStartPortId(49673L);
		
		Long requestId = circuitRequest.connect(circuit);
		
		assertTrue(requestId > 0);
		
		circuitRequest.delete(requestId, false);
	}	
	
	public CircuitViewData createDummyDataCircuit(Long itemId){
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		CircuitViewData circuit = new CircuitViewData();
		circuit.setCircuitId(101L);
		circuit.setStartItemId(itemId);
		circuit.setStartItemName("ARCH01-TEST");
		circuit.setStartPortId(100L);
		circuit.setStartPortName("Net01");
		circuit.setLocationId(loc.getDataCenterLocationId());
		circuit.setCircuitType(SystemLookup.PortClass.DATA);
		return circuit;
	}

	public CircuitViewData createDummyPowerCircuit(Long itemId){
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		CircuitViewData circuit = new CircuitViewData();
		circuit.setCircuitId(202L);
		circuit.setStartItemId(itemId);
		circuit.setStartItemName("ARCH01-TEST");
		circuit.setStartPortId(100L);
		circuit.setStartPortName("PS1");
		circuit.setLocationId(loc.getDataCenterLocationId());
		circuit.setCircuitType(SystemLookup.PortClass.POWER);
		return circuit;
	}	
}


