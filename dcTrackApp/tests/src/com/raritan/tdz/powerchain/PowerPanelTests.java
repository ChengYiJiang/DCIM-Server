package com.raritan.tdz.powerchain;

import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * test the power panel portion of power chain
 * @author bunty
 *
 */
public class PowerPanelTests extends PowerChainTestBase {

	// the item do not exist, server should not throw system exception
	@Test
	public final void invalidItemCreateTest() throws Throwable {
		LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
					-100L, null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PowerChain.invalidItemId");
		
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}
	
	// test if the floor pdu breaker is not created for action : POWER_PANEL_CREATE_BREAKER_PORT
	@Test
	public final void floorPduCreateTest() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
					fpduItem.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PowerChain.invalidItemId");
		
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
		validateFPDUInputBreakerCount(fpduItem, 0);
		
	}
	
	protected void powerPanelAddBranchCircuit(Item panelItem) throws Throwable {
		LNEvent powerPanelCreate = new LNEvent(100, updateEvt, "dct_items", 
					panelItem.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_ADD_BRANCH_CIRCUIT_BREAKER);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}


	@Test
	public final void testCreatePanelBreakersWithNoFPDUBreakerAndNoPanel() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);
				
			}
			
		}
		
	}
	

	
	@Test
	public final void testCreatePanelBreakersWithNoFPDUBreaker() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);
				
			}
			
		}
		
	}
	
	
	@Test
	public final void testCreatePanelBreakersWithFPDUBreaker() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker count
		validateFPDUInputBreakerCount(fpduItem, 1);
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);
				
			}
			
		}
		
	}
	

	@Test
	public final void testCreatePanelBreakersAfterNoFPDUBreakerAndNoPanel() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);
				
				int branchCircuitBreakerCount = 0;
				if (null != panel.getPowerPorts()) {
					for (PowerPort pp: panel.getPowerPorts()) {
						if (pp.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) 
							branchCircuitBreakerCount++;
					}
				}
				
				// create more branch circuit breakers
				createBranchCircuitBreakerPanelPort(panel, 4L);

				// update the connections to the panel breaker
				powerPanelAddBranchCircuit(panel);

				// Validate the count of branch circuit breakers
				validateBranchCircuitBreakerCount(panel, branchCircuitBreakerCount + 4);
				
				// Validate branch circuit breaker values
				validateBranchCircuitBreakerValue(panel);

				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);

			}
			
		}
		
	}
	
	
	@Test
	public final void testCreatePanelBreakersAfterNoFPDUBreaker() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);

				int branchCircuitBreakerCount = 0;
				if (null != panel.getPowerPorts()) {
					for (PowerPort pp: panel.getPowerPorts()) {
						if (pp.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) 
							branchCircuitBreakerCount++;
					}
				}
				
				// create more branch circuit breakers
				createBranchCircuitBreakerPanelPort(panel, 4L);

				// update the connections to the panel breaker
				powerPanelAddBranchCircuit(panel);

				// Validate the count of branch circuit breakers
				validateBranchCircuitBreakerCount(panel, branchCircuitBreakerCount + 4);
				
				// Validate branch circuit breaker values
				validateBranchCircuitBreakerValue(panel);

				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);

			}
			
		}
		
	}
	

	@Test
	public final void testCreatePanelBreakersAfterFPDUBreaker() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker count
		validateFPDUInputBreakerCount(fpduItem, 1);
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);

				int branchCircuitBreakerCount = 0;
				if (null != panel.getPowerPorts()) {
					for (PowerPort pp: panel.getPowerPorts()) {
						if (pp.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) 
							branchCircuitBreakerCount++;
					}
				}
				
				// create more branch circuit breakers
				createBranchCircuitBreakerPanelPort(panel, 4L);

				// update the connections to the panel breaker
				powerPanelAddBranchCircuit(panel);

				// Validate the count of branch circuit breakers
				validateBranchCircuitBreakerCount(panel, branchCircuitBreakerCount + 4);
				
				// Validate branch circuit breaker values
				validateBranchCircuitBreakerValue(panel);

				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);

			}
			
		}
		
	}
	

	@Test
	public final void testCreatePanelBreakersThenUpdateValues() throws Throwable {
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU_WITH_2PANELS");
		
		Set<Item> panels = fpduItem.getChildItems();
		for (Item panel: panels) {
			if (panel.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) && panel.getSubclassLookup().getLkpValueCode() != null) {
				LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
						panel.getItemId(), null, null, PowerChainLookup.Action.POWER_PANEL_CREATE_BREAKER_PORT);
		
				List<String> expectedErrorCodes = new ArrayList<String>();
				processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
				// validate panel breaker count
				validatePanelBreakerCount(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
				// validate connection from branch circuit breaker to panel breaker
				validateBranchCircuitBreakerConnectionToPanelBreaker(panel);
				
				// validate connection from panel breaker to fpdu input breaker
				validatePanelBreakerConnectionToFPDUInputBreaker(panel);
				
				// change the panel values
				changePanelValues( (MeItem) panel );
				// update the panel breaker values with the new changes
				powerPanelUpdateBranchCircuitValueTest(panel);
				
				// Validate panel breaker values
				validatePanelBreakerValue(panel);
				
			}
			
		}
		
	}


}
