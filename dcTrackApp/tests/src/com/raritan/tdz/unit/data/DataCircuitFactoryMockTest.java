package com.raritan.tdz.unit.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.raritan.tdz.data.DataCircuitFactory;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.unit.circuit.DataCircuitExpectations;
import com.raritan.tdz.unit.tests.UnitTestBase;

public class DataCircuitFactoryMockTest  extends UnitTestBase  {
	@Autowired
	DataCircuitFactory circuitFactMock;

	@Autowired
	DataCircuitExpectations circuitExpectations;
    
	@Test
	public void testCreateAnyCircuit() throws Throwable {
		DataCircuit circuit = circuitFactMock.createDeviceToNetworkCircuit(null);
		
		circuitExpectations.addExpectations(circuit);
	}
}
