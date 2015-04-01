
package com.raritan.tdz.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.powerchain.home.PortFactory;

public class PowerChainFactoryImpl implements PowerChainFactory {
	@Autowired
	PowerPortFactory portFact;
	
	@Autowired
	PowerConnFactory powerConnFact;
	
	@Autowired
	ItemFactory itemFact;
	
	@Autowired
	PortFactory upsBankPortFactory;

	@Autowired
	PortFactory floorPduPortFactory;
	
	@Autowired
	PortFactory powerPanelPortFactory;

	@Autowired
	private SystemLookupFinderDAO systemLookupDAO;

	@Autowired
	ConnectorLookupFinderDAO connectorLookupDAO;
	
	@Autowired
	PowerConnDAO connDAO;
	
	private MeItem currentUpsBank;
	private MeItem currentUPS;
	private List<PowerConnection> powerChainConnsList;
	

	public PowerChainFactoryImpl() {
		powerChainConnsList = new ArrayList<PowerConnection>();
	}

	@Override
	public MeItem getCurrentUpsBank() {
		return currentUpsBank;
	}

	@Override
	public MeItem getCurrentUPS() {
		return currentUPS;
	}
	
	@Override
	public List<PowerConnection> createPowerChange(String name) throws Throwable{
		Errors errors = null;
		currentUpsBank = itemFact.createUPSBank3PhaseWYE(name +" BANK-A", null);
		currentUPS = itemFact.createUPS3PhaseWYE(name +" UPS-A", null, currentUpsBank);
		MeItem floorPdu = itemFact.createFloorPDU3PhaseWYE(name +" PDU-A", null, currentUpsBank);
		MeItem powerPanel = itemFact.createPowerPanel3PhaseWYE(floorPdu, "PB1", SystemLookup.SubClass.LOCAL, null);
		
		PowerPort upsBankBreaker = (PowerPort)upsBankPortFactory.get(currentUpsBank, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, errors );
		PowerPort fpduBreaker = (PowerPort)floorPduPortFactory.get(floorPdu, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);		
		
		//Add pole to panels
		portFact.createPortsForPanel(powerPanel, 12);

		PowerPort panelBreaker = (PowerPort)powerPanelPortFactory.get(powerPanel, SystemLookup.PortSubClass.PANEL_BREAKER, errors);

		createOnePoleBreakersForPanel(powerPanel, panelBreaker, "120", 15, 4, 1);
		createTwoPoleBreakersForPanel(powerPanel, panelBreaker, "208", 30, 4, 5);
		createThreePoleBreakersForPanel(powerPanel, panelBreaker, "208", 30, 4, 9);
		
		//link input breaker from panel to branch circuit port
		for(PowerPort port:powerPanel.getPowerPorts()){
			itemFact.save(port);
			
			if(!port.isBranchCircuitBreaker()) continue;
			
			PowerConnection conn = powerConnFact.createConnImplicit(port, panelBreaker, null);
			
			itemFact.save(conn);
		}
				
		List<PowerConnection> connList = new ArrayList<PowerConnection>();		
		connList.add(powerConnFact.createConnImplicit(panelBreaker, fpduBreaker, null));
		connList.add(powerConnFact.createConnImplicit(fpduBreaker, upsBankBreaker, null));
		connList.add(powerConnFact.createConnImplicit(upsBankBreaker, null, null));
		
		for(PowerConnection conn:connList){
			itemFact.save(conn);
		}
		
		powerChainConnsList.addAll(connList);
		
		return connList;
	}
	
	@Override
	public void createOnePoleBreakersForPanel(MeItem panel, PowerPort panelBreaker, String volts, int amps, int quantity, int startPort){
		//Code create a default breaker layout
		LksData voltsLks = systemLookupDAO.findByLkpValueAndType(volts, "VOLTS").get(0);
		LksData phaseLks = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0);
		ConnectorLkuData connector = connectorLookupDAO.findByName("1-Pole Breaker").get(0);
		
		int count = 0;
		
		PowerPort[] portList = panel.getPowerPorts().toArray(new PowerPort[0]);
		Arrays.sort(portList, new PortComparator());
		
		for(int i=0; i<portList.length; i++){
			PowerPort port = portList[i];
			
			if(!port.isBranchCircuitBreaker()) continue;
			
			if(port.getSortOrder() < startPort) continue;
			
			port.setAmpsNameplate(amps);
			port.setVoltsLookup(voltsLks);
			port.setPhaseLookup(phaseLks);
			port.setConnectorLookup(connector);
			
			count++;
			
			if(count == quantity) break;
		}
	}

	@Override
	public void createTwoPoleBreakersForPanel(MeItem panel, PowerPort panelBreaker, String volts, int amps, int quantity, int startPort){
		//Code create a default breaker layout
		LksData voltsLks = systemLookupDAO.findByLkpValueAndType(volts, "VOLTS").get(0);
		LksData phaseLks = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0);

		LksData poleLksA = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.AB).get(0);
		LksData poleLksB = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.BC).get(0);
		LksData poleLksC = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.CA).get(0);
		List<LksData> polesLookup = Arrays.asList(poleLksA, poleLksB, poleLksC);
		
		/*
			77;"Breaker";"1-Pole Breaker";"1";"";;""
			78;"Breaker";"2-Pole Breaker";"2";"";;""
			79;"Breaker";"3-Pole Breaker";"3";"";;""
		 */
		ConnectorLkuData connector = connectorLookupDAO.findByName("2-Pole Breaker").get(0);
		
		int idx = 0;
		int count = 0;
		
		PowerPort[] portList = panel.getPowerPorts().toArray(new PowerPort[0]);
		Arrays.sort(portList, new PortComparator());
		
		for(int i=0; i<portList.length; i++){
			PowerPort port = portList[i];
			
			if(!port.isBranchCircuitBreaker()) continue;
			
			if(port.getSortOrder() < startPort) continue;
			
			count += 2;
			
			if(count > quantity || count > portList.length) break;
			
			port.setAmpsNameplate(amps);
			port.setVoltsLookup(voltsLks);
			port.setPhaseLookup(phaseLks);
			port.setPhaseLegsLookup(polesLookup.get(idx));
			port.setConnectorLookup(connector);
			port.setPortName(port.getPortName() + "," + portList[i+1].getPortName());
			portList[i+1].setBreakerPort(port);
			i++;
			
			idx++;
			
			if(idx == 3) idx = 0;
		}
	}

	@Override
	public void createThreePoleBreakersForPanel(MeItem panel, PowerPort panelBreaker, String volts, int amps, int quantity, int startPort){
		//Code create a default breaker layout
		LksData voltsLks = systemLookupDAO.findByLkpValueAndType(volts, "VOLTS").get(0);
		LksData phaseLks = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseIdClass.THREE_WYE).get(0);
		LksData poleLksABC = systemLookupDAO.findByLkpValueCode(SystemLookup.PhaseLegClass.ABC).get(0);
		ConnectorLkuData connector = connectorLookupDAO.findByName("3-Pole Breaker").get(0);
		
		int count = 0;

		PowerPort[] portList = panel.getPowerPorts().toArray(new PowerPort[0]);
		Arrays.sort(portList, new PortComparator());
		
		for(int i=0; i<portList.length; i++){
			PowerPort port = portList[i];
			
			if(!port.isBranchCircuitBreaker()) continue;
			
			if(port.getSortOrder() < startPort) continue;
			
			count += 3;
			
			if(count > quantity || count > portList.length) break;
			
			port.setAmpsNameplate(amps);
			port.setVoltsLookup(voltsLks);
			port.setPhaseLookup(phaseLks);
			port.setPhaseLegsLookup(poleLksABC);
			port.setConnectorLookup(connector);
			port.setPortName(port.getPortName() + "," + portList[i+1].getPortName() + "," + portList[i+2].getPortName());
			portList[i+1].setBreakerPort(port);
			portList[i+2].setBreakerPort(port);
			i += 2;
		}
	}	
	
	class PortComparator implements Comparator
	{
		@Override
		public int compare(Object arg0, Object arg1) {
			PowerPort p1 = (PowerPort) arg0;
			PowerPort p2 = (PowerPort) arg1;
			
			if(p1.getSortOrder() > p2.getSortOrder()) return 1;
			if(p1.getSortOrder() == p2.getSortOrder()) return 0;
			
			return -1;
		}
	}	

	@Override
	public void printPowerNode(MeItem node){
		System.out.println("\n====================================================================");
		System.out.println("NODE:" + node.getItemName() + "\t\tCLASS: " + node.getClassLookup().getLkpValue());

		if(node.getPowerPorts() == null || node.getPowerPorts().size() == 0){
			System.out.println("ITEM HAS NO PORTS");
			System.out.println("====================================================================\n");
			return;
		}
		
		System.out.println("------------------------------------------------------------------------");
		
		PowerPort[] portList = node.getPowerPorts().toArray(new PowerPort[0]);
		Arrays.sort(portList, new PortComparator());
		
		for(PowerPort port:portList){
			if(!port.isBranchCircuitBreaker()){
				System.out.println("PORT ID:    \t" + port.getPortId());
				System.out.println("PORT NAME:    \t" + port.getPortName());
				System.out.println("SUBCLASS:     \t" + port.getPortSubClassLookup().getLkpValue());
				System.out.println("BREAKER AMPS: \t" + port.getAmpsNameplate());
				System.out.println("BREAKER: VOLTS\t: " + port.getVoltsLookup().getLkpValue());
				System.out.println("BREAKER PHASE:\t" + port.getPhaseLookup().getLkpValue());
			}
		}
		System.out.println("------------------------------------------------------------------------");
		
		String name;
		
		for(PowerPort port:portList){
			if(!port.isBranchCircuitBreaker()) continue;
			
			if(port.getAmpsNameplate() > 0){
				name = "Breaker: ";
			}
			else{
				name = "Pole:    ";
			}
			System.out.println("PORT ID:    \t" + port.getPortId() + "\t" + name  + "\t" + port.getPortName() + "\t\tAMPS: " + port.getAmpsNameplate() + "\tVOLTS: "+ port.getVoltsLookup().getLkpValue() +"\tPhase: " + port.getPhaseLookup().getLkpValue());
		}		
		System.out.println("====================================================================\n");
	}
	
	@Override
	public List<PowerConnection> getEndingNodes(PowerPort port){
		List<PowerConnection> recList = new ArrayList<PowerConnection>();		
		List<PowerConnection> connsList = connDAO.getConnectionsForSourcePort(port.getPortId());
		PowerConnection conn = null;
		
		if(connsList != null && connsList.size() > 0){
			conn = connsList.get(0);
		}
		
		while(conn != null){
			recList.add(conn);
			
			System.out.println(conn.getDestPortId());
			
			connsList = connDAO.getConnectionsForSourcePort(conn.getDestPortId());
			conn = null;
			
			if(connsList != null && connsList.size() > 0){
				conn = connsList.get(0);
			}
		}
		
		return recList;
	}
	
	@Override
	public void deletePowerChainConns(){
		for(PowerConnection conn:powerChainConnsList){
			Item item = conn.getSourceItem();
			
			try {
				List<PowerConnection> cList = connDAO.getConnsForItem(item.getItemId());

				if(cList == null) continue;

				for(PowerConnection c:cList){
					connDAO.delete(c);
				}						
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
							
		}
		
	}
}
