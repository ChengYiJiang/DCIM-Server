package com.raritan.tdz.powerchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainHome;
import com.raritan.tdz.powerchain.home.PowerChainLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * Power Chain test base
 * @author bunty
 *
 */
public class PowerChainTestBase extends TestBase {
	@Autowired(required=true)
	protected PowerChainHome powerChainHome;
	
	@Autowired(required=true)
	private UnitTestItemDAO unitTestItemDAO;
	
	/*@Autowired(required=true)
	protected SystemLookupFinderDAO systemLookupFinderDAO;*/
	
	protected LksData insertEvt;
	protected LksData updateEvt;
	protected LksData deleteEvt;
	
	@AfterClass
	public void classTearDown() throws Throwable {
		
		Query deleteQuery = session.createSQLQuery(
			    "delete from dct_items_me where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		@SuppressWarnings("unused")
		int updated = deleteQuery.executeUpdate();
		session.flush();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_connections_power where source_port_id in (select port_power_id from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK'))");
		updated = deleteQuery.executeUpdate();
		session.flush();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK'))");
		updated = deleteQuery.executeUpdate();
		session.flush();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();
		session.flush();
		
		deleteQuery = session.createSQLQuery(
			    "update dct_items_me set ups_bank_item_id = null where ups_bank_item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();

		deleteQuery = session.createSQLQuery(
			    "delete from dct_items where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();
		
		session.flush();
		
	}
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		powerChainHome = (PowerChainHome)  ctx.getBean("powerChainHome");
		unitTestItemDAO = (UnitTestItemDAO) ctx.getBean("unitTestItemDAO");
		
		/*List<LksData> lksList =  null;
		lksList = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.VBJavaBridgeOperations.INSERT);
		insertEvt = lksList.get(0);
		
		lksList =  systemLookupFinderDAO.findByLkpValueCode(SystemLookup.VBJavaBridgeOperations.UPDATE);
		updateEvt = lksList.get(0);
		
		lksList =  systemLookupFinderDAO.findByLkpValueCode(SystemLookup.VBJavaBridgeOperations.DELETE);
		deleteEvt = lksList.get(0);*/
		
		
		insertEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT);
		updateEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE);
		deleteEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE);
		
		System.out.println("power chain setup complete");
	}

	protected void processEventWithExpectedErrorCode(LNEvent event, List<String> errorCodes) throws BusinessValidationException {
		// save the item
		try {
			powerChainHome.processLNEvent(event);		
		} catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwbe = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet()) {
				if (errorCodes.contains(entry.getKey())) { 
					throwbe = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet()) {
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			if (throwbe) {
				throw be;
			}
		}
	}
	

	protected void validatePanelBreakerCount(Item panel) {
		Set<PowerPort> pps = panel.getPowerPorts();
		int panelBreakerCount = 0;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PANEL_BREAKER)) {
				panelBreakerCount++;
			}
		}
		Assert.assertEquals(panelBreakerCount, 1);
	}

	protected void validateBranchCircuitBreakerCount(Item panel, int expectedCount) {
		Set<PowerPort> pps = panel.getPowerPorts();
		int branchCircuitBreakerCount = 0;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) {
				branchCircuitBreakerCount++;
			}
		}
		Assert.assertEquals(expectedCount, branchCircuitBreakerCount);
	}

	protected void validateBranchCircuitBreakerValue(Item panel) {
	Set<PowerPort> pps = panel.getPowerPorts();
		PowerPort branchCircuitBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) {
				branchCircuitBreaker = pp;
				Assert.assertEquals(SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER), branchCircuitBreaker.getPortSubClassLookup());
				Assert.assertEquals(PowerChainLookup.ConnectorLookup.ONE_POLE_BREAKER, branchCircuitBreaker.getConnectorLookup().getConnectorId());
			}
		}

	}
	
	protected void validatePanelBreakerValue(Item panel) {
		Set<PowerPort> pps = panel.getPowerPorts();
		PowerPort panelBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PANEL_BREAKER)) {
				panelBreaker = pp;
				break;
			}
		}
		
		String portName = "Panel Breaker";
		if (null != panel.getSubclassLookup() && null != panel.getSubclassLookup().getLkpValueCode() &&
				panel.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
			portName = "Busway Breaker";
		}
		Assert.assertArrayEquals("Port Name incorrect", panelBreaker.getPortName().toCharArray(), (new String(portName)).toCharArray());
		Assert.assertEquals(SystemLookup.getLksData(session, SystemLookup.PortSubClass.PANEL_BREAKER), panelBreaker.getPortSubClassLookup());
		Assert.assertEquals(PowerChainLookup.ConnectorLookup.THREE_PHASE_POLE_BREAKER, panelBreaker.getConnectorLookup().getConnectorId());
		Assert.assertEquals(panelBreaker.getPhaseLookup(), ((MeItem)panel).getPhaseLookup());
		// FIXME:: commented for now because existing database have invalid voltage values
		java.lang.Integer lineVolt = new java.lang.Integer((int)((MeItem)panel).getLineVolts());
		Assert.assertEquals(panelBreaker.getVoltsLookup().getLkpValue(), lineVolt.toString());
		Assert.assertEquals(new Double(panelBreaker.getAmpsNameplate()), new Double(((MeItem)panel).getRatingAmps()));
		Assert.assertEquals(panelBreaker.getPhaseLegsLookup(), SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
	}

	protected void validateUpsBankUsedValue(Item upsBank, boolean expected) {
		Set<PowerPort> pps = upsBank.getPowerPorts();
		PowerPort upsBankBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER)) {
				upsBankBreaker = pp;
				break;
			}
		}
		Boolean usedFlag = getPortUsedFlag(upsBankBreaker.getPortId());
		System.out.println("Used val = " + usedFlag.toString());
		Assert.assertEquals(expected, usedFlag.booleanValue());
		
	}
	

	
	protected void validateupsBankBreakerValue(Item upsBank) {
		Set<PowerPort> pps = upsBank.getPowerPorts();
		PowerPort upsBankBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER)) {
				upsBankBreaker = pp;
				break;
			}
		}
		
		Assert.assertArrayEquals("Port Name incorrect", upsBankBreaker.getPortName().toCharArray(), (new String("Bank Breaker")).toCharArray() );
		Assert.assertEquals(SystemLookup.getLksData(session, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER), upsBankBreaker.getPortSubClassLookup());
		Assert.assertEquals(PowerChainLookup.ConnectorLookup.THREE_PHASE_POLE_BREAKER, upsBankBreaker.getConnectorLookup().getConnectorId());
		Assert.assertEquals(upsBankBreaker.getPhaseLookup(), ((MeItem)upsBank).getPhaseLookup());
		// FIXME:: commented for now because existing database have invalid voltage values
		java.lang.Integer ratingVolt = new java.lang.Integer((int)((MeItem)upsBank).getRatingV());
		Assert.assertEquals(upsBankBreaker.getVoltsLookup().getLkpValue(), ratingVolt.toString());
		
		MeItem meItem = (MeItem) upsBank;
		Long phaseLkpValueCode = (null != meItem.getPhaseLookup() && null != meItem.getPhaseLookup().getLkpValueCode()) ? meItem.getPhaseLookup().getLkpValueCode() : -1;
		
		double normalizeFactor = (phaseLkpValueCode.longValue() == SystemLookup.PhaseIdClass.THREE_DELTA || phaseLkpValueCode.longValue() == SystemLookup.PhaseIdClass.THREE_WYE) ? Math.sqrt(3) : 1;
		double deRatedFactor = 0.8;
		
		double ampsRated = 0;
		if (0 != meItem.getRatingV()) {
			ampsRated = meItem.getRatingKva() * 1000 / (meItem.getRatingV() * normalizeFactor * deRatedFactor);
		}
		Assert.assertEquals(new Double(upsBankBreaker.getAmpsNameplate()), new Double(ampsRated));
		Assert.assertEquals(upsBankBreaker.getPhaseLegsLookup(), SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
	}


	protected void validateFPDUUsedValue(Item fpdu, boolean expected) {
		Set<PowerPort> pps = fpdu.getPowerPorts();
		PowerPort fpduBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PDU_INPUT_BREAKER)) {
				fpduBreaker = pp;
				break;
			}
		}

		Assert.assertEquals(fpduBreaker.getUsed(), expected);
	}
	
	protected void validateFPDUBreakerValue(Item fpdu) {
		Set<PowerPort> pps = fpdu.getPowerPorts();
		PowerPort fpduBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PDU_INPUT_BREAKER)) {
				fpduBreaker = pp;
				break;
			}
		}
		
		Assert.assertArrayEquals("Port Name incorrect", fpduBreaker.getPortName().toCharArray(), (new String("PDU Breaker")).toCharArray());
		Assert.assertEquals(SystemLookup.getLksData(session, SystemLookup.PortSubClass.PDU_INPUT_BREAKER), fpduBreaker.getPortSubClassLookup());
		Assert.assertEquals(PowerChainLookup.ConnectorLookup.THREE_PHASE_POLE_BREAKER, fpduBreaker.getConnectorLookup().getConnectorId());
		Assert.assertEquals(fpduBreaker.getPhaseLookup(), ((MeItem)fpdu).getPhaseLookup());
		java.lang.Integer lineVolt = null;
		Set<PowerConnection> pcs = fpduBreaker.getSourcePowerConnections();
		if ((null != pcs && pcs.size() > 0)) {
			for (PowerConnection conn: pcs) {
				PowerPort destPort = (PowerPort) conn.getDestPort();
				if (null == destPort) break;
				lineVolt = new java.lang.Integer(destPort.getVoltsLookup().getLkpValue());
				break;
			}
		}
		else {
			lineVolt = new java.lang.Integer((int)((MeItem)fpdu).getLineVolts());
		}
		
		Assert.assertEquals(fpduBreaker.getVoltsLookup().getLkpValue(), lineVolt.toString());
		Assert.assertEquals(new Double(fpduBreaker.getAmpsNameplate()), new Double(((MeItem)fpdu).getRatingAmps()));
		Assert.assertEquals(fpduBreaker.getPhaseLegsLookup(), SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
	}


	protected void validateBranchCircuitBreakerConnectionToPanelBreaker(Item panel) {
		Set<PowerPort> pps = panel.getPowerPorts();
		PowerPort panelBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PANEL_BREAKER)) {
				panelBreaker = pp;
				break;
			}
		}
		for (PowerPort pp: pps) {
			PowerPort branchCircuitBreaker = null;
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) {
				branchCircuitBreaker = pp;
				Set<PowerConnection> pcs = branchCircuitBreaker.getSourcePowerConnections();
				if (null == pcs || pcs.size() <= 0) {
					Assert.fail("No connection from branch circuit breaker");
				}
				if (pcs.size() > 1) {
					Assert.fail("More than one connections from branch circuit breaker");
				}
				for (PowerConnection pc: pcs) {
					if (pc.getDestPort().getPortId().longValue() != panelBreaker.getPortId().longValue()) {
						Assert.fail("not a correct connection established between branch circuit breaker and panel breaker");
					}
				}
			}
		}
	}

	class ConnectionDetails {
		long sourcePortSubClassCount;
		long connectionCount;
		
		public ConnectionDetails(long sourcePortSubClassCount, long connectionCount) {
			this.sourcePortSubClassCount = sourcePortSubClassCount;
			this.connectionCount = connectionCount;
		}
		
		
	}
	
	private ConnectionDetails getConnectionDetail(Item srcItem, Long srcPortSubClass, Item destItem, Long destPortSubClass, Long numOfConnections) {
		Set<PowerPort> srcPPs = srcItem.getPowerPorts();
		ConnectionDetails connDetails = new ConnectionDetails(0, 0);
		
		for (PowerPort srcPP: srcPPs) {
			if (srcPP.getPortSubClassLookup().getLkpValueCode().equals(srcPortSubClass)) {
				connDetails.sourcePortSubClassCount++;
				Set<PowerConnection> pcs = srcPP.getSourcePowerConnections();
				for (PowerConnection pc: pcs) {
					if (pc.getDestPort().getItem().getItemId() == destItem.getItemId() && 
							pc.getDestPort().getPortSubClassLookup().getLkpValueCode().equals(destPortSubClass)) {
						connDetails.connectionCount++;
					}
				}
			}
		}
		
		return connDetails;
	} 
	
	protected void validateConnectionCount(Item srcItem, Long srcPortSubClass, Item destItem, Long destPortSubClass, Long numOfConnections) {
		
		ConnectionDetails connDetails = getConnectionDetail(srcItem, srcPortSubClass, destItem, destPortSubClass, numOfConnections);
		
		Assert.assertEquals(numOfConnections.longValue(), connDetails.connectionCount);
		
	}
	


	protected void validatePanelBreakerConnectionToFPDUInputBreaker(Item panel) {
		Set<PowerPort> pps = panel.getPowerPorts();
		PowerPort panelBreaker = null;
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PANEL_BREAKER)) {
				panelBreaker = pp;
				break;
			}
		}
		
		PowerPort fpduInputBreaker = null;
		Item fpduItem = panel.getParentItem();
		pps = fpduItem.getPowerPorts();
		if (null == pps) {
			// no fpdu input breaker port to validate against
			return;
		}
		for (PowerPort pp: pps) {
			if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PDU_INPUT_BREAKER)) {
				fpduInputBreaker = pp;
				break;
			}
		}
		
		Set<PowerConnection> pcs = panelBreaker.getSourcePowerConnections();
		if (null == pcs || pcs.size() <= 0) {
			Assert.fail("No connection from panel breaker");
		}
		if (pcs.size() > 1) {
			Assert.fail("More than one connections from panel breaker");
		}
		for (PowerConnection pc: pcs) {
			if (pc.getDestPort().getPortId().longValue() != fpduInputBreaker.getPortId().longValue()) {
				Assert.fail("not a correct connection established between panel breaker and fpdu input breaker");
			}
		}
	}

	protected void validateUpsBankOutputBreakerCount(Item upsBank, int expectedCount) {
		int upsBankOutputBreakerCount = 0;
		Set<PowerPort> pps = null;
		if (null != upsBank) {
			pps = upsBank.getPowerPorts();
		}
		if (null != pps) {
			for (PowerPort pp: pps) {
				if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER)) {
					upsBankOutputBreakerCount++;
				}
			}
		}
		Assert.assertEquals(upsBankOutputBreakerCount, expectedCount);
	}
	

	
	protected void validateFPDUInputBreakerCount(Item fpdu, int expectedCount) {
		int pduInputBreakerCount = 0;
		Set<PowerPort> pps = null;
		if (null != fpdu) {
			pps = fpdu.getPowerPorts();
		}
		if (null != pps) {
			for (PowerPort pp: pps) {
				if (pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.PDU_INPUT_BREAKER)) {
					pduInputBreakerCount++;
				}
			}
		}
		Assert.assertEquals(pduInputBreakerCount, expectedCount);
	}
	
	protected void powerPanelAddBranchCircuit(Item panelItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
					panelItem.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_ADD_BRANCH_CIRCUIT_BREAKER);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}


	protected final void powerPanelUpdateBranchCircuitValueTest(Item panel) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
					panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_UPDATE_BREAKER_VALUES);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}
	
	protected final void createfloorPDUInputBreaker(Item fpduItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
				fpduItem.getItemId(), null, null, PowerChainLookup.Action.FLOOR_PDU_CREATE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}

	protected final void createUpsBankBreaker(Item upsBankItem) throws Throwable {
		
		createUpsBankBreaker(upsBankItem.getItemId());
		
	}
	
	protected final void createUpsBankBreaker(Long upsBankItemId) throws Throwable {
		
		LNEvent upsBankCreate = new LNEvent(100, insertEvt, "dct_items", 
				upsBankItemId, null, null, PowerChainLookup.Action.UPS_BANK_CREATE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(upsBankCreate, expectedErrorCodes);
		
	}


	protected final void createBreakerPortAndConnections(Item fpduItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
				fpduItem.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}

	protected final void updateFloorPDUInputBreakerValue(Item fpduItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
				fpduItem.getItemId(), null, null, PowerChainLookup.Action.FLOOR_PDU_UPDATE_BREAKER_VALUES);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
	}


	protected final void createFloorPDUToUPSBankConnection(Item fpduItem, Item upsBank) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
				fpduItem.getItemId(), (new Long(upsBank.getItemId())).toString(), null, PowerChainLookup.Action.FLOOR_PDU_BREAKER_PORT_CONNECTION);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
	}
	
	protected final void updateUPSBankValue(Item upsBankItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
				upsBankItem.getItemId(), null, null, PowerChainLookup.Action.UPS_BANK_UPDATE_BREAKER_PORT_VALUES);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
	}
	
	protected final void deleteFPDUtoUPSBankConnection(Item fpduItem) throws Throwable {
		
		LNEvent powerPanelCreate = new LNEvent(100, deleteEvt, "dct_items", 
				fpduItem.getItemId(), null, null, PowerChainLookup.Action.FLOOR_PDU_DELETE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}

	private Boolean getPortUsedFlag(Long portId) {
		String sql = "SELECT is_used FROM dct_ports_power WHERE port_power_id = :port_id";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("port_id", portId);
		Boolean result = (Boolean) query.uniqueResult();
		return result;
	}

	protected List<MeItem> createPanels(MeItem fpdu, Long subClassLkpValueCode, boolean withOutlets, boolean withOutletsConnected) throws Throwable {
		
		List<MeItem> powerOutlets = new ArrayList<MeItem>();
		
		// create 2 panels and set the parent item as the fpdu
		MeItem panel1 = createPowerPanel(fpdu, "INT_TEST_PS1", 4L, subClassLkpValueCode, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel1 );
		session.flush();*/
		fpdu.addChildItem(panel1);
		
		MeItem panel2 = createPowerPanel(fpdu, "INT_TEST_PS2", 4L, subClassLkpValueCode, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel2 );
		session.flush();*/
		fpdu.addChildItem(panel2);

		List<Item> panels = new ArrayList<Item>(); 
		panels.add(panel1);
		panels.add(panel2);
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		Long count = 1L;
		for (Item panel: panels) {
			
			powerPanelBreakerPortActionHandler.process(panel, null, true, true);
			session.update(panel);
			session.flush();
			if (withOutlets) {
				if (withOutletsConnected) {
					MeItem po = createConnectedPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, fpdu.getUpsBankItem());
					powerOutlets.add(po);
				}
				else {
					MeItem po = createPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, fpdu.getUpsBankItem());
					powerOutlets.add(po);
				}
				count++;
			}
			
		}
		
		return powerOutlets;
	}
	
	public MeItem createFloorPDU(boolean withUpsBank, boolean withPanels, boolean withOutlets, boolean withOutletsConnected) throws Throwable {
		
		MeItem upsBank = null;
		if (withUpsBank) {
			upsBank = createNewTestUPSBank("INT_TEST_UPSBANK");
			MeItem ups = createNewTestUPS("INT_TEST_BANK");
			ups.setUpsBankItem(upsBank);
			upsBank.setPsredundancy("N");
		}
		// create FPDU in planned state, no panels
		// MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		MeItem fpdu = createNewTestFloorPDU("INT_TEST_FPDU"); // createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		if (withUpsBank) {
			fpdu.setUpsBankItem(upsBank);
		}
		floorPduCreateBreakerPortActionHandler.process(fpdu, null, true);
		if (withUpsBank) {
			floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, null, true, true);
		}
		
		if (withPanels) {
			// Create Local Panels and outlets and used flag updated
			createPanels(fpdu, SystemLookup.SubClass.LOCAL, withOutlets, withOutletsConnected);
		}
		session.update(fpdu);
		
		session.flush();
		
		session.refresh(fpdu);
		
		return fpdu;
		
	}

	private MeItem createConnectedPowerOutlet(String outletName, Item cabinet, Item panel, Item upsBank) throws Throwable {
		Set<PowerPort> panelPorts = panel.getPowerPorts();
		PowerPort bcBreakerPort = null;
		for (PowerPort port: panelPorts) {
			if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
				bcBreakerPort = port;
				break;
			}
		}
		// List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
		
		MeItem outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, bcBreakerPort, 2L);
		if (panel.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
			bcBreakerPort.setBuswayItem(outlet);
		}
		
		Set<PowerPort> ports = outlet.getPowerPorts();
		for (PowerPort port: ports) {
			port.setUsed(true);
			// break;
		}
		
		session.update(outlet);
		session.update(panel);
		// powerOutlet.add(outlet.getItemId());
		
		return outlet;
	}

	protected MeItem createPowerOutlet(String outletName, Item cabinet, Item panel, Item upsBank) throws Throwable {
/*		MeItem outlet = null;
		if (null != panel) {
			Set<PowerPort> panelPorts = panel.getPowerPorts();
			List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
		
			outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, panelPortList.get(0), 2L);
		}
		else {
			outlet = createPowerOutlet(outletName, upsBank, null, cabinet, null, 2L);
		}
		powerOutlet.add(outlet.getItemId());
		
		return outlet;*/
		
		MeItem outlet = null;
		PowerPort bcBreakerPort = null;
		
		if (null != panel) {
			Set<PowerPort> panelPorts = panel.getPowerPorts();
			bcBreakerPort = null;
			for (PowerPort port: panelPorts) {
				if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
					bcBreakerPort = port;
					break;
				}
			}
		}
			// List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
			
		outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, bcBreakerPort, 2L);
		if (null != panel) {
			if (panel.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
				bcBreakerPort.setBuswayItem(outlet);
			}
			session.update(outlet);
			session.update(panel);
		}
		// powerOutlet.add(outlet.getItemId());
		
		/*Set<PowerPort> ports = outlet.getPowerPorts();
		for (PowerPort port: ports) {
			port.setUsed(true);
			// break;
		}*/
		
		return outlet;

	}


}
 