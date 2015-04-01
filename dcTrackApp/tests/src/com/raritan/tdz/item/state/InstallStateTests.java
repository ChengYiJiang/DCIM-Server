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
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class InstallStateTests extends StateTestBase {

	//////////////////////////////
	// Test Cabinet and childrens
	//////////////////////////////
	@SuppressWarnings("unused")
	private void testCabinet(Long parentStatus, Long childrenStatus) throws Throwable {

		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// Set the children items state to 'childrenStatus'
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setChildrenStatus(cabItem, childrenStatus, errorCodes);
		
		// set cabinet item state to archived, allowed
		setItemStatus(cabItem, parentStatus, errorCodes);

	}
	
	@Test
	public final void testCabinetInstalledAndChildInstalled() throws Throwable {
		
		// Create the cabinet in planned state with children (2) in planned state
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// set the cabinet to installed state
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(cabItem, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		
		// set the children to installed state
		setChildrenStatus(cabItem, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		
	}
	
	@Test
	public final void testCabinetInstalledAndChildNotInstalled() throws Throwable {

		// Create the cabinet in planned state with children (2) in planned state
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// set the cabinet to installed state
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(cabItem, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		
		// set the children to planned state
		setChildrenStatus(cabItem, SystemLookup.ItemStatus.PLANNED, errorCodes);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetNotInstalledAndChildInstalled() throws Throwable {
		
		// Create the cabinet in planned state with children (2) in planned state
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// keep the cabinet in planned state
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		// set the children to installed state
		errorCodes.add("ItemValidator.parentChildConstraint.installRequest");
		setChildrenStatus(cabItem, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		
	}
	
	public final void testCabinetNotInstalledAndChildNotInstalled() throws Throwable {

		// Create the cabinet in planned state with children (2) in planned state
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(true, true);
		
		// set the cabinet to installed state
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(cabItem, SystemLookup.ItemStatus.PLANNED, errorCodes);
		
		// set the children to installed state
		setChildrenStatus(cabItem, SystemLookup.ItemStatus.PLANNED, errorCodes);

	}

	/////////////////////////////////////////
	// Test Cabinet-Container and FS Devices
	/////////////////////////////////////////
	
	@Test
	public final void testContainerInstalled() throws Throwable {
		
		testContainerFSStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test
	public final void testFSDevInstalled() throws Throwable {
		
		testFSDevContainerStatus(SystemLookup.ItemStatus.PLANNED, SystemLookup.ItemStatus.INSTALLED);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInitialFSDevStatusAsArchived() throws Throwable {
		
		CabinetItem container = createFSDevWithContainer(SystemLookup.ItemStatus.INSTALLED);
		
		validateStatus(container.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		
		Set<Item> fsDevices = container.getChildItems();
		for (Item fsDev: fsDevices) {
			validateStatus(fsDev.getItemId(), SystemLookup.ItemStatus.INSTALLED);
		}
		
	}
	


	//////////////////////////
	// Test Chassis and Blade
	//////////////////////////
	private void testChassisAndBlade(boolean installChassis, boolean installBlades, List<String> errorCodes) throws Throwable {
		// Create chassis in cabinet with blades, all in planned state
		List<Item> itemList = createChassisWithBladesInPlannedState();
		
		Item cabinet = null;
		for (Item item: itemList) {
			if (null != item.getClassLookup() &&
					item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.CABINET) { 
				cabinet = item;
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
		
		// set all the blade in archive state
		Set<Item> blades = chassis.getChildItems();
		
		// set the cabinet to installed state
		setItemStatus(cabinet, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		
		// set the chassis to installed state
		if (installChassis) {
			chassis.setUPosition(1);
			setItemStatus(chassis, SystemLookup.ItemStatus.INSTALLED, errorCodes);
		}
		
		if (installBlades) {
			// set the blades to installed state
			long slotPosition = 1;
			for (Item blade: blades) {
	
				blade.setSlotPosition(slotPosition++);
				setItemStatus(blade, SystemLookup.ItemStatus.INSTALLED, errorCodes);
				
			}
		}
		
	}

	
	@Test
	public final void testChassisInstalledAndBladeInstalled() throws Throwable {
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		testChassisAndBlade(true, true, errorCodes);
		
	}
	
	@Test
	public final void testChassisInstalledAndBladeNotInstalled() throws Throwable {
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		testChassisAndBlade(true, false, errorCodes);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testChassisNotInstalledAndBladeInstalled() throws Throwable {
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		errorCodes.add("ItemValidator.parentChildConstraint.installRequest");
		testChassisAndBlade(false, true, errorCodes);
		
	}
	
	@Test
	public final void testChassisNotInstalledAndBladeNotInstalled() throws Throwable {
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		testChassisAndBlade(false, false, errorCodes);
	}

	//////////////////////////////
	// Test FPDU and Panels
	//////////////////////////////

	private void testFPDUAndPanel(boolean installFPDU, boolean installPanels, List<String> errorCodes) throws Throwable {

		MeItem fpdu = createFloorPDU(true, true, false, false);
		
		Assert.assertNotNull(fpdu);
		
		session.clear();
		
		if (installFPDU) {
			setItemStatus(fpdu, SystemLookup.ItemStatus.INSTALLED, errorCodes);
			session.clear();
		}
		
		if (installPanels) {
			setChildrenStatus(fpdu, SystemLookup.ItemStatus.INSTALLED, errorCodes);
			session.clear();
		}
		
	}

	
	@Test
	public final void testFPDUInstalledAndPanelInstalled() throws Throwable {

		List<String> errorCodes = new ArrayList<String>(); // no error expected

		testFPDUAndPanel(true, true, errorCodes);
		
	}
	
	public final void testFPDUInstalledAndPanelNotInstalled() throws Throwable {

		List<String> errorCodes = new ArrayList<String>(); // no error expected

		testFPDUAndPanel(true, false, errorCodes);
		
	}

	public final void testFPDUNotInstalledAndPanelInstalled() throws Throwable {

		List<String> errorCodes = new ArrayList<String>(); // no error expected

		testFPDUAndPanel(false, true, errorCodes);

	}

	//////////////////////
	// Initial State tests
	//////////////////////
	private void testNewItemInInstalled(Item item) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>(); // error code expected
		errorCodes.add("ItemValidator.invalidTransition.newItem");
		
		testNewItemInState(item, SystemLookup.ItemStatus.INSTALLED, errorCodes );
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewCabinet() throws Throwable {
		
		// Create a cabinet item 
		CabinetItem cabItem = createCabinetInPlannedState();
		testNewItemInInstalled(cabItem);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewChassis() throws Throwable {
		
		// Create a chassis item 
		ItItem devChassisItem = createChassisInPlannedState();
		testNewItemInInstalled(devChassisItem);
		
	}
	
	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewDevBlade() throws Throwable {
		
		ItItem devBlade = createNewTestPortDeviceBlade("DEV_BLADE_IN_INSTALLED", 55, 1L);
		testNewItemInInstalled(devBlade);

	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewVM() throws Throwable {
		
		ItItem vm = createNewTestDeviceVM("VM_IN_INSTALLED");
		testNewItemInInstalled(vm);

	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewDataPanel() throws Throwable {
		
		ItItem dp = createNewTestDataPanel("DATAPANEL_IN_INSTALLED", null);
		testNewItemInInstalled(dp);

	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewPowerOutlet() throws Throwable {
		
		MeItem po = createPowerOutlet("POWEROUTLET_IN_INSTALLED", null, null, null, null, 0L);
		testNewItemInInstalled(po);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewUPS() throws Throwable {
		
		MeItem ups = createNewTestUPS("UPS_IN_INSTALLED");

		MeItem upsBank = createNewTestUPSBank("UPSBank_IN_INSTALLED");
		
		ups.setUpsBankItem(upsBank);
		
		itemHome.saveItem( ups );
		
		testNewItemInInstalled(ups);
		
	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testInstallNewCRAC() throws Throwable {
		
		Item crac = createNewTestCRAC("CRAC_IN_INSTALLED");
		testNewItemInInstalled(crac);

	}

	///////////////////////////////////////////////////////////
	// test passive item state change with the cabinet item state
	///////////////////////////////////////////////////////////
	
	@Test
	public final void testInstallCabinetWithPassiveItem() throws Throwable {
		
		testStorageCabinetWithPassiveItem(SystemLookup.ItemStatus.INSTALLED);
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// test from installed to any other state, all these tests should provide business validation exception ///
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromInstalledToPlanned() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromInstalledToPoweredOff() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromInstalledToStorage() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromInstalledToOffSite() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testVMFromInstalledToArchived() throws Throwable {

		
		testVMFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	@Test
	public final void testVMFromInstalledToInstalled() throws Throwable {

		
		testVMFromStateToStateNoError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.INSTALLED);
		
	}


	/// Cabinet state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromInstalledToPlanned() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testCabinetFromInstalledToInstalled() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromInstalledToPoweredOff() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromInstalledToStorage() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromInstalledToOffSite() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetFromInstalledToArchived() throws Throwable {

		
		testCabinetFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.ARCHIVED);
		
	}

	// Device Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromInstalledToPlanned() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testDeviceChassisFromInstalledToInstalled() throws Throwable {

		
		testDeviceFromStateToStateNoError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromInstalledToPoweredOff() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromInstalledToStorage() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromInstalledToOffSite() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceChassisFromInstalledToArchived() throws Throwable {

		
		testDeviceFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Network Chassis state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromInstalledToPlanned() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testNetworkChassisFromInstalledToInstalled() throws Throwable {

		
		testNetworkFromStateToStateNoError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromInstalledToPoweredOff() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromInstalledToStorage() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromInstalledToOffSite() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testNetworkChassisFromInstalledToArchived() throws Throwable {

		
		testNetworkFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.ARCHIVED);
		
	}
	

	// Probe state transition tests
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromInstalledToPlanned() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.PLANNED);
		
	}

	@Test
	public final void testProbeFromInstalledToInstalled() throws Throwable {

		
		testProbeFromStateToStateNoError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.INSTALLED);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromInstalledToPoweredOff() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromInstalledToStorage() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.STORAGE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromInstalledToOffSite() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.OFF_SITE);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testProbeFromInstalledToArchived() throws Throwable {

		
		testProbeFromStateToStateError(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.ARCHIVED);
		
	}



}
