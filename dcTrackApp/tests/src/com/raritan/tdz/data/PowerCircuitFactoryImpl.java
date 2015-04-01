package com.raritan.tdz.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class PowerCircuitFactoryImpl implements PowerCircuitFactory {
	@Autowired
	PowerPortDAO portDAO;
	@Autowired
	PowerConnDAO connDAO;
	@Autowired
	PowerCircuitDAO circuitDAO;
	@Autowired
	PowerPortFactory portFact;
	@Autowired
	PowerConnFactory powerConnFact;
	@Autowired
	ItemFactory itemFact;
	@Autowired
	PowerChainFactory powerChainFact;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupDAO;

	@Autowired
	ConnectorLookupFinderDAO connectorLookupDAO;
	
	private GenericObjectSave circuitSave;
	
	public GenericObjectSave getCircuitSave() {
		return circuitSave;
	}

	public void setCircuitSave(GenericObjectSave circuitSave) {
		this.circuitSave = circuitSave;
	}

	public PowerCircuitFactoryImpl() {
		
	}
	
	@Override
	public PowerCircuit getCircuit(Long circuitId){
		return circuitDAO.read(circuitId);
	}
	
	@Override
	public PowerCircuit createRPDUToFloorOutlet() throws Throwable{
		PowerCircuit circuit = null;

		List<PowerConnection> connList = powerChainFact.createPowerChange("PC1");
		PowerPort breaker = getBCBreakerFromChain(connList, "208", 30, SystemLookup.PhaseIdClass.SINGLE_3WIRE);		

		Item outlet = itemFact.createPowerOutlet(null, null);
		PowerPort outletPort = portFact.createPortsForItem(outlet, "R", SystemLookup.PortSubClass.WHIP_OUTLET, 1, null, "208", 30);		
		
		Item rpdu = itemFact.createRPDU(null, null);
		PowerPort inputCord = portFact.createPortsForItem(rpdu, "Input Cord", SystemLookup.PortSubClass.INPUT_CORD, 1, null, "208", 30);
		
		connList = powerChainFact.getEndingNodes(breaker);
		
		connList.add(0, powerConnFact.createConnExplicit(inputCord, outletPort, null));
		connList.add(1, powerConnFact.createConnImplicit(outletPort, breaker, null));
		
		circuit = createCircuit(connList);

		return circuit;
	}
			
	@Override
	public PowerCircuit createCircuit(List<PowerConnection> circuitConnections){
		savePorts(circuitConnections);
		saveConnections(circuitConnections);
		
		PowerConnection endConn = circuitConnections.get(circuitConnections.size() - 1);
		
		//Use new connectionId saved
		String circuitTrace = createTrace(circuitConnections);
		
		PowerCircuit circuit = new PowerCircuit();
		circuit.setStartConnection(circuitConnections.get(0));
		circuit.setEndConnection(circuitConnections.get(circuitConnections.size()-1));		
		circuit.setCircuitConnections(circuitConnections);
		circuit.setCircuitTrace(circuitTrace);
		
		circuitSave.save(circuit);
		
		return circuit;
	}	
	
	
	@Override
	public List<Long> getCircuitItemIds(PowerCircuit circuit){
		List<Long> tempList = new ArrayList<Long>();
		
		if(circuit == null) return tempList;
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			tempList.add(conn.getSourceItem().getItemId());
		}
		
		tempList.add(powerChainFact.getCurrentUPS().getItemId());
		
		return tempList;
	}

	
	@Override
	public List<Item> getCircuitItems(PowerCircuit circuit){
		List<Item> tempList = new ArrayList<Item>();
		
		if(circuit == null) return tempList;
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			Item item = conn.getSourceItem();
			tempList.add(item);
		}
		tempList.add(powerChainFact.getCurrentUPS());
		
		return tempList;
	}
	
	@Override
	public List<Long> getCircuitPortIds(PowerCircuit circuit){
		List<Long> tempList = new ArrayList<Long>();
		
		if(circuit == null) return tempList;
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			tempList.add(conn.getSourcePortId());
		}
		
		return tempList;
	}
	
	
	@Override
	public void deleteCircuit(PowerCircuit circuit, boolean doBreaker){
		if(circuit == null) return;
		
		circuitDAO.delete(circuit);
		
		for(Long connId:circuit.getConnListFromTrace()){
			PowerConnection conn = connDAO.read(connId);
			PowerPort port = conn.getSourcePowerPort();
			
			if((port.isBreaker() || port.isOutlet()) && doBreaker == false) break;
			
			if(conn != null){
				connDAO.delete(conn);
			}
		}
	}

	@Override
	public PowerCircuit mergeCircuits(PowerCircuit startCircuit, PowerCircuit endCircuit){
		List<PowerConnection> connList = new ArrayList<PowerConnection>();
		
		PowerConnection endConn = endCircuit.getStartConnection();
		
		for(PowerConnection conn:startCircuit.getCircuitConnections()){
			connList.add(conn);
		}
		
		connList.get(connList.size() - 1).setDestPowerPort(endConn.getSourcePowerPort());
		connList.addAll(endCircuit.getCircuitConnections());
		
		PowerCircuit circuit = createCircuit(connList);
		
		return circuit;
	}

	
	@Override
	public PowerCircuit addPortToStartOfCircuit(PowerPort startPort, PowerCircuit endCircuit){
		List<PowerConnection> connList = new ArrayList<PowerConnection>();
		
		PowerPort destPort = endCircuit.getCircuitConnections().get(0).getSourcePowerPort();
		PowerConnection conn = powerConnFact.createConnExplicit(startPort, destPort, null);
		
		connList.add(conn);
		connList.addAll(endCircuit.getCircuitConnections());
		
		PowerCircuit circuit = createCircuit(connList);
		
		return circuit;
	}	
	
	@Override
	public void printCircuit(PowerCircuit circuit){
		System.out.println("\n\n======================================================");
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			System.out.println(conn.getSourceItem().getItemName() + ":"  + conn.getSourcePortName() + " ==> " + conn.getDestPortName());
		
		}
		System.out.println("======================================================\n");
		
	}

	private String createTrace(List<PowerConnection> connList){
		String trace = ",";
		
		for(PowerConnection c:connList){
			trace += c.getConnectionId() + ",";
		}
		return trace;
	}

	private void savePorts(List<PowerConnection> connList){
		for(PowerConnection c:connList){
			PowerPort port = c.getSourcePowerPort();
			
			if(port.isBreaker()) break;
			
			if(port.getPortId() == null){
				circuitSave.save(port);
			}
			else{
				circuitSave.update(port);
			}
		}
	}

	private void saveConnections(List<PowerConnection> connList){
		for(PowerConnection c:connList){
			PowerPort port = c.getSourcePowerPort();
			
			if(port.isBreaker()) break;
			
			if(c.getConnectionId() > 0){
				circuitSave.update(c);
			}
			else{
				circuitSave.save(c);
			}
		}
	}

	
	private PowerPort getBCBreakerFromChain(List<PowerConnection> connList, String volts, int amps, Long phase){
		Item panel = connList.get(0).getSourceItem();
		
		for(PowerPort port:panel.getPowerPorts()){
			if(!port.isBranchCircuitBreaker()) continue;
			
			if(port.getAmpsNameplate() != amps) continue;
			
			if(!port.getPhaseLookup().getLkpValueCode().equals(phase)) continue;
			
			if(port.getVoltsLookup().getLkpValue().equals(volts)) return port;
		}
		
		return null;
		
	}
		
		
}
