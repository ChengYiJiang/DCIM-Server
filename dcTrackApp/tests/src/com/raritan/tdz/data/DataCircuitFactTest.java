package com.raritan.tdz.data;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.tests.TestBase;

public class DataCircuitFactTest  extends TestBase  {
	DataCircuitFactory circuitFact;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		circuitFact = (DataCircuitFactory)ctx.getBean("dataCircuitFact");
	}
	
	@Test
	public void testCreateAnyCircuit() throws Throwable {
		DataCircuit circuit = circuitFact.createDeviceToNetworkCircuit(null);
			
		this.addTestItemList(circuitFact.getCircuitItemIds(circuit));
		
		circuitFact.deleteCircuit(circuit);
		
	}
}
