package com.raritan.tdz.item.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class StorageStateTests extends StateTestBase {

	// test storage cabinet with no children
	@Test
	public final void testStorageCabinetWithNoChildren() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to STORAGE
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
	}
	
	// test storage cabinet with children in storage
	@Test
	public final void testStorageCabinetWithChildrenInStorage() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// Set the children items state to STORAGE
		Set<Item> children = cabItem.getChildItems();
		for (Item child: children) {
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				continue;
			}

			List<ValueIdDTO> itemDto = setItemState(child.getItemId(), SystemLookup.ItemStatus.STORAGE);
			
			// save item with expected error code
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(child.getItemId(), itemDto, errorCodes);
			
		}
		
		// set item state to STORAGE
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
	}

	// test storage cabinet with children in non-storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageCabinetWithOneChildInNonStorage() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// Set the children items state to STORAGE
		Set<Item> children = cabItem.getChildItems();
		for (Item child: children) {
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				continue;
			}

			List<ValueIdDTO> itemDto = setItemState(child.getItemId(), SystemLookup.ItemStatus.STORAGE);
			
			// save item with expected error code
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(child.getItemId(), itemDto, errorCodes);
			
			// set only 1 child to storage state
			break;
		}
		
		// set item state to STORAGE
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
	}

	// test storage cabinet with children in non-storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageCabinetWithAllChildInNonStorage() throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// set item state to STORAGE
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);
		
	}

	// test storage chassis with no blades
	@Test
	public final void testStorageChassisWithNoBlade() throws Throwable {

		ItItem devChassisItem = createChassisInPlannedState();

		// set item state to STORAGE
		List<ValueIdDTO> itemDto = setItemState(devChassisItem.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(devChassisItem.getItemId(), itemDto, errorCodes);
		

	}
	
	// test storage chassis with blade in storage
	@Test
	public final void testStorageChassisWithBladeInStorage() throws Throwable {

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
		
		// set all the blade in storage state
		Set<Item> blades = chassis.getChildItems();
		for (Item blade: blades) {
			List<ValueIdDTO> bladeItemDto = setItemState(blade.getItemId(), SystemLookup.ItemStatus.STORAGE);

			// save the chassis with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			saveItemWithExpectedErrorCode(blade.getItemId(), bladeItemDto, errorCodes);

		}
		
		// set the chassis in storage state
		List<ValueIdDTO> chassisItemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		
		
		// save the chassis with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(chassis.getItemId(), chassisItemDto, errorCodes);

	}
	
	// test storage chassis with blade in non-storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageChassisWithAllBladesInNonStorage() throws Throwable {

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
		
		// set the chassis in storage state, but the blade is still in planned state
		List<ValueIdDTO> itemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save the chassis with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		saveItemWithExpectedErrorCode(chassis.getItemId(), itemDto, errorCodes);

	}

	// test storage chassis with blade in non-storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageChassisWithOneBladesInNonStorage() throws Throwable {

		// Create chassis in cabinet with blades, all in planned state
		List<Item> itemList = createChassisWithBladesInPlannedState();
		
		for (Item item: itemList) {
			if (null != item.getSubclassLookup() &&
					item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BLADE_SERVER) { 
				List<ValueIdDTO> bladeItemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.STORAGE);

				// save the blade in STORAGE state with no error
				List<String> errorCodes = new ArrayList<String>(); // no error expected
				saveItemWithExpectedErrorCode(item.getItemId(), bladeItemDto, errorCodes);

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
		

		// set the chassis in storage state, but the blade is still in planned state
		List<ValueIdDTO> itemDto = setItemState(chassis.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// save the chassis with expected error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		saveItemWithExpectedErrorCode(chassis.getItemId(), itemDto, errorCodes);

	}

	// test storage floor pdu with no panels
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageFloorPDUWithNoPanels() throws Throwable {
		
		// create FPDU in planned state, no panels
		MeItem fpdu = createFloorPDUInPlannedState();
		
		// set the fpdu state to storage
		List<ValueIdDTO> itemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.STORAGE);

		// save the chassis with expected error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		errorCodes.add("ItemValidator.invalidTransition");
		saveItemWithExpectedErrorCode(fpdu.getItemId(), itemDto, errorCodes);

	}
	
	// test storage floor pdu with panels in storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageFloorPDUWithPanelInStorage() throws Throwable {
		
		MeItem fpdu = createFloorPDUWithPanelsInPlannedState();
		
		Assert.assertNotNull(fpdu);
		
		// Set all the panels to storage
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			List<ValueIdDTO> panelItemDto = setItemState(panel.getItemId(), SystemLookup.ItemStatus.STORAGE);

			// save the blade in storaged state with no error
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			errorCodes.add("ItemValidator.invalidTransition");
			saveItemWithExpectedErrorCode(panel.getItemId(), panelItemDto, errorCodes);

		}
		
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.STORAGE);

		// save the blade in storaged state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);
		
	}
	
	// test storage floor pdu with panels in non-storage
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageFloorPDUWithPanelInNonStorage() throws Throwable {

		MeItem fpdu = createFloorPDUWithPanelsInPlannedState();
		
		Assert.assertNotNull(fpdu);
		
		// Do not set the panels to storage
		
		// Set the floor pdu in storage state
		List<ValueIdDTO> fpduItemDto = setItemState(fpdu.getItemId(), SystemLookup.ItemStatus.STORAGE);

		// save the blade in storaged state with no error
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.invalidTransition");
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		saveItemWithExpectedErrorCode(fpdu.getItemId(), fpduItemDto, errorCodes);

		// Validate all the panels are in storage
		Set<Item> panels = fpdu.getChildItems();
		for (Item panel: panels) {
			session.refresh(panel); // update the panel and ports information from the database
			validateItemStatus(panel.getItemId(), SystemLookup.ItemStatus.STORAGE);
		}
		validateItemStatus(fpdu.getItemId(), SystemLookup.ItemStatus.STORAGE);
	}


	///////////////////////
	//// Item name blank test
	//////////////////////
	
	private final void testStorageExistingItemWithNoName(Item item) throws Throwable {
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.STORAGE);
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		// validate status
		validateStatus(item.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// set the item name to blank
		itemDto = setItemName(item.getItemId(), "");
		// save item with expected error code
		errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);

		// validate generated name
		validateGeneratedItemName(item.getItemId());
		

	} 
	
	private final void testStorageNewItemWithNoName(Item item) throws Throwable {
		
		// Create a cabinet item 
		// CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.STORAGE);
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		// validate status
		validateStatus(item.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		// set the item name to blank
		itemDto = setItemName(item.getItemId(), "");
		// save item with expected error code
		errorCodes = new ArrayList<String>(); // no error expected
		
		Long itemId = saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes);

		// validate generated name
		validateGeneratedItemName(itemId);

		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());
		

	}

	
	@Test
	public final void testStorageExistingCabinetWithNoName() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		testStorageExistingItemWithNoName(cabItem);

	}
	
	
	@Test
	public final void testStorageNewCabinetWithNoName() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		
		testStorageNewItemWithNoName(cabItem);

	}

	
	@Test
	public final void testStorageExistingChassisWithNoName() throws Throwable {
		
		// Create a cabinet item 
		ItItem devChassisItem = createChassisInPlannedState();
		
		testStorageExistingItemWithNoName(devChassisItem);

	}
	

	@Test
	public final void testStorageNewChassisWithNoName() throws Throwable {
		
		// Create a cabinet item 
		ItItem devChassisItem = createChassisInPlannedState();
		
		testStorageNewItemWithNoName(devChassisItem);

	}

	
	private final void testStorageExistingItem(Item item, List<String> errorCodes) throws Throwable {
		
		// set item state to archived
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), SystemLookup.ItemStatus.STORAGE);
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		// validate status
		validateStatus(item.getItemId(), (errorCodes.size() > 0) ? SystemLookup.ItemStatus.PLANNED : SystemLookup.ItemStatus.STORAGE);
		
	} 
	
	private final void testStorageNewItem(Item item, List<String> errorCodes) throws Throwable {
		
		// Create a cabinet item 
		// CabinetItem cabItem = createCabinetInPlannedState();
		
		Map<String, UiComponentDTO> itemMap = itemHome.getItemDetails( item.getItemId(), getTestAdminUser() );
		
		// set item state to planned
		List<ValueIdDTO> itemDto = null; // setItemState(itemMap, SystemLookup.ItemStatus.PLANNED);

		// set the item status to storage
		itemDto = setItemState(itemMap, SystemLookup.ItemStatus.STORAGE);
		
		itemDto = setPowerPortNewItem(itemMap, -1L);
		
		itemDto = setItemName(itemMap, item.getItemName() + "_NEW");
		
		itemDto = setItemId(itemMap, -1L);
		
		Long itemId = saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes);

		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());

	}

	private final void testStorageExistingItem(Item item) throws Throwable {

		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		errorCodes.add("ItemValidator.invalidTransition");
		
		testStorageExistingItem(item, errorCodes);
		
	}
	
	private final void testStorageNewItem(Item item) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.decomissionToStorage");
		errorCodes.add("ItemValidator.invalidTransition");
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testStorageNewItem(item, errorCodes);
		
	}

	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingFPDU() throws Throwable {
		
		MeItem fpdu = createFloorPDUInPlannedState();
		
		testStorageExistingItem(fpdu);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewFPDU() throws Throwable {
		
		MeItem fpdu = createFloorPDUInPlannedState();

		testStorageNewItem(fpdu);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingVM() throws Throwable {
		
		ItItem vm = createNewTestDeviceVM("VM_IN_STORAGE");
		
		testStorageExistingItem(vm);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewVM() throws Throwable {
		
		ItItem vm = createNewTestDeviceVM("VM_IN_STORAGE");

		testStorageNewItem(vm);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingDataPanel() throws Throwable {
		
		ItItem dp = createNewTestDataPanel("DATAPANEL_IN_STORAGE", null);
		
		testStorageExistingItem(dp);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewDataPanel() throws Throwable {
		
		ItItem dp = createNewTestDataPanel("DATAPANEL_IN_STORAGE", null);

		testStorageNewItem(dp);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingPowerOutlet() throws Throwable {
		
		MeItem po = createPowerOutlet("POWEROUTLET_IN_STORAGE", null, null, null, null, 0L);
		
		testStorageExistingItem(po);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewPowerOutlet() throws Throwable {
		
		MeItem po = createPowerOutlet("POWEROUTLET_IN_STORAGE", null, null, null, null, 0L);

		testStorageNewItem(po);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingUPS() throws Throwable {
		
		MeItem ups = createNewTestUPS("UPS_IN_STORAGE");

		testStorageExistingItem(ups);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewUPS() throws Throwable {
		
		MeItem ups = createNewTestUPS("UPS_IN_STORAGE");

		testStorageNewItem(ups);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageExistingCRAC() throws Throwable {
		
		Item crac = createNewTestCRAC("CRAC_IN_STORAGE");

		testStorageExistingItem(crac);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testStorageNewCRAC() throws Throwable {
		
		Item crac = createNewTestCRAC("CRAC_IN_STORAGE");

		testStorageNewItem(crac);
		
	}
	
	private final void testStorageExistingItemSuccess(Item item) throws Throwable {

		List<String> errorCodes = new ArrayList<String>(); // no error expected
		
		testStorageExistingItem(item, errorCodes);
		
	}
	
	private final void testStorageNewItemSuccess(Item item) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		
		testStorageNewItem(item, errorCodes);
		
	}

	
	@Test
	public final void testStorageExistingCabinet() throws Throwable {
		
		CabinetItem cabItem = createCabinetInPlannedState();

		testStorageExistingItemSuccess(cabItem);
		
	}

	@Test
	public final void testStorageNewCabinet() throws Throwable {
		
		CabinetItem cabItem = createCabinetInPlannedState();

		testStorageNewItemSuccess(cabItem);
		
	}

	@Test
	public final void testStorageExistingRackable() throws Throwable { 
		
		ItItem devRackable = createNewTestPortDevice("DEV_RACKABLE_IN_STORAGE", SystemLookup.ItemStatus.PLANNED, -1);

		testStorageExistingItemSuccess(devRackable);
		
	}

	@Test
	public final void testStorageNewRackable() throws Throwable {
		
		ItItem devRackable = createNewTestPortDevice("DEV_RACKABLE_IN_STORAGE", SystemLookup.ItemStatus.PLANNED, -1);

		testStorageNewItemSuccess(devRackable);
		
	}

	@Test
	public final void testStorageExistingDevChassis() throws Throwable {
		
		ItItem devChassis = createNewTestPortDeviceChassis("DEV_CHASSIS_IN_STORAGE");

		testStorageExistingItemSuccess(devChassis);
		
	}

	@Test
	public final void testStorageNewDevChassis() throws Throwable {
		
		ItItem devChassis = createNewTestPortDeviceChassis("DEV_CHASSIS_IN_STORAGE");

		testStorageNewItemSuccess(devChassis);
		
	}
	
	@Test
	public final void testStorageExistingDevBlade() throws Throwable {
		
		ItItem devBlade = createNewTestPortDeviceBlade("DEV_BLADE_IN_STORAGE", 55, 1L);

		testStorageExistingItemSuccess(devBlade);
		
	}

	@Test
	public final void testStorageNewDevBlade() throws Throwable {
		
		ItItem devBlade = createNewTestPortDeviceBlade("DEV_BLADE_IN_STORAGE", 55, null);

		testStorageNewItemSuccess(devBlade);
		
	}
	
	@Test
	public final void testStorageExistingNWChassis() throws Throwable {
		
		ItItem nwChassis = createNewTestPortNetworkChassis("NW_CHASSIS_IN_STORAGE", 55);

		testStorageExistingItemSuccess(nwChassis);
		
	}

	@Test
	public final void testStorageNewNWChassis() throws Throwable {
		
		ItItem nwChassis = createNewTestPortNetworkChassis("NW_CHASSIS_IN_STORAGE", 55);

		testStorageNewItemSuccess(nwChassis);
		
	}

	@Test
	public final void testStorageExistingNWBlade() throws Throwable {
		
		ItItem nwBlade = createNewTestPortNetworkBlade("NW_BLADE_IN_STORAGE", -1);

		testStorageExistingItemSuccess(nwBlade);
		
	}

	@Test
	public final void testStorageNewNWBlade() throws Throwable {
		
		ItItem nwBlade = createNewTestPortNetworkBlade("NW_BLADE_IN_STORAGE", -1);

		testStorageNewItemSuccess(nwBlade);
		
	}
	
	@Test
	public final void testStorageExistingNWStackable() throws Throwable {
		
		ItItem nwStackable = createNewTestNetworkStack("NW_Stackable_IN_STORAGE", "Test-Stack");

		testStorageExistingItemSuccess(nwStackable);
		
	}

	@Test
	public final void testStorageNewNWStackable() throws Throwable {
		
		ItItem nwStackable = createNewTestNetworkStack("NW_Stackable_IN_STORAGE", "Test-Stack");

		testStorageNewItemSuccess(nwStackable);
		
	}

	@Test
	public final void testStorageExistingProbe() throws Throwable {
		
		ItItem probe = createNewTestProbe("PROBE_IN_STORAGE", SystemLookup.ItemStatus.PLANNED);

		testStorageExistingItemSuccess(probe);
		
	}

	@Test
	public final void testStorageNewProbe() throws Throwable {
		
		ItItem probe = createNewTestProbe("PROBE_IN_STORAGE", SystemLookup.ItemStatus.PLANNED);

		testStorageNewItemSuccess(probe);
		
	}
	
	@Test
	public final void testStorageExistingRPDU() throws Throwable {
		
		MeItem rpdu = createNewTestPortRPDU("RPDU_IN_STORAGE", SystemLookup.ItemStatus.PLANNED);

		testStorageExistingItemSuccess(rpdu);
		
	}

	@Test
	public final void testStorageNewRPDU() throws Throwable {
		
		MeItem rpdu = createNewTestPortRPDU("RPDU_IN_STORAGE", SystemLookup.ItemStatus.PLANNED);

		testStorageNewItemSuccess(rpdu);
		
	}

	/////////////////////////////////////////
	// Test Cabinet-Container and FS Devices
	/////////////////////////////////////////
	
	@Test
	public final void testContainerStorage() throws Throwable {
		
		testContainerFSStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}

	@Test
	public final void testFSDevStorage() throws Throwable {
		
		testFSDevContainerStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}

	@Test 
	public final void testInitialFSDevStatusAsStorage() throws Throwable {
		
		CabinetItem container = createFSDevWithContainer(SystemLookup.ItemStatus.STORAGE);
		
		validateStatus(container.getItemId(), SystemLookup.ItemStatus.STORAGE);
		
		Set<Item> fsDevices = container.getChildItems();
		for (Item fsDev: fsDevices) {
			validateStatus(fsDev.getItemId(), SystemLookup.ItemStatus.STORAGE);
		}
		
	}
	
	///////////////////////////////////////////////////////////
	// test passive item state change with the cabinet item state
	///////////////////////////////////////////////////////////
	
	@Test
	public final void testStorageCabinetWithPassiveItem() throws Throwable {
		
		testStorageCabinetWithPassiveItem(SystemLookup.ItemStatus.STORAGE);
		
	}

	/// Cabinet state transition tests
	
	@Test
	public final void testCabinetFromStorageToPlanned() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromStorageToInstalled() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromStorageToPoweredOff() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testCabinetFromStorageToStorage() throws Throwable {

		
		testCabinetFromStateToStateNoError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromStorageToOffSite() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testCabinetFromStorageToArchived() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.ARCHIVED);
		
	}


	// Device Chassis state transition tests
	
	@Test
	public final void testDeviceChassisFromStorageToPlanned() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromStorageToInstalled() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromStorageToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testDeviceChassisFromStorageToStorage() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromStorageToOffSite() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testDeviceChassisFromStorageToArchived() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test
	public final void testNetworkChassisFromStorageToPlanned() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromStorageToInstalled() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromStorageToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testNetworkChassisFromStorageToStorage() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromStorageToOffSite() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testNetworkChassisFromStorageToArchived() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test
	public final void testProbeFromStorageToPlanned() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromStorageToInstalled() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromStorageToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testProbeFromStorageToStorage() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromStorageToOffSite() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testProbeFromStorageToArchived() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.STORAGE, SystemLookup.ItemStatus.ARCHIVED);
		
	}


	
}

