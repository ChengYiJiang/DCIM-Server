package com.raritan.tdz.item.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class ArchiveStateTests extends StateTestBase {

	Long atLeastConnection = 1L;  
	
	// test: archive cabinet with no children, allowed
	@Test
	public final void testArchiveCabinetWithNoChildren() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);

		validateStatus(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

	}
	
	// test: archive cabinet with children in archive, allowed
	@Test
	public final void testArchiveCabinetWithChildrenInArchive() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// Set the children items state to Archived
		Set<Item> children = cabItem.getChildItems();
		for (Item child: children) {
			
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				continue;
			}
			List<ValueIdDTO> itemDto = setItemState(child.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			
			// save item with expected error code
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(child.getItemId(), itemDto, errorCodes);
			
			validateStatus(child.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			
		}
		
		// set cabinet item state to archived, allowed
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
		validateStatus(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		for (Item child: children) {
			
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				validateStatus(child.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			}
		}

	}

	// test archive cabinet with children in non-archive, expect server exception
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveCabinetWithOneChildInNonArchive() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// Set the children items state to Archived
		Set<Item> children = cabItem.getChildItems();
		for (Item child: children) {
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				continue;
			}

			List<ValueIdDTO> itemDto = setItemState(child.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			
			// save item with expected error code
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(child.getItemId(), itemDto, errorCodes);
			
			validateStatus(child.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			
			// set only 1 child to archive state
			break;
		}
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToArchived");
		try {
			saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		}
		finally {
		
			validateStatus(cabItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
	}

	// test archive cabinet with children in non-archive, expect server exception
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveCabinetWithAllChildInNonArchive() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToArchived");
		try {
			saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		}
		finally {
		
			validateStatus(cabItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
	}

	// With the cabinet in archive state, add child in non-archive state, expect excpetion
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetArchivedThenAddChildInNonArchiveState() throws Throwable {

		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
		// Now Add child item in planned state
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Test", null, cabItem.getItemId());
		
		List<ValueIdDTO> childItemDto = setItemState(devChassisItem.getItemId(), SystemLookup.ItemStatus.PLANNED);

		// save item with expected error code
		errorCodes.add("ItemValidator.invalidParentStatus");
		try {
			saveItemWithExpectedErrorCode(devChassisItem.getItemId(), childItemDto, errorCodes);
		}
		finally {
		
			validateStatus(cabItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateStatus(devChassisItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

	}
	

	// test archive chassis with no blades, allowed
	@Test
	public final void testArchiveChassisWithNoBlade() throws Throwable {

		ItItem devChassisItem = createChassisInPlannedState();

		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(devChassisItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(devChassisItem.getItemId(), itemDto, errorCodes);

		validateStatus(devChassisItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

	}
	
	// test archive chassis with blade in archive, allowed
	@Test
	public final void testArchiveChassisWithBladeInArchive() throws Throwable {

		// Create chassis in cabinet with blades, all in planned state
		List<Item> itemList = createChassisWithBladesInPlannedState();
		Item chassis = null;
		
		for (Item item: itemList) {
			if (null != item.getSubclassLookup() &&
					item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BLADE_CHASSIS) { 
				chassis = item;
				break;
			}
		}
		
		// set all the blade in archive state
		Set<Item> blades = chassis.getChildItems();
		for (Item blade: blades) {
			List<ValueIdDTO> bladeItemDto = setItemState(blade.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

			// save the chassis with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(blade.getItemId(), bladeItemDto, errorCodes);
			
			validateStatus(blade.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		}
		
		// set the chassis in archive state
		List<ValueIdDTO> chassisItemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		
		
		// save the chassis with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(chassis.getItemId(), chassisItemDto, errorCodes);

		validateStatus(chassis.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
	}
	
	// test archive chassis with blade in non-archive, expect server exception
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveChassisWithAllBladesInNonArchive() throws Throwable {

		// Create chassis in cabinet with blades, all in planned state
		List<Item> itemList = createChassisWithBladesInPlannedState();
		Item chassis = null;
		
		for (Item item: itemList) {
			if (null != item.getSubclassLookup() && 
					item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BLADE_CHASSIS) { 
				chassis = item;
				break;
			}
		}
		
		// set the chassis in archive state, but the blade is still in planned state
		List<ValueIdDTO> itemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save the chassis with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToArchived");
		try {
			saveItemWithExpectedErrorCode(chassis.getItemId(), itemDto, errorCodes);
		}
		finally {
		
			validateStatus(chassis.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

	}

	// test archive chassis with one blade in non-archive, expect server exception
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveChassisWithOneBladesInNonArchive() throws Throwable {

		// Create chassis in cabinet with blades, all in planned state
		List<Item> itemList = createChassisWithBladesInPlannedState();
		
		for (Item item: itemList) {
			if (null != item.getSubclassLookup() &&
					item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BLADE_SERVER) { 
				List<ValueIdDTO> bladeItemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

				// save the blade in archived state with no error
				List<String> errorCodes = new ArrayList<String>(); // no error expected
				saveItemWithExpectedErrorCode(item.getItemId(), bladeItemDto, errorCodes);

				validateStatus(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
				
				break;
			}
		}

		Item chassis = null;
		
		for (Item item: itemList) {
			if (null != item.getSubclassLookup() &&
					item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BLADE_CHASSIS) { 
				chassis = item;
				break;
			}
		}
		

		// set the chassis in archive state, but the blade is still in planned state
		List<ValueIdDTO> itemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// save the chassis with expected error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToArchived");
		try {
			saveItemWithExpectedErrorCode(chassis.getItemId(), itemDto, errorCodes);
		}
		finally {
		
			validateStatus(chassis.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

	}

	
	// With the cabinet in archive state, add child in non-archive state, expect exception
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testChassisArchivedThenAddBladeInNonArchiveState() throws Throwable {

		// Create Chassis and Cabinet in Archived state
		ItItem devChassisItem = createChassisInState(SystemLookup.ItemStatus.ARCHIVED); //createChassisInPlannedState();

		// Now Add blade item in planned state
		ItItem devBlade1Item = createNewTestDeviceBladeInChassis("UNITTEST-PORT-DEV_BLADE1-Test", devChassisItem.getParentItem().getItemId(), devChassisItem.getItemId(), null);
		List<ValueIdDTO> childItemDto = setItemState(devBlade1Item.getItemId(), SystemLookup.ItemStatus.PLANNED);

		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidParentStatus");
		errorCodes.add("ItemValidator.noCabinetInLocation");
		errorCodes.add("ItemValidator.noAvailableChassisInCabinet");
		try {
			saveItemWithExpectedErrorCode(devBlade1Item.getItemId(), childItemDto, errorCodes);
		}
		finally {

			validateStatus(devChassisItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateStatus(devBlade1Item.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
	}
	

	//////////////////////////////
	// FLOOR PDU ARCHIVE TEST
	//////////////////////////////
	
	
	// test archive floor pdu with no panels
	@Test
	public final void testArchiveFloorPDUWithNoPanels() throws Throwable {
		
		// create FPDU in planned state, no panels
		MeItem fpdu = createFloorPDUInPlannedState();
		
		// set the fpdu state to archived
		List<ValueIdDTO> itemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the chassis with expected error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), itemDto, errorCodes);

		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// set the fpdu state to planned to allow deletion
		itemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
		errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), itemDto, errorCodes);

	}
	
	// test archive floor pdu with panels in archive
	@Test
	public final void testArchiveFloorPDUWithPanelInArchive() throws Throwable {
		
		// MeItem fpdu = createFloorPDUWithPanelsInPlannedState();
		MeItem fpdu = createFloorPDU(true, true, false, false);
		
		Assert.assertNotNull(fpdu);
		
		session.clear();
		
		// Set all the panels to archive
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

			// save the panel in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);

			// validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			
		}
		
		session.clear(); // clear the session before validation the items, ports and connections
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		panels = fpdu.getChildItems();
		for (Item panel: panels) {
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validatePortStatus(panel.getItemId(), null); // port status should not change
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L); // no implicit connections established in the power panel
		}

		
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the blade in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		
		session.clear(); // clear the session before validation
		
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 0L);
		validateItemDestConnections(fpdu.getItemId(), 0L);

		
	}
	
	// test archive floor pdu with panels in non-archive
	@Test
	public final void testArchiveFloorPDUWithPanelInNonArchive() throws Throwable {

		// MeItem fpdu = createFloorPDUWithPanelsInPlannedState();
		MeItem fpdu = createFloorPDU(true, true, false, false);
		
		Assert.assertNotNull(fpdu);
		
		session.clear(); // clear the session before operating on the the item ids 
		
		// Do not set the panels to archive
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		
		session.clear(); // clear the session before validating

		// Validate all the panels are in archive
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			// session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validatePortStatus(panel.getItemId(), null); // port status should not change
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L); // no implicit connections established in the power panel

		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 0L);
		validateItemDestConnections(fpdu.getItemId(), 0L);

	}

	// test archive floor pdu with panels connected to the outlet that are in use
	@Test
	public final void testArchiveFloorPDUWithPanelConnectedToNonCircuitedOutlet() throws Throwable {
		
		// MeItem fpdu = createFloorPDUWithPanelsAndOutletInPlannedState();
		MeItem fpdu = createFloorPDU(true, true, true, false);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		
		session.clear(); // clear the session before validating
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 0L);
		validateItemDestConnections(fpdu.getItemId(), 0L);

		// validate that fpdu and panels have no connections
		validateItemConnections(fpdu.getItemId(), 0L);
		
		// Validate Power Outlets
		validatePowerOutletsCleared();
		
		session.clear();
		
	}
	
	// test archive floor pdu with panels connected to the outlet that are in use
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveFloorPDUWithPanelConnectedToCircuitedOutlet() throws Throwable {

		// MeItem fpdu = createFloorPDUWithPanelsAndOutletConnectedInPlannedState();
		MeItem fpdu = createFloorPDU(true, true, true, true);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); 
		errorCodes.add("ItemValidator.deleteConnected");
		try {
			saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		}
		finally {
		
			validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

	}
	
	@Test
	public final void testArchiveFloorPDUConnectedToUpsBankAlone() throws Throwable {
		
		MeItem fpdu = createFloorPDU(true, false, false, false);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateItemConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemConnections(fpdu.getItemId(), 0L);

	}
	

	@Test
	public final void testArchiveFloorPDUConnectedToPanelsAlone() throws Throwable {
		
		MeItem fpdu = createFloorPDU(false, true, false, false);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);

		// Validate that fpdu and all panels do not have any connections and are in archive state
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateItemConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemConnections(fpdu.getItemId(), 0L);

	}
	
	@Test
	public final void testArchiveFloorPDUConnectedToUpsBankAndPanels() throws Throwable {
		
		MeItem fpdu = createFloorPDU(true, true, false, false);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);

		// Validate that fpdu and all panels do not have any connections and are in archive state
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateItemConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// validate that fpdu and panels have no connections
		validateItemConnections(fpdu.getItemId(), 0L);
		

	}

	@Test
	public final void testArchiveFloorPDUConnectedToUpsBankAndPanelsAndOutletNotConnected() throws Throwable {
		
		MeItem fpdu = createFloorPDU(true, true, true, false);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);

		// Validate that fpdu and all panels do not have any connections and are in archive state
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validateItemConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// validate that fpdu and panels have no connections
		validateItemConnections(fpdu.getItemId(), 0L);
		

	}


	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveFloorPDUConnectedToUpsBankAndPanelsAndOutletConnected() throws Throwable {
		
		MeItem fpdu = createFloorPDU(true, true, true, true);
		
		Assert.assertNotNull(fpdu);

		session.clear();
		
		// Set the floor pdu in archive state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the fpdu in archived state with error
		List<String> errorCodes = new ArrayList<String>(); 
		errorCodes.add("ItemValidator.deleteConnected");
		
		try {
			saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		}
		finally {
			session.refresh(fpdu);
			// Validate that fpdu and all panels do not have any connections and are in archive state
			Set<Item> panels = fpdu.getChildItems();
			for (Item panel: panels) {
				session.refresh(panel); // update the panel and ports information from the database
				validateStatus(panel.getItemId(), SystemLookup.ItemStatus.PLANNED);
				validateItemConnections(panel.getItemId(), atLeastConnection);
			}
			validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
			
			// validate that fpdu and panels have no connections
			validateItemConnections(fpdu.getItemId(), atLeastConnection);
			
			// Validate Power Outlets
			// validatePowerOutletsNotCleared();
			
			session.clear();
			
		}
		

	}

	///////////////////////////
	// Power Panels Archive test
	//////////////////////////
	
	@Test
	public final void testArchivePowerPanelWithNoOutlets() throws Throwable {

		MeItem fpdu = createFloorPDU(true, true, false, false);
		
		Assert.assertNotNull(fpdu);

		session.clear(); // clear the session after creating all the items

		// Set all the panels to archive
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

			// save the blade in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);

		}

		session.clear(); // clear the session before validation the items, ports and connections
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		panels = fpdu.getChildItems();
		for (Item panel: panels) {
			// session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validatePortStatus(panel.getItemId(), null); // port status should not change
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 1L);
		validateItemDestConnections(fpdu.getItemId(), 0L);

	}

	@Test
	public final void testArchivePowerPanelWithOutletNotCircuited() throws Throwable {

		MeItem fpdu = createFloorPDU(true, true, true, false);
		
		Assert.assertNotNull(fpdu);

		session.clear(); // clear the session after creating all the items

		// Set all the panels to archive
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

			// save the blade in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);

		}

		session.clear(); // clear the session before validation the items, ports and connections
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		panels = fpdu.getChildItems();
		for (Item panel: panels) {
			// session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validatePortStatus(panel.getItemId(), null); // port status should not change
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L);
		}
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 1L); // to ups bank
		validateItemDestConnections(fpdu.getItemId(), 0L);

		// Validate Power Outlets
		validatePowerOutletsCleared();

		// Set all the panels to planned to allow deletion
		panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.PLANNED);

			// save the blade in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);

		}

		session.clear(); // clear the session before validation the items, ports and connections

	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchivePowerPanelWithOutletCircuited() throws Throwable {
		
		boolean throwBussEx = false;
		BusinessValidationException ex = null;
		
		MeItem fpdu = createFloorPDU(true, true, true, true);
		
		Assert.assertNotNull(fpdu);

		session.clear(); // clear the session after creating all the items

		// Set all the panels to archive
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

			boolean validateForSaveSuccessful = true;
			// save the blade in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			errorCodes.add("ItemValidator.deleteConnected");
			try {
				saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);
			} catch (BusinessValidationException be) {
				session.clear(); // clear the session before validation the items, ports and connections
				validateStatus(panel.getItemId(), SystemLookup.ItemStatus.PLANNED);
				validatePortStatus(panel.getItemId(), null); // port status should not change
				validateItemSourceConnections(panel.getItemId(), atLeastConnection);
				validateItemDestConnections(panel.getItemId(), atLeastConnection);
				validateForSaveSuccessful = false;
				throwBussEx = true;
				ex = be;
			}
			if (validateForSaveSuccessful) {
				session.clear(); // clear the session before validation the items, ports and connections
				validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
				validatePortStatus(panel.getItemId(), null); // port status should not change
				validateItemSourceConnections(panel.getItemId(), 0L);
				validateItemDestConnections(panel.getItemId(), 0L);
			}

		}

		session.clear(); // clear the session before validation the items, ports and connections
		
		// Validate that fpdu and all panels do not have any connections and are in archive state
		/*panels = fpdu.getChildItems();
		for (Item panel: panels) {
			// session.refresh(panel); // update the panel and ports information from the database
			validateStatus(panel.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
			validatePortStatus(panel.getItemId(), null); // port status should not change
			validateItemSourceConnections(panel.getItemId(), 0L);
			validateItemDestConnections(panel.getItemId(), 0L);
		}*/
		validateStatus(fpdu.getItemId(), SystemLookup.ItemStatus.PLANNED);
		validatePortStatus(fpdu.getItemId(), null); // port status should not change
		
		// validate that fpdu and panels have no connections
		validateItemSourceConnections(fpdu.getItemId(), 1L); // to ups bank
		validateItemDestConnections(fpdu.getItemId(), atLeastConnection); // to panels not been moved to archived

		// TODO:: for some panels the connection is not created between FPDU and panel bcoz of validation amps rating
		// validatePowerOutletsNotCleared();
		
		if (true == throwBussEx) {
			throw ex;
		}
		
	}

	///////////////////////////
	// Power Outlet Archive test
	//////////////////////////

	@Test
	public final void testArchivePowerOutletWithNoPanel() throws Throwable {
		
		MeItem powerOutlet = createPowerOutlet("INT_TEST_PO_1", null, null, null);
		
		session.clear(); // clear the session after creating all the items

		List<ValueIdDTO> poItemDto = setItemState(powerOutlet.getItemId(), SystemLookup.ItemStatus.ARCHIVED);

		// save the blade in archived state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(powerOutlet.getItemId(), poItemDto, errorCodes);

		// Validate Power Outlets
		validatePowerOutletsCleared();

		// set item status to planned to allow deletion
		poItemDto = setItemState(powerOutlet.getItemId(), SystemLookup.ItemStatus.PLANNED);

		// save the blade in archived state with no error
		errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(powerOutlet.getItemId(), poItemDto, errorCodes);
		
		session.clear(); // clear the session after creating all the items
	}

	@Test
	public final void testArchivePowerOutletWithPanel() throws Throwable {

		MeItem fpdu = createFloorPDU(true, true, true, false);
		
		Assert.assertNotNull(fpdu);

		session.clear(); // clear the session after creating all the items
		
		for (Long itemId: powerOutlet) {
			List<ValueIdDTO> poItemDto = setItemState(itemId, SystemLookup.ItemStatus.ARCHIVED);

			// save the blade in archived state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(itemId, poItemDto, errorCodes);
			
			session.clear();
			
			validatePowerOutletCleared(itemId);
		}
		
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			validateItemUsedFlag(panel.getItemId(), false, 415); // Check for branch circuit breaker port
		}
		
		session.clear();
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveCircuitedPowerOutlet() throws Throwable {

		MeItem fpdu = createFloorPDU(true, true, true, true);
		
		boolean throwBe = false;
		BusinessValidationException be = null;
		
		Assert.assertNotNull(fpdu);

		session.clear(); // clear the session after creating all the items
		
		for (Long itemId: powerOutlet) {
			List<ValueIdDTO> poItemDto = setItemState(itemId, SystemLookup.ItemStatus.ARCHIVED);

			// save the power outlet in archived state with expected error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			errorCodes.add("ItemValidator.deleteConnected");
			try {
				saveItemWithExpectedErrorCode(itemId, poItemDto, errorCodes);
			}
			catch (BusinessValidationException ex) {
				throwBe = true;
				be = ex;

				session.clear();
				
				validatePowerOutletNotCleared(itemId);

			}
			
		}
	
		// TODO: Validate the used flag of the panel, should be the same as before save
		/*Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			validateItemUsedFlag(panel.getItemId(), true);
		}*/
		
		session.clear();
		
		if (throwBe) {
			throw be;
		}
		
	}
	
	
	///////////////////////
	//// Item name blank test
	//////////////////////
	
	private final void testArchiveExistingItemWithNoName(Item item) throws Throwable {
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		// validate status
		validateStatus(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// set the item name to blank
		itemDto = setItemName(item.getItemId(), "");
		// save item with expected error code
		errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);

		// validate generated name
		validateGeneratedItemName(item.getItemId());
		
		// set item state to planned to allow deletion
		itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.PLANNED);
		errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);

	} 
	
	private final void testArchiveNewItemWithNoName(Item item, List<String> newItemErrorCodes) throws Throwable {
		
		// Create a cabinet item 
		// CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		// validate status
		validateStatus(item.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		// set the item name to blank
		itemDto = setItemName(item.getItemId(), "");
		// save item with expected error code
		errorCodes = new ArrayList<String>(); // no error expected
		if (null == newItemErrorCodes) {
			newItemErrorCodes = errorCodes;
		}
		Long itemId = saveNewItemWithExpectedErrorCode(-1L, itemDto, newItemErrorCodes);

		// TODO:: set the item ids for the ports (data, power and sensor)
		
		// validate generated name
		validateGeneratedItemName(itemId);

		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());
		

	}

	
	@Test
	public final void testArchiveExistingCabinetWithNoName() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		testArchiveExistingItemWithNoName(cabItem);

	}
	
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveNewCabinetWithNoName() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		testArchiveNewItemWithNoName(cabItem, errorCodes);

	}

	
	@Test
	public final void testArchiveExistingChassisWithNoName() throws Throwable {
		
		// Create a cabinet item 
		ItItem devChassisItem = createChassisInPlannedState();
		
		testArchiveExistingItemWithNoName(devChassisItem);

	}
	

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testArchiveNewChassisWithNoName() throws Throwable {
		
		// Create a chassis item 
		ItItem devChassisItem = createChassisInPlannedState();
		
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		testArchiveNewItemWithNoName(devChassisItem, errorCodes);

	}

	// MeItem fpdu = createFloorPDUInPlannedState();
	@Test
	public final void testArchiveExistingFPDUWithNoName() throws Throwable {
		
		MeItem fpdu = createFloorPDUInPlannedState();
		
		testArchiveExistingItemWithNoName(fpdu);
		
	}

	// @Test
	public final void testArchiveNewFPDUWithNoName() throws Throwable {
		
		MeItem fpdu = createFloorPDUInPlannedState();
		
		testArchiveNewItemWithNoName(fpdu, null);
		
	}

	/////////////////////////////////////////
	// Test Cabinet-Container and FS Devices
	/////////////////////////////////////////
	
	@Test
	public final void testContainerArchived() throws Throwable {
		
		testContainerFSStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	@Test
	public final void testFSDevArchived() throws Throwable {
		
		testFSDevContainerStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	@Test
	public final void testContainerArchivedToPlanned() throws Throwable {
		
		CabinetItem container = testContainerFSStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(container, SystemLookup.ItemStatus.PLANNED, errorCodes);
		
		Set<Item> children = container.getChildItems();
		
		for (Item child: children) {
			validateStatus(child.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
		
	}

	@Test
	public final void testFSDevArchivedToPlanned() throws Throwable {
		
		CabinetItem container = testFSDevContainerStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setChildrenStatus(container, SystemLookup.ItemStatus.PLANNED, errorCodes);
		
		validateStatus(container.getItemId(), SystemLookup.ItemStatus.PLANNED);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testInitialFSDevStatusAsArchived() throws Throwable {
		
		CabinetItem container = createFSDevWithContainer(SystemLookup.ItemStatus.ARCHIVED);
		
		validateStatus(container.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		Set<Item> fsDevices = container.getChildItems();
		for (Item fsDev: fsDevices) {
			validateStatus(fsDev.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		}
		
	}
	
	
	@Test
	public final void testContainerPlannedFromArchived() throws Throwable {
		
		CabinetItem container = testContainerFSStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(container, SystemLookup.ItemStatus.PLANNED, errorCodes);

		Set<Item> fsDevices = container.getChildItems();
		for (Item fsDev: fsDevices) {
			validateStatus(fsDev.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

	}

	@Test
	public final void testFSDevPlannedFromArchived() throws Throwable {
		
		CabinetItem container = testFSDevContainerStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);

		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setChildrenStatus(container, SystemLookup.ItemStatus.PLANNED, errorCodes);

		validateStatus(container.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
	}
	
	///////////////////////////////////////////////////////////
	// test passive item state change with the cabinet item state
	///////////////////////////////////////////////////////////
	
	@Test
	public final void testArchiveCabinetWithPassiveItem() throws Throwable {
		
		testStorageCabinetWithPassiveItem(SystemLookup.ItemStatus.ARCHIVED);
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// test from planned to any other state, all these tests should provide business validation exception ///
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public final void testVMFromArchivedToPlanned() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test //(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromArchivedToInstalled() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromArchivedToPoweredOff() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromArchivedToStorage() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromArchivedToOffSite() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testVMFromArchivedToArchived() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	/// Cabinet state transition tests
	
	@Test
	public final void testCabinetFromArchivedToPlanned() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromArchivedToInstalled() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromArchivedToPoweredOff() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test //(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromArchivedToStorage() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromArchivedToOffSite() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testCabinetFromArchivedToArchived() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.ARCHIVED);
		
	}


	
	// Device Chassis state transition tests
	
	@Test
	public final void testDeviceChassisFromArchivedToPlanned() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromArchivedToInstalled() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromArchivedToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test //(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromArchivedToStorage() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromArchivedToOffSite() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testDeviceChassisFromArchivedToArchived() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test
	public final void testNetworkChassisFromArchivedToPlanned() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromArchivedToInstalled() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromArchivedToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test //(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromArchivedToStorage() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromArchivedToOffSite() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testNetworkChassisFromArchivedToArchived() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test
	public final void testProbeFromArchivedToPlanned() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromArchivedToInstalled() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromArchivedToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test //(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromArchivedToStorage() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromArchivedToOffSite() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testProbeFromArchivedToArchived() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.ARCHIVED);
		
	}


	
}
