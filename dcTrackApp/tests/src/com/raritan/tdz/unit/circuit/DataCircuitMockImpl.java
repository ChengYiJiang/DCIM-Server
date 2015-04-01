package com.raritan.tdz.unit.circuit;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.circuit.connection.DataConnMock;
import com.raritan.tdz.unit.item.ItemMock;
import com.raritan.tdz.unit.item.port.DataPortMock;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class DataCircuitMockImpl {
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;
	 
	@Autowired
	protected Mockery jmockContext;
	 
	@Autowired
	protected DataCircuitDAO circuitDAO; 
	
	@Autowired
	protected ItemMock itemMock;
	
	@Autowired
	protected DataPortMock dataPortMock;
	
	@Autowired
	protected DataConnMock dataConnMock;
	
	@Autowired
	protected DataCircuitExpectations expectations;
	
	public DataCircuit createDeviceToNetworkCircuit(){
		List<DataConnection> circuitConnections = new ArrayList<DataConnection>();
		
		Item startItem = itemMock.createRackableStandardDeviceItem(null, 1);
		Item endItem = itemMock.createRackableFixedNetworkStackItem(null, 1);
		
		DataPort sourcePort = dataPortMock.createPortsForItem(jmockContext, startItem, SystemLookup.PortSubClass.ACTIVE, 1, true);
		DataPort destPort = dataPortMock.createPortsForItem(jmockContext, endItem, SystemLookup.PortSubClass.ACTIVE, 1, true);
				
		DataConnection startConn = dataConnMock.createConnPlannedExplicit(jmockContext, sourcePort, destPort);
		circuitConnections.add(startConn);
		
		DataConnection endConn = dataConnMock.createConnPlannedExplicit(jmockContext, destPort, null);
		circuitConnections.add(endConn);
		
		DataCircuit circuit = createCircuit(circuitConnections);
		
		return circuit;
	}
	
	private String createTrace(List<DataConnection> connList){
		String trace = ",";
		
		for(DataConnection c:connList){
			trace += c.getConnectionId() + ",";
		}
		return trace;
	}

	public DataCircuit createCircuit(List<DataConnection> circuitConnections){

		String circuitTrace = createTrace(circuitConnections);
		
		DataCircuit circuit = new DataCircuit();
		circuit.setCircuitId(unitTestIdGenerator.nextId());
		circuit.setStartConnection(circuitConnections.get(0));
		circuit.setEndConnection(circuitConnections.get(circuitConnections.size()-1));		
		circuit.setCircuitConnections(circuitConnections);
		circuit.setCircuitTrace(circuitTrace);
		
		Long circuitId = circuit.getDataCircuitId();
		List<DataCircuit> recList = new ArrayList<DataCircuit>();
		recList.add(circuit);
		
		expectations.createLoadCircuit(jmockContext, circuitId, circuit);
		expectations.createRead(jmockContext, circuitId, circuit);
		
		for(DataConnection c:circuitConnections){
			expectations.createViewDataCircuitByConnId(jmockContext, c.getConnectionId(), recList);
		}
		
		Long startPortId = circuitConnections.get(0).getSourcePortId();
		
		expectations.createViewDataCircuitByStartPortId(jmockContext, startPortId, recList);
		
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();		
		cCriteria.setCircuitId( CircuitUID.getCircuitUID(circuitId, SystemLookup.PortClass.DATA));
		cCriteria.setCircuitType(SystemLookup.PortClass.DATA);
		expectations.createViewDataCircuitByCriteria(jmockContext, cCriteria , recList);
		
		return circuit;
	}
}
