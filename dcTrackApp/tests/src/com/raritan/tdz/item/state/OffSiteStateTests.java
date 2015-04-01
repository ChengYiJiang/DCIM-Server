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

public class OffSiteStateTests extends StateTestBase {

	private void testNewItemInOffSite(Item item) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(item, SystemLookup.ItemStatus.OFF_SITE, errorCodes );
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewCabinet() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		testNewItemInOffSite(cabItem);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewChassis() throws Throwable {
		
		// Create a chassis item 
		ItItem devChassisItem = createChassisInPlannedState();
		testNewItemInOffSite(devChassisItem);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewDevBlade() throws Throwable {
		
		ItItem devBlade = createNewTestPortDeviceBlade("DEV_BLADE_IN_POWERED_OFF", 55, 1L);
		testNewItemInOffSite(devBlade);

	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewVM() throws Throwable {
		
		ItItem vm = createNewTestDeviceVM("VM_IN_POWERED_OFF");
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(vm, SystemLookup.ItemStatus.OFF_SITE, errorCodes );

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewDataPanel() throws Throwable {
		
		ItItem dp = createNewTestDataPanel("DATAPANEL_IN_POWER_OFF", null);
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(dp, SystemLookup.ItemStatus.OFF_SITE, errorCodes );

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewPowerOutlet() throws Throwable {
		
		MeItem po = createPowerOutlet("POWEROUTLET_IN_POWER_OFF", null, null, null, null, 0L);
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(po, SystemLookup.ItemStatus.OFF_SITE, errorCodes );
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewUPS() throws Throwable {
		
		MeItem ups = createNewTestUPS("UPS_IN_OFF_SITE");
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(ups, SystemLookup.ItemStatus.OFF_SITE, errorCodes );
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testOffSiteNewCRAC() throws Throwable {
		
		Item crac = createNewTestCRAC("CRAC_IN_OFF_SITE");
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(crac, SystemLookup.ItemStatus.OFF_SITE, errorCodes );

	}

	// Device Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromOffSiteToPlanned() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromOffSiteToInstalled() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromOffSiteToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromOffSiteToStorage() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test
	public final void testDeviceChassisFromOffSiteToOffSite() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromOffSiteToArchived() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromOffSiteToPlanned() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.PLANNED);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromOffSiteToInstalled() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromOffSiteToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromOffSiteToStorage() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test
	public final void testNetworkChassisFromOffSiteToOffSite() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromOffSiteToArchived() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromOffSiteToPlanned() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromOffSiteToInstalled() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromOffSiteToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromOffSiteToStorage() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test
	public final void testProbeFromOffSiteToOffSite() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromOffSiteToArchived() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.OFF_SITE, SystemLookup.ItemStatus.ARCHIVED);
		
	}


}
