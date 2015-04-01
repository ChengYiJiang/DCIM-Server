package com.raritan.tdz.item.state;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class PowerOffStateTests extends StateTestBase {

	private void testNewItemInPowerOff(Item item) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(item, SystemLookup.ItemStatus.POWERED_OFF, errorCodes );
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewCabinet() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		testNewItemInPowerOff(cabItem);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewChassis() throws Throwable {
		
		// Create a chassis item 
		ItItem devChassisItem = createChassisInPlannedState();
		testNewItemInPowerOff(devChassisItem);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewDevBlade() throws Throwable {
		
		ItItem devBlade = createNewTestPortDeviceBlade("DEV_BLADE_IN_POWERED_OFF", 55, 1L);
		testNewItemInPowerOff(devBlade);

	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewVM() throws Throwable {
		
		ItItem vm = createNewTestDeviceVM("VM_IN_POWERED_OFF");
		testNewItemInPowerOff(vm);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewDataPanel() throws Throwable {
		
		ItItem dp = createNewTestDataPanel("DATAPANEL_IN_POWER_OFF", null);
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(dp, SystemLookup.ItemStatus.POWERED_OFF, errorCodes );

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewPowerOutlet() throws Throwable {
		
		MeItem po = createPowerOutlet("POWEROUTLET_IN_POWER_OFF", null, null, null, null, 0L);
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(po, SystemLookup.ItemStatus.POWERED_OFF, errorCodes );
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewUPS() throws Throwable {
		
		MeItem ups = createNewTestUPS("UPS_IN_POWEROFF");
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(ups, SystemLookup.ItemStatus.POWERED_OFF, errorCodes );
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPowerOffNewCRAC() throws Throwable {
		
		Item crac = createNewTestCRAC("CRAC_IN_POWEROFF");
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(crac, SystemLookup.ItemStatus.POWERED_OFF, errorCodes );

	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// test from planned to any other state, all these tests should provide business validation exception ///
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPoweredOffToPlanned() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.PLANNED);
		
	}

	//@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPoweredOffToInstalled() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED);
		
	}

	// @Test
	public final void testVMFromPoweredOffToPoweredOff() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPoweredOffToStorage() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPoweredOffToOffSite() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromPoweredOffToArchived() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	// Device Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPoweredOffToPlanned() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPoweredOffToInstalled() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test
	public final void testDeviceChassisFromPoweredOffToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPoweredOffToStorage() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPoweredOffToOffSite() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromPoweredOffToArchived() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPoweredOffToPlanned() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPoweredOffToInstalled() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test
	public final void testNetworkChassisFromPoweredOffToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPoweredOffToStorage() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPoweredOffToOffSite() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromPoweredOffToArchived() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPoweredOffToPlanned() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPoweredOffToInstalled() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test
	public final void testProbeFromPoweredOffToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPoweredOffToStorage() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPoweredOffToOffSite() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromPoweredOffToArchived() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.ARCHIVED);
		
	}


}
