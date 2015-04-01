package com.raritan.tdz.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.DataPortDAO;

public class DataCircuitFactoryImpl implements DataCircuitFactory {
	@Autowired
	DataPortDAO portDAO;
	@Autowired
	DataConnDAO connDAO;
	@Autowired
	DataCircuitDAO circuitDAO;
	@Autowired
	DataPortFactory portFact;
	@Autowired
	DataConnFactory dataConnFact;
	@Autowired
	ItemFactory itemFactory;
	
	private GenericObjectSave circuitSave;
	
	public GenericObjectSave getCircuitSave() {
		return circuitSave;
	}

	public void setCircuitSave(GenericObjectSave circuitSave) {
		this.circuitSave = circuitSave;
	}

	public DataCircuitFactoryImpl() {
		
	}

	@Override
	public DataCircuit getCircuit(Long circuitId){
		return circuitDAO.read(circuitId);
	}
	
	@Override
	public DataCircuit createDeviceToNetworkCircuit(Long statusValueCode){
		Item startItem = null;
		Item endItem = null;
		
		try {
			startItem = itemFactory.createDevice(null, statusValueCode);
			endItem = itemFactory.createNetworkStack(null, "STACK-01", statusValueCode);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Item> itemList = new ArrayList<Item>();
		itemList.add(startItem);
		itemList.add(endItem);
		
		DataCircuit circuit = createCircuitUsingItems(itemList, statusValueCode);
		
		return circuit;
	}

	@Override
	public DataCircuit createDeviceToNetworkCircuit(Long statusValueCode, Item cabinet) {
		Item startItem = null;
		Item endItem = null;
		
		try {
			startItem = itemFactory.createDevice(null, statusValueCode, cabinet);
			endItem = itemFactory.createNetworkStack(null, "STACK-01", statusValueCode, cabinet);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Item> itemList = new ArrayList<Item>();
		itemList.add(startItem);
		itemList.add(endItem);
		
		DataCircuit circuit = createCircuitUsingItems(itemList, statusValueCode);
		
		return circuit;
	}

	@Override
	public DataCircuit createDevice2Panel2Network(Long statusValueCode){
		List<Item> itemList = new ArrayList<Item>();
		
		try {
			Item item = itemFactory.createDevice(null, statusValueCode);
			itemList.add(item);
			
			item = itemFactory.createDataPanel(null, statusValueCode);
			itemList.add(item);

			item = itemFactory.createDataPanel(null, statusValueCode);
			itemList.add(item);
			
			item = itemFactory.createNetworkStack(null, "STACK-01", statusValueCode);
			itemList.add(item);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}				
		
		DataCircuit circuit = createCircuitUsingItems(itemList, statusValueCode);
		
		return circuit;
	}	

	@Override
	public DataCircuit createDataPanelToNetwork(Long statusValueCode){
		List<Item> itemList = new ArrayList<Item>();
		
		try {
			Item item = itemFactory.createDataPanel(null, statusValueCode);
			itemList.add(item);

			item = itemFactory.createDataPanel(null, statusValueCode);
			itemList.add(item);
			
			item = itemFactory.createNetworkStack(null, "STACK-01", statusValueCode);
			itemList.add(item);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}				
		
		DataCircuit circuit = createCircuitUsingItems(itemList, statusValueCode);
		
		return circuit;
	}
	
	@Override
	public DataCircuit createCircuitUsingItems(List<Item> itemList, Long statusValueCode){
		List<DataPort> portList = new ArrayList<DataPort>();

		for(Item item:itemList){
			portList.add(portFact.createPortsForItem(item, SystemLookup.PortSubClass.ACTIVE, 1));
		}
			
		List<DataConnection> connList = dataConnFact.createConnectionUsingPortList(portList, statusValueCode);
		
		DataCircuit circuit = createCircuit(connList);
		
		return circuit;
	}
	
	private String createTrace(List<DataConnection> connList){
		String trace = ",";
		
		for(DataConnection c:connList){
			trace += c.getConnectionId() + ",";
		}
		return trace;
	}

	private void savePorts(List<DataConnection> connList){
		for(DataConnection c:connList){
			circuitSave.save(c.getSourceDataPort());
		}
	}

	private void saveConnections(List<DataConnection> connList){
		for(DataConnection c:connList){
			circuitSave.save(c);
		}
	}
	
	@Override
	public DataCircuit createCircuit(List<DataConnection> circuitConnections){
		savePorts(circuitConnections);
		saveConnections(circuitConnections);
		
		//Use new connectionId saved
		String circuitTrace = createTrace(circuitConnections);
		
		DataCircuit circuit = new DataCircuit();
		circuit.setStartConnection(circuitConnections.get(0));
		circuit.setEndConnection(circuitConnections.get(circuitConnections.size()-1));		
		circuit.setCircuitConnections(circuitConnections);
		circuit.setCircuitTrace(circuitTrace);
		
		circuitSave.save(circuit);
		
		return circuit;
	}	
	
	@Override
	public List<Long> getCircuitItemIds(DataCircuit circuit){
		List<Long> tempList = new ArrayList<Long>();
		
		if(circuit == null) return tempList;
		
		for(DataConnection conn:circuit.getCircuitConnections()){
			tempList.add(conn.getSourceItem().getItemId());
		}
		
		return tempList;
	}

	@Override
	public List<Item> getCircuitItems(DataCircuit circuit){
		List<Item> tempList = new ArrayList<Item>();
		
		if(circuit == null) return tempList;
		
		for(DataConnection conn:circuit.getCircuitConnections()){
			tempList.add(conn.getSourceItem());
		}
		
		return tempList;
	}
	
	public List<Long> getCircuitPortIds(DataCircuit circuit) {
		List<Long> tempList = new ArrayList<Long>();
		
		if(circuit == null) return tempList;
		
		for(DataConnection conn:circuit.getCircuitConnections()){
			tempList.add(conn.getSourcePortId());
		}
		
		return tempList;
	}
	
	@Override
	public void deleteCircuit(DataCircuit circuit){
		if(circuit == null) return;
		
		circuitDAO.delete(circuit);
		
		for(Long connId:circuit.getConnListFromTrace()){
			DataConnection conn = connDAO.read(connId);
			
			if(conn != null){
				connDAO.delete(conn);
			}
		}
	}

	@Override
	public DataCircuit mergeCircuits(DataCircuit startCircuit, DataCircuit endCircuit){
		List<DataConnection> connList = new ArrayList<DataConnection>();
		
		DataConnection endConn = endCircuit.getStartConnection();
		
		for(DataConnection conn:startCircuit.getCircuitConnections()){
			connList.add(conn);
		}
		
		connList.get(connList.size() - 1).setDestDataPort(endConn.getSourceDataPort());
		connList.addAll(endCircuit.getCircuitConnections());
		
		DataCircuit circuit = createCircuit(connList);
		
		return circuit;
	}

	@Override
	public DataCircuit addItemToStartOfCircuit(Item startItem, DataCircuit endCircuit){
		List<DataConnection> connList = new ArrayList<DataConnection>();
		
		DataPort destPort = endCircuit.getCircuitConnections().get(0).getSourceDataPort();
		DataPort startPort = portFact.createPortsForItem(startItem, SystemLookup.PortSubClass.ACTIVE, 1);
		DataConnection conn = dataConnFact.createConnPlannedExplicit(startPort, destPort, null);
		
		connList.add(conn);
		connList.addAll(endCircuit.getCircuitConnections());
		
		DataCircuit circuit = createCircuit(connList);
		
		return circuit;
	}	
	
	@Override
	public DataCircuit createFanoutCircuit(Long statusValueCode){
		List<Item> itemList = new ArrayList<Item>();
		
		try {
			Item item = itemFactory.createDataPanel(null, statusValueCode);
			itemList.add(item);

			item = itemFactory.createNetworkStack(null, "STACK-01", statusValueCode);
			itemList.add(item);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}				
		
		DataCircuit circuit = createCircuitUsingItems(itemList, statusValueCode);
		
		return circuit;
	}
		
}
