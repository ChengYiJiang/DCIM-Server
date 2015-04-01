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
public class CircuitDataCreationTest extends TestBase {

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
	
	@Test
	public final void testLogicalToPhysicalDataCircuit() throws Throwable {
		//connect a blade logical port to a chassis physical port 
		CircuitDTO circuit = new CircuitDTO();

		long blade_port_id = 49134L; 
		long chassis_port_id = 49133L;

		List<Long> portList = new ArrayList<Long>();		
		portList.add(blade_port_id);
		portList.add(chassis_port_id);
				
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		DataPortNodeDTO node;		
				
		for(Long portId:portList){
			node = createDataNodeUsingPort(portId);
			nodeList.add(node);
			
			if(portId.longValue() == blade_port_id){  
				nodeList.add(new VirtualWireDTO());
			}
			else if(portId.longValue() != chassis_port_id){  //last port, breaker port
				nodeList.add(new PatchCordDTO());
			}			
		}
		circuit.setNodeList(nodeList);
		
		CircuitDTO newCircuit = this.service.saveCircuit(circuit);

		AssertJUnit.assertTrue(newCircuit.getNodeList().size() == nodeList.size());

		for(CircuitNodeInterface n:newCircuit.getNodeList()){
			n.print();
		}
		
		deleteCircuit(newCircuit);
	}	
	

	@Test
	public final void testVirtualToLogicalDataCircuit() throws Throwable {
		//connect a virtual circuit port to a logical port
		//logical port is only connected to a physical port
		CircuitDTO circuit = new CircuitDTO();

		long virtual_port_id = 8066L; 
		long blade_port_id = 49134L; 
		long chassis_port_id = 49133L;

		List<Long> portList = new ArrayList<Long>();
		portList.add(virtual_port_id);
		portList.add(blade_port_id);
		portList.add(chassis_port_id);
				
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		DataPortNodeDTO node;		
				
		for(Long portId:portList){
			node = createDataNodeUsingPort(portId);
			nodeList.add(node);
			
			if(portId.longValue() == virtual_port_id || portId.longValue() == blade_port_id){  
				nodeList.add(new VirtualWireDTO());
			}
			else if(portId.longValue() != chassis_port_id){  //last port, breaker port
				nodeList.add(new PatchCordDTO());
			}			
		}
		circuit.setNodeList(nodeList);
		
		CircuitDTO newCircuit = this.service.saveCircuit(circuit);
		
		AssertJUnit.assertTrue(newCircuit.getNodeList().size() == nodeList.size());
		
		for(CircuitNodeInterface n:newCircuit.getNodeList()){
			n.print();
		}	
		
		deleteCircuit(newCircuit);
	}

	@Test
	public final void testVirtualToShareLogicalDataCircuit() throws Throwable {
		//connect a virtual circuit port to a logical port
		//logical port is used by a virtual port, port sharing test
		
		CircuitDTO circuit = new CircuitDTO();

		long virtual_port_id = 8067L; 
		long blade_port_id = 49134L; 
		long chassis_port_id = 49133L;

		List<Long> portList = new ArrayList<Long>();
		portList.add(virtual_port_id);
		portList.add(blade_port_id);
		portList.add(chassis_port_id);
				
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		DataPortNodeDTO node;		
				
		for(Long portId:portList){
			node = createDataNodeUsingPort(portId);
			nodeList.add(node);
			
			if(portId.longValue() == virtual_port_id || portId.longValue() == blade_port_id){  
				nodeList.add(new VirtualWireDTO());
			}
			else if(portId.longValue() != chassis_port_id){  //last port, breaker port
				nodeList.add(new PatchCordDTO());
			}			
		}
		circuit.setNodeList(nodeList);
		
		CircuitDTO newCircuit = this.service.saveCircuit(circuit);
		
		AssertJUnit.assertTrue(newCircuit.getNodeList().size() == nodeList.size());
		
		for(CircuitNodeInterface n:newCircuit.getNodeList()){
			n.print();
		}
		
		deleteCircuit(newCircuit);
	}
	
	@Test
	public final void testLogicalToLogicalDataCircuit() throws Throwable {
		//Connect a logical port to another logical port
		//TA3343
		CircuitDTO circuit = new CircuitDTO();

		long blade1_port_id = 49136L; 
		long blade2_port_id = 49137L;
		
		List<Long> portList = new ArrayList<Long>();
		portList.add(blade1_port_id);
		portList.add(blade2_port_id);
				
		List<CircuitNodeInterface> nodeList = new LinkedList<CircuitNodeInterface>();
		DataPortNodeDTO node;		
				
		for(Long portId:portList){
			node = createDataNodeUsingPort(portId);
			nodeList.add(node);
			
			if(portId.longValue() == blade1_port_id){  
				nodeList.add(new VirtualWireDTO());
			}
		}
		circuit.setNodeList(nodeList);
		
		CircuitDTO newCircuit = this.service.saveCircuit(circuit);
		
		AssertJUnit.assertTrue(newCircuit.getNodeList().size() == nodeList.size());
		
		for(CircuitNodeInterface n:newCircuit.getNodeList()){
			n.print();
		}	
		
		deleteCircuit(newCircuit);
	}
	
	private PowerPortNodeDTO createPowerNodeUsingPort(long portId){
		PowerPortNodeDTO node = new PowerPortNodeDTO();
		
		this.session = sf.getCurrentSession();
		
		PowerPort port = (PowerPort)session.get(PowerPort.class, portId);
		
		node.setPortId(portId);
		node.setItemClassLksValueCode(port.getItem().getClassLookup().getLkpValueCode());
		node.setItemId(port.getItem().getItemId());
		node.setPortSubClassLksValueCode(port.getPortSubClassLookup().getLkpValueCode());
		node.setUsed(port.getUsed());
		
		return node;
	}
	
	private DataPortNodeDTO createDataNodeUsingPort(long portId){
		DataPortNodeDTO node = new DataPortNodeDTO();
		
		this.session = sf.getCurrentSession();
		
		DataPort port = (DataPort)session.get(DataPort.class, portId);
		
		node.setPortId(portId);
		node.setItemClassLksValueCode(port.getItem().getClassLookup().getLkpValueCode());
		node.setItemId(port.getItem().getItemId());
		node.setPortSubClassLksValueCode(port.getPortSubClassLookup().getLkpValueCode());
		node.setUsed(port.getUsed());
		
		return node;
	}
	
	private void deleteCircuit(CircuitDTO circuit) throws ServiceLayerException{

		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();
		CircuitCriteriaDTO rec = new CircuitCriteriaDTO();
		rec.setCircuitId(circuit.getCircuitId());
		rec.setCircuitType(circuit.getCircuitType());
		recList.add(rec);
		
		this.service.deleteCircuitByIds(recList);		
	}
	
}


