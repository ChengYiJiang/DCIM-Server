package com.raritan.tdz.circuit.service;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.dto.DataPortNodeDTO;
import com.raritan.tdz.circuit.dto.PatchCordDTO;
import com.raritan.tdz.circuit.dto.PowerCableDTO;
import com.raritan.tdz.circuit.dto.PowerPortNodeDTO;
import com.raritan.tdz.circuit.dto.StructureCableDTO;
import com.raritan.tdz.circuit.dto.VirtualWireDTO;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Circuit business logic tests. Only add tests that create new circuits 
 * 
 * @author Santo Rosario
 */
public class CircuitButtonStatusTest extends TestBase {

	private CircuitPDService service;
	

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		service = (CircuitPDService)ctx.getBean("circuitPDService");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
	/*@Test
	public void testgetCircuitButtonStatus(){
		List<Float> circuitIdList = new ArrayList<Float>();
		circuitIdList.add(CircuitUID.getCircuitUID(12015L, 30000L));
		
		try {
			String status = service.getCircuitButtonStatus(circuitIdList );
			
			System.out.println(status);
			
		} catch (ServiceLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}