package com.raritan.tdz.circuit.power;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class CircuitTestBase extends TestBase {

	// phase, wire, column
	private Map<String, Map<Long, String>> portNamePoleMapFactory = new HashMap<String, Map<Long,String>>();
	
	private Map<Long, String> portNamePoleMap_3Phase_2Column_OddEven = null;
	private Map<Long, String> portNamePoleMap_3Phase_1Column_OddEven = null;
	
	private Map<Long, String> portNamePoleMap_1Phase2Wire = null;

	private Map<Long, String> portNamePoleMap_1Phase3Wire_2Column = null;
	private Map<Long, String> portNamePoleMap_1Phase3wire_1Column = null;
	
	private Map<String, Long> portPhasePoleWithPhaseLegsMap = null;
	
	private List<Long> circuitItemsId;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		
		super.setUp();
		
		circuitItemsId = new ArrayList<Long>();
		
		// Note: for three phase item mod value is 6
		portNamePoleMap_3Phase_2Column_OddEven = new HashMap<Long, String>();
		portNamePoleMap_3Phase_2Column_OddEven.put(1L, "A"); portNamePoleMap_3Phase_2Column_OddEven.put(2L, "A");
		portNamePoleMap_3Phase_2Column_OddEven.put(3L, "B"); portNamePoleMap_3Phase_2Column_OddEven.put(4L, "B");
		portNamePoleMap_3Phase_2Column_OddEven.put(5L, "C"); portNamePoleMap_3Phase_2Column_OddEven.put(0L, "C");
		// item phase:leg
		portNamePoleMapFactory.put("3:2", portNamePoleMap_3Phase_2Column_OddEven);
		
		portNamePoleMap_3Phase_1Column_OddEven = new HashMap<Long, String>();
		portNamePoleMap_3Phase_1Column_OddEven.put(1L, "A"); 
		portNamePoleMap_3Phase_1Column_OddEven.put(2L, "B");
		portNamePoleMap_3Phase_1Column_OddEven.put(3L, "C"); 
		portNamePoleMap_3Phase_1Column_OddEven.put(4L, "A");
		portNamePoleMap_3Phase_1Column_OddEven.put(5L, "B"); 
		portNamePoleMap_3Phase_1Column_OddEven.put(0L, "C");
		portNamePoleMapFactory.put("3:1", portNamePoleMap_3Phase_1Column_OddEven);

		// Note: for single phase mod value is 4
		portNamePoleMap_1Phase2Wire = new HashMap<Long, String>();
		portNamePoleMap_1Phase2Wire.put(1L, "A"); 
		portNamePoleMap_1Phase2Wire.put(2L, "A");
		portNamePoleMap_1Phase2Wire.put(3L, "A"); 
		portNamePoleMap_1Phase2Wire.put(0L, "A");
		// phase:wire:col
		portNamePoleMapFactory.put("1:2:2", portNamePoleMap_1Phase2Wire);
		
		portNamePoleMapFactory.put("1:2:1", portNamePoleMap_1Phase2Wire);
		
		portNamePoleMap_1Phase3Wire_2Column = new HashMap<Long, String>();
		portNamePoleMap_1Phase3Wire_2Column.put(1L, "A"); 
		portNamePoleMap_1Phase3Wire_2Column.put(2L, "A");
		portNamePoleMap_1Phase3Wire_2Column.put(3L, "B"); 
		portNamePoleMap_1Phase3Wire_2Column.put(0L, "B");
		portNamePoleMapFactory.put("1:3:2", portNamePoleMap_1Phase3Wire_2Column);
		
		portNamePoleMap_1Phase3wire_1Column = new HashMap<Long, String>();
		portNamePoleMap_1Phase3wire_1Column.put(1L, "A"); 
		portNamePoleMap_1Phase3wire_1Column.put(2L, "B");
		portNamePoleMap_1Phase3wire_1Column.put(3L, "A"); 
		portNamePoleMap_1Phase3wire_1Column.put(0L, "B");
		portNamePoleMapFactory.put("1:3:1", portNamePoleMap_1Phase3Wire_2Column);
		
		
		portPhasePoleWithPhaseLegsMap = new HashMap<String, Long>();
		// Single Phase - port phase:wire:leg
		portPhasePoleWithPhaseLegsMap.put("1:2:A", SystemLookup.PhaseLegClass.A);
		portPhasePoleWithPhaseLegsMap.put("1:2:B", SystemLookup.PhaseLegClass.B);
		portPhasePoleWithPhaseLegsMap.put("1:2:C", SystemLookup.PhaseLegClass.C);
		
		// 2 phase port
		portPhasePoleWithPhaseLegsMap.put("1:3:A", SystemLookup.PhaseLegClass.AB);
		portPhasePoleWithPhaseLegsMap.put("1:3:B", SystemLookup.PhaseLegClass.BC);
		portPhasePoleWithPhaseLegsMap.put("1:3:C", SystemLookup.PhaseLegClass.CA);
		
		// 3 phase - port phase:leg
		portPhasePoleWithPhaseLegsMap.put("3:A", SystemLookup.PhaseLegClass.ABC);
		portPhasePoleWithPhaseLegsMap.put("3:B", SystemLookup.PhaseLegClass.ABC);
		portPhasePoleWithPhaseLegsMap.put("3:C", SystemLookup.PhaseLegClass.ABC);

	}
	
	public void addCircuitItem(Item item){
		
		circuitItemsId.add(item.getItemId());
	}

	protected final MeItem createUPSBank(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setUPosition(-9);
		item.setMountedRailLookup( SystemLookup.getLksData(session, SystemLookup.RailsUsed.BOTH) );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.UPS_BANK) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED) );
		item.setModel(getModel(1304L)); // "Series 610" / "Liebert"
		item.setDataCenterLocation( loc );
		item.setRatingV(480);
		item.setRatingKva(20);
		item.setRatingKW(20);
		item.setTransformer(false);
		item.setEvenOddLayout(false);
		item.setPsredundancy("N");
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE));
		itemHome.saveItem( item );
		session.flush();
		
		upsBankCreateBreakerPortActionHandler.process(item.getItemId(), null, null, null, true, false);
		
		addCircuitItem(item);
		
		return item;
	}
	
	protected final MeItem createFloorPDUWithPanelsAndBranchCircuitBreakers(String itemName, MeItem upsBank) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setuPosition(-9);
		item.setFacingLookup( SystemLookup.getLksData(session, SystemLookup.FrontFaces.NORTH ) );
		item.setNumPorts(3);
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setFreePowerPortCount(1);
		
		item.setRatingKva(200);
		item.setTransformer(false);
		item.setEvenOddLayout(false);
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE));
		// item.setRatingV(120);
		item.setLineVolts(480);
		item.setRatingAmps(22);
		item.setDataCenterLocation(loc);
		// MeItem upsBank = createUPSBank("CIRCUIT_TEST_UPS_BANK");
		item.setUpsBankItem(upsBank);
		
		itemHome.saveItem( item );
		session.flush();

		floorPduCreateBreakerPortActionHandler.process(item, null, true);
		floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(item, upsBank, null, true, true);
		
		Item panel1 = createCircuitPowerPanel(item, "CIRCUIT_TEST_PS1", 12L, SystemLookup.SubClass.LOCAL, upsBank);
		// Item panel2 =  createCircuitPowerPanel(item, "PS2", 8L, SystemLookup.SubClass.LOCAL, upsBank);
		item.addChildItem( panel1 );
		// item.addChildItem( panel2 );
		
		// make a connection between fpdu and panel
		/*powerPanelBreakerPortActionHandler.process(panel1, null, true);
		powerPanelBreakerPortActionHandler.process(panel2, null, true);*/
		
		addCircuitItem(item);
		
		return item;
	}

	protected MeItem createCircuitPowerPanel(Item parentItem, String panelName, Long numBranchCircuitBreakers, Long subClassLkpValueCode, MeItem upsBankItem) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName( panelName );
		item.setuPosition(-9);
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setSubclassLookup( SystemLookup.getLksData(session, subClassLkpValueCode.longValue()) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation(loc);
		item.setParentItem(parentItem);
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setFreePowerPortCount(13);
		
		item.setTransformer(false);
		item.setPolesQty(12);
		item.setEvenOddLayout(false);
		item.setLineVolts(208);
		item.setPhaseVolts(120);
		item.setPhaseAColorLookup(SystemLookup.getLksData(session, SystemLookup.PhaseColor.Black));
		item.setPhaseBColorLookup(SystemLookup.getLksData(session, SystemLookup.PhaseColor.Red));
		item.setPhaseCColorLookup(SystemLookup.getLksData(session, SystemLookup.PhaseColor.Blue));
		item.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE ));
		item.setUpsBankItem(upsBankItem);
		item.setRatingAmps(100);
		
		for (Long i = 0L; i < numBranchCircuitBreakers; i++) {
			item.addPowerPort(createBranchCircuitBreakerPanelPort(item, i.toString(), i.intValue()));
		}
		itemHome.saveItem( item );
		session.flush();
		
		powerPanelBreakerPortActionHandler.process(item, null, true, true);
		
		addTestItem( item );
		return item;
	}

	class PortInfo {
		Long portNumber;
		Long portPhase;
		Long ampsNameplate;
		
	};

	protected final void createBranchCircuitBreakerPanelPort(MeItem item, List<PortInfo> portsInfo, Long numOfColumns, Long numOfWire) throws Throwable {
		List<Long> createdPorts = new ArrayList<Long>();

		// Create the poles first
		for (int i = 0; i < item.getNumPorts(); i++) {
			// Port Label as item name
			// phase as item phase
			// phase volt as item phase volt and item line volt 
			// amps as passed value - long
			// num of poles as item's getNumPort
			// pole layout as passed parameter - single col / 2 column - enum
			// pole numbering as passed parameter - seq, odd-even, (A,B,C) etc - enum
			// start pole number as passed parameter - enum
			// Wire colors as passed parameter - enum
			createPole(Long.valueOf(i), item, numOfColumns, numOfWire, createdPorts);
		}

		// Create the breaker ports using the port Name. Map this port Name to the sort order number and then process the creation of breaker ports
		for (PortInfo portInfo: portsInfo) {
			
			createBranchCircuitPort(portInfo.portNumber, item, portInfo.portPhase, portInfo.ampsNameplate, numOfColumns, numOfWire, createdPorts);
		}
		
		
	}
	
	private PowerPort createPole(Long portNumber, MeItem item, Long numOfColumns, Long numOfWire, List<Long> createdPorts) {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		if (createdPorts.contains(portNumber)) {
			return null;
		}
		
		PowerPort pole = new PowerPort();
		pole.setItem(item);
		pole.setPortName(portNumber.toString());
		pole.setUsed(false);
		pole.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
		pole.setSortOrder(portNumber.intValue());
		pole.setPlacementX(0);
		pole.setPlacementY(0);
		if (2 == numOfColumns) {
			boolean evenPort = (portNumber % 2) == 0;
			if (evenPort) {
				pole.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.RIGHT_REAR));
			}
			else {
				pole.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR));
			}
		}
		else if (1 == numOfColumns) {
			pole.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR));
		}
		pole.setIsRedundant(false);
		pole.setPowerFactor(1.0);
		pole.setCreationDate(creationDate);
		Long itemPhase = (item.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.SINGLE_2WIRE || item.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.SINGLE_3WIRE ) ? 1L : 3L;
		Integer pNameMod = (portNumber.intValue() % ((itemPhase == 3) ? 6 : 4));
		Map<Long, String> portNamePoleMap = portNamePoleMapFactory.get(itemPhase.toString() + ((null != numOfWire) ? (":" + numOfWire.toString()) : "") + (":" + numOfColumns.toString()));
		pole.setPolePhase(portNamePoleMap.get(pNameMod));
		item.addPowerPort(pole);
		createdPorts.add(portNumber);
		
		return null;
	}
	
	private String generateBranchCircuitBreakerName(Long portNumber, Long phase, Long numOfColumns) { 
		StringBuffer strBuf = new StringBuffer();
		
		for (Long i = portNumber; i < portNumber + (phase * numOfColumns); i += numOfColumns) {
			
			strBuf.append(i.toString());
			
			if (i < (portNumber + (phase * numOfColumns)) - numOfColumns) {
				strBuf.append(",");
			}
			
		}
		
		return strBuf.toString();
		
	}
	
	private PowerPort createBranchCircuitPort(Long portNumber, MeItem item, Long portPhase /*1, 2, or 3*/, Long ampsNameplate, Long numOfColumns, Long numOfWire, List<Long> createdPorts) {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		PowerPort breakerPort = new PowerPort();
		String portName = generateBranchCircuitBreakerName(portNumber, portPhase, numOfColumns); 
		breakerPort.setPortName(portName); // update the port name using existing name, port phase (must be less than or equal to item phase) and name against the sort order. 
		breakerPort.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)); // not required, updated when pole is created
		breakerPort.setSortOrder(portNumber.intValue()); // not required, updated when pole is created
		breakerPort.setUsed(false); // not required, updated when pole is created
		
		// Integer pNameMod = (portNumber.intValue() % ((phase == 3) ? 6 : 4));
		// Map<Long, String> portNamePoleMap = portNamePoleMapFactory.get(phase.toString() + ((null != numOfWire) ? (":" + numOfWire.toString()) : "") + (":" + numOfColumns.toString()));
		// Map<Long, String> portNamePoleMap = portNamePoleMapFactory.get(phase.toString() + ((null != numOfWire) ? (":" + numOfWire.toString()) : "") + (":" + numOfColumns.toString()));
		Long itemPhase = (item.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.SINGLE_2WIRE || item.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.SINGLE_3WIRE ) ? 1L : 3L;
		Integer pNameMod = (portNumber.intValue() % ((itemPhase == 3) ? 6 : 4)); // comment
		Map<Long, String> portNamePoleMap = portNamePoleMapFactory.get(itemPhase.toString() + ((null != numOfWire) ? (":" + numOfWire.toString()) : "") + (":" + numOfColumns.toString())); // comment
		breakerPort.setPhaseLegsLookup(SystemLookup.getLksData(session, portPhasePoleWithPhaseLegsMap.get(portPhase.toString() + ((null != numOfWire) ? (":" + numOfWire.toString()) : "") + (":" + portNamePoleMap.get(pNameMod))))); // replace portNamePoleMap.get(pNameMod) with the port.getPolePhase() 
		if (portPhase == 3) {
			breakerPort.setConnectorLookup(new ConnectorLkuData(79));
			breakerPort.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE));
			List<LksData> voltsLksList =  systemLookupDAO.findByLkpValueAndType(Long.toString((new Double(item.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
			if (null != voltsLksList && voltsLksList.size() == 1) {
				breakerPort.setVoltsLookup(voltsLksList.get(0));
			}
			// port.setVoltsLookup(/*item.getPhaseVolts()*/SystemLookup.getLksData(session, SystemLookup.VoltClass.V_208, SystemLookup.LkpType.VOLTS));
			
		}
		else if (numOfWire == 3) {
			breakerPort.setConnectorLookup(new ConnectorLkuData(78));
			breakerPort.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE));
			List<LksData> voltsLksList =  systemLookupDAO.findByLkpValueAndType(Long.toString((new Double(item.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
			if (null != voltsLksList && voltsLksList.size() == 1) {
				breakerPort.setVoltsLookup(voltsLksList.get(0));
			}
			// port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_208, SystemLookup.LkpType.VOLTS));
			
		}
		else if (numOfWire == 2) {
			breakerPort.setConnectorLookup(new ConnectorLkuData(77));
			breakerPort.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_2WIRE));
			List<LksData> voltsLksList =  systemLookupDAO.findByLkpValueAndType(Long.toString((new Double(item.getPhaseVolts())).intValue()), SystemLookup.LkpType.VOLTS);
			if (null != voltsLksList && voltsLksList.size() == 1) {
				breakerPort.setVoltsLookup(voltsLksList.get(0));
			}
			// port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_120, SystemLookup.LkpType.VOLTS));
			
		}
		breakerPort.setPolePhase(portNamePoleMap.get(pNameMod)); // not required, updated when pole is created
		breakerPort.setIsRedundant(false); // not required, updated when pole is created
		breakerPort.setPowerFactor(1); // not required, updated when pole is created
		breakerPort.setPlacementX(0); // not required, updated when pole is created
		breakerPort.setPlacementY(0); // not required, updated when pole is created
		if (2 == numOfColumns) {
			boolean evenPort = (portNumber % 2) == 0;
			if (evenPort) {
				breakerPort.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.RIGHT_REAR)); // not required, updated when pole is created
			}
			else {
				breakerPort.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR)); // not required, updated when pole is created
			}
		}
		else if (1 == numOfColumns) {
			breakerPort.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR)); // not required, updated when pole is created
		}
		breakerPort.setCreationDate(creationDate); // not required, updated when pole is created
		breakerPort.setAmpsNameplate(ampsNameplate);
		breakerPort.setItem(item); // not required, updated when pole is created
		item.addPowerPort(breakerPort); // not required, updated when pole is created 
		createdPorts.add(Long.valueOf(breakerPort.getPortName())); 
		
		// Create the remaining ports
		String portNames[] = portName.split(",");
		for (Long i = 0L; i < portNames.length; i++) {
			PowerPort portNonBrkr = new PowerPort();
			String pName = portNames[i.intValue()]; 
			Long pNumber = Long.valueOf(pName);
			portNonBrkr.setPortName(pName); // not required, updated when pole is created
			portNonBrkr.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)); // not required, updated when pole is created
			portNonBrkr.setSortOrder(pNumber.intValue()); // not required, updated when pole is created
			portNonBrkr.setUsed(false); // not required, updated when pole is created
			pNameMod = (pNumber.intValue() % ((portPhase == 3) ? 6 : 4)); // not required, updated when pole is created
			portNonBrkr.setPolePhase(portNamePoleMap.get(pNameMod)); // not required, updated when pole is created 
			portNonBrkr.setIsRedundant(false); // not required, updated when pole is created
			portNonBrkr.setPowerFactor(1); // not required, updated when pole is created
			portNonBrkr.setPlacementX(0); // not required, updated when pole is created
			portNonBrkr.setPlacementY(0); // not required, updated when pole is created
			if (2 == numOfColumns) {
				boolean evenPort = (pNumber % 2) == 0;
				if (evenPort) {
					portNonBrkr.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.RIGHT_REAR)); // not required, updated when pole is created
				}
				else {
					portNonBrkr.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR)); // not required, updated when pole is created
				}
			}
			else if (1 == numOfColumns) {
				portNonBrkr.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR)); // not required, updated when pole is created
			}
			portNonBrkr.setCreationDate(creationDate); // not required, updated when pole is created
			portNonBrkr.setItem(item); // not required, updated when pole is created
			portNonBrkr.setBreakerPort(breakerPort); 
			item.addPowerPort(portNonBrkr); // not required, updated when pole is created
			createdPorts.add(Long.valueOf(portNonBrkr.getPortName())); 
		}
		
		
		return null;
		
	}
	
	// User should just provide the portname, phase and amps nameplate
	private PowerPort createBranchCircuitPort(String portName, MeItem item, List<String> createdPorts) {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		String portNames[] = portName.split(",");
		Integer portNameIdx = portNames.length;
		for (Integer i = portNameIdx; i > 0; i--) {
			String pName = portNames[i - 1];
			if (createdPorts.contains(pName)) {
				continue;
			}
			boolean evenPort = ((Integer.valueOf(pName) % 2) == 0);
			Integer pNameMod = (Integer.valueOf(pName) % 6);
			PowerPort port = new PowerPort();	
			port.setPortName(pName);
			port.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
			port.setSortOrder(Integer.valueOf(pName));
			port.setUsed(false);
			if (i == 1) {
				port.setPhaseLegsLookup(SystemLookup.getLksData(session, portPhasePoleWithPhaseLegsMap.get(String.valueOf(portNames.length) + ":" + portNamePoleMap_3Phase_2Column_OddEven.get(pNameMod))));
				port.setPortName(portName);
				if (portNames.length == 3) {
					port.setConnectorLookup(new ConnectorLkuData(79));
					port.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE));
					List<LksData> voltsLksList =  systemLookupDAO.findByLkpValueAndType(Long.toString((new Double(item.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
					if (null != voltsLksList && voltsLksList.size() == 1) {
						port.setVoltsLookup(voltsLksList.get(0));
					}
					port.setPhaseLegsLookup(SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
					
				}
				else if (portNames.length == 2) {
					port.setConnectorLookup(new ConnectorLkuData(78));
					port.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE));
					port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_208, SystemLookup.LkpType.VOLTS));
					
				}
				else if (portNames.length == 1) {
					port.setConnectorLookup(new ConnectorLkuData(77));
					port.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_2WIRE));
					port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_120, SystemLookup.LkpType.VOLTS));
					
				}
			}
			port.setPolePhase(portNamePoleMap_3Phase_2Column_OddEven.get(pNameMod));
			port.setIsRedundant(false);
			port.setPowerFactor(1);
			port.setPlacementX(0);
			port.setPlacementY(0);
			if (evenPort) {
				port.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.RIGHT_REAR));
			}
			else {
				port.setFaceLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR));
			}
			port.setCreationDate(creationDate);
			// TODO: set the amps nameplate
			createdPorts.add(pName);
		}
		
		PowerPort port = new PowerPort();	
		
		
		
		// port.setPortStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		// port.setFaceLookup(new LksData(261L));
		port.setAmpsNameplate(90);
		port.setItem(item);
		
		return port;		
		
	}

}
