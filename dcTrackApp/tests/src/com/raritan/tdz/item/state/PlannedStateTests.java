package com.raritan.tdz.item.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class PlannedStateTests extends StateTestBase {

	///////////////////////////////////////////////////////////
	// test passive item state change with the cabinet item state
	///////////////////////////////////////////////////////////
	
	@Test
	public final void testInstallCabinetWithPassiveItem() throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", SystemLookup.ItemStatus.ARCHIVED);
		
		ItItem passiveItem1 = createNewTestPassive("UNITTEST-PASSIVE-1-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		ItItem passiveItem2 = createNewTestPassive("UNITTEST-PASSIVE-2-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED);
		
		cabinetItem.addChildItem(passiveItem1);
		cabinetItem.addChildItem(passiveItem2);

		// set cabinet state to testStatus
		List<ValueIdDTO> itemDto = setItemState(cabinetItem.getItemId(), SystemLookup.ItemStatus.PLANNED);

		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabinetItem.getItemId(), itemDto, errorCodes);

		// check the children items state as testStatus
		Set<Item> children = cabinetItem.getChildItems();
		for (Item child: children) {
			// Passive item should be in the same state as cabinet
			validateStatus(child.getItemId(), SystemLookup.ItemStatus.PLANNED);
			
			// the parent item id should be still set to the cabinet
			validateParentItem(child.getItemId(), cabinetItem.getItemId());
			
		}

	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// test from planned to any other state, all these tests should provide business validation exception ///
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public final void testVMFromPlannedToPlanned() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testVMFromPlannedToInstalled() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPlannedToPoweredOff() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPlannedToStorage() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPlannedToOffSite() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testVMFromPlannedToArchived() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	
	// Cabinet state transition tests
	
	@Test
	public final void testCabinetFromPlannedToPlanned() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testCabinetFromPlannedToInstalled() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromPlannedToPoweredOff() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testCabinetFromPlannedToStorage() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromPlannedToOffSite() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testCabinetFromPlannedToArchived() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	
	// Device Chassis state transition tests
	
	@Test
	public final void testDeviceChassisFromPlannedToPlanned() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testDeviceChassisFromPlannedToInstalled() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPlannedToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testDeviceChassisFromPlannedToStorage() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPlannedToOffSite() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testDeviceChassisFromPlannedToArchived() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test
	public final void testNetworkChassisFromPlannedToPlanned() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testNetworkChassisFromPlannedToInstalled() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPlannedToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testNetworkChassisFromPlannedToStorage() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPlannedToOffSite() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testNetworkChassisFromPlannedToArchived() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test
	public final void testProbeFromPlannedToPlanned() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test
	public final void testProbeFromPlannedToInstalled() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPlannedToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test
	public final void testProbeFromPlannedToStorage() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPlannedToOffSite() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test
	public final void testProbeFromPlannedToArchived() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.ARCHIVED);
		
	}



}
