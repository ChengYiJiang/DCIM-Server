package com.raritan.tdz.port;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;


public class PowerSupplyPortTests extends PowerPortTests {

	static long MODEL_ID = 1015; //9468; // power supply ports : 4 ports
	/* ---------------------------ItemObjectFactoryImpl.java----------------------------------------- */
	/* ---------------- Applicable Item Class tests -------------------- */
	/* -------------------------------------------------------------------- */
	// this should not throw any exception errors
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClass() throws Throwable {
		ItItem deviceItem = createNewTestPortDeviceBlade("UNITTEST-PORT-PS-Dev-Test", 55, 1L); // cabinet BK
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		// BusinessValidationException : PortValidator.powerSupplyUnsupportedClass
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				if (entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}
		}

	}

	// this should not throw any exception errors
	// FIXME:: enable test after fixing the test Cabinet creation problem
	// @Test
	public final void testNetworkChassisItemClass() throws Throwable {
		ItItem deviceItem = createNewTestPortNetworkChassis("UNITTEST-PORT-PS-NW-Test", 55); // Cabinet BK
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		// valueIdDTOList = ;
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( deviceItem.getItemId(), null /*userInfo */ );
		assertNotNull( item );
	}
	
	@Test
	public final void testProbeItemClass() throws Throwable {
		ItItem deviceItem = createNewPortTestProbe("UNITTEST-PORT-PS-Probe-Test", 55); // Cabinet BK
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			Map<String, UiComponentDTO> item = itemHome.getItemDetails( deviceItem.getItemId(), null /*userInfo */ );
			assertNotNull( item );
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				if (entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}
	

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCRACItemClass() throws Throwable {
		Item deviceItem = createNewTestCRAC("UNITTEST-PORT-PS-CRAC-Test");
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				if (entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}
		}

	}
	
	// this should throw an exception. Catch and then verify
	// This test is commented because the delete of data panel is a problem
	// @Test(expectedExceptions=InvalidPortObjectException.class)
	public final void testDataPanelItemClass() throws ClassNotFoundException, Throwable {
		ItItem deviceItem = createNewTestPortDataPanel("UNITTEST-PORT-PS-DataPanel-Test", 55); // Cabinet BK
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}


	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testCabinetItemClass() throws ClassNotFoundException, Throwable {
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-PORT-PS-Cabinet-Test", null);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(cabinetItem.getItemId(), cabinetItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(cabinetItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(cabinetItem.getItemId(), valueIdDTOList, getTestAdminUser());
		}
		catch (BusinessValidationException be) {
			be.printValidationErrors();
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				if (entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}

		}
		assertNotNull(componentDTOMap);
	}
	
	/* -------------------------------------------------------------------- */
	/* -- Add / Edit and Delete ports in unsupported item state ---- */
	/* -------------------------------------------------------------------- */
	
	/* item is already in archived state and then try to add more ports */
	@Test
	public final void testDeviceItemClassAddPortsInArchivedState() throws Throwable {
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.ARCHIVED, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.cannotAddPorts")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
	}
	
	/* create item and ports in planned state. Change item status to archived and edit ports comments */  
	@Test
	public final void testDeviceItemClassEditPortsInArchivedState() throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
		// Edit the ports 
		valueIdDTOList = editPPCommentsAndItemStatusWithSortOrder( deviceItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED );
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.cannotEditPorts")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
	}
	
	/* create item and ports in planned state. Change item status to archived and try to delete ports */
	@Test
	public final void testDeviceItemClassDeletePortsInArchivedState() throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
		// Delete 2 power ports 
		valueIdDTOList = deletePPAndEditItemStatusWithSortOrder( deviceItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED, 2 );

		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.cannotDeletePorts")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
	}

	/* -------------------------------------------------------------------- */
	/* -- Add / Edit and Delete ports in supported item state ------ */
	/* -------------------------------------------------------------------- */

	private void testDeviceItemClassAddPortsInSupportedState(long itemStatus) throws Throwable {
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", itemStatus, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}
	}
	
	/* item is already in installed state and then try to add more ports */
	@Test
	public final void testDeviceItemClassAddPortsInInstalledState() throws Throwable {
		testDeviceItemClassAddPortsInSupportedState(SystemLookup.ItemStatus.INSTALLED);
	}
	
	/* item is already in off-site state and then try to add more ports */
	// test commented becuase cannot move item from planned to off site
	// @Test
	public final void testDeviceItemClassAddPortsInOffSiteState() throws Throwable {
		testDeviceItemClassAddPortsInSupportedState(SystemLookup.ItemStatus.OFF_SITE);
	}

	/* item is already in powered off state and then try to add more ports */
	// test commented becuase cannot move item from planned to powered off
	// @Test
	public final void testDeviceItemClassAddPortsInPoweredOffState() throws Throwable {
		testDeviceItemClassAddPortsInSupportedState(SystemLookup.ItemStatus.POWERED_OFF);
	}

	/* item is already in storage state and then try to add more ports */
	@Test
	public final void testDeviceItemClassAddPortsInStorageState() throws Throwable {
		testDeviceItemClassAddPortsInSupportedState(SystemLookup.ItemStatus.STORAGE);
	}

	/* create item and ports in planned state. Change item status to requested state and edit ports comments */  
	private void testDeviceItemClassEditPortsInSupportedState(long itemStatus) throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
		// Edit the ports 
		valueIdDTOList = editPPCommentsAndItemStatusWithSortOrder( deviceItem.getItemId(), itemStatus );
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
	}

	/* create item and ports in planned state. Change item status to installed state and edit ports comments */
	@Test
	public final void testDeviceItemClassEditPortsInInstalledState() throws Throwable {
		testDeviceItemClassEditPortsInSupportedState(SystemLookup.ItemStatus.INSTALLED);
	}
	
	/* create item and ports in planned state. Change item status to off-site state and edit ports comments */
	// test commented becuase cannot move item from planned to off site
	// @Test
	public final void testDeviceItemClassEditPortsInOffSiteState() throws Throwable {
		testDeviceItemClassEditPortsInSupportedState(SystemLookup.ItemStatus.OFF_SITE);
	}

	/* create item and ports in planned state. Change item status to powered off state and edit ports comments */
	// test commented becuase cannot move item from planned to powered off
	// @Test
	public final void testDeviceItemClassEditPortsInPoweredOffState() throws Throwable {
		testDeviceItemClassEditPortsInSupportedState(SystemLookup.ItemStatus.POWERED_OFF);
	}

	/* create item and ports in planned state. Change item status to storage state and edit ports comments */
	@Test
	public final void testDeviceItemClassEditPortsInStorageState() throws Throwable {
		testDeviceItemClassEditPortsInSupportedState(SystemLookup.ItemStatus.STORAGE);
	}
	
	/* create item and ports in planned state. Change item status to archived and try to delete ports */
	private void testDeviceItemClassDeletePortsInSupportedState(long itemStatus) throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", itemStatus, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
		// Delete 2 power ports 
		valueIdDTOList = deletePPAndEditItemStatusWithSortOrder( deviceItem.getItemId(), SystemLookup.ItemStatus.ARCHIVED, 2 );

		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.cannotDeletePorts")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
	}

	/* create item and ports in planned state. Change item status from installed to archived and try to delete ports */
	public final void testDeviceItemClassDeletePortsInInstalledState() throws Throwable {
		testDeviceItemClassDeletePortsInSupportedState(SystemLookup.ItemStatus.INSTALLED);
	}

	/* create item and ports in planned state. Change item status to off site and try to delete ports */
	// test commented becuase cannot move item from planned to off site
	// @Test
	public final void testDeviceItemClassDeletePortsInOffSiteState() throws Throwable {
		testDeviceItemClassDeletePortsInSupportedState(SystemLookup.ItemStatus.OFF_SITE);
	}

	/* create item and ports in planned state. Change item status to powered off and try to delete ports */
	// test commented becuase cannot move item from planned to powered off
	// @Test
	public final void testDeviceItemClassDeletePortsInPoweredOffState() throws Throwable {
		testDeviceItemClassDeletePortsInSupportedState(SystemLookup.ItemStatus.POWERED_OFF);
	}

	/* create item and ports in planned state. Change item status from storage to archived and try to delete ports */
	public final void testDeviceItemClassDeletePortsInStorageState() throws Throwable {
		testDeviceItemClassDeletePortsInSupportedState(SystemLookup.ItemStatus.IN_STORAGE);
	}
	
	/* ----------------------------------------------------------------------------------------------------- */
	/* -- test editibility of power supply port attributes when connected and not connected ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	/* create item and ports in planned state. Create ports in connected state. Change port attributed that is allowed to change */  
	@Test
	public final void testDeviceItemClassEditPortsAttributeWhenPortConnected() throws Throwable {
		
		// Add a new item and ports (in connected state)
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrderAndConnected(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setItemStatus(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		}

		
		// set the power ports as used
		setPowerPortUsed(deviceItem.getItemId());
		
		// Edit port attributes that are allowed to be changed when connected 
		valueIdDTOList = editAllowedPPAttributedWhenConnected( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED );
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			Map<String, UiComponentDTO> item = itemHome.getItemDetails( deviceItem.getItemId(), null /*userInfo */ );
			assertNotNull( item );
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			// set the power ports as not used
			setPowerPortNotUsed(deviceItem.getItemId());

		}

		
	}
	

	/* create item and ports in planned state. Create ports in connected state. Change port attributed that is NOT allowed to change */  
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClassEditPortsConnectorWhenPortConnected() throws Throwable {
		
		// Add a new item and ports (in connected state)
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrderAndConnected(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// set the power ports as used
		setPowerPortUsed(deviceItem.getItemId());
		
		// Edit port attributes that are allowed to be changed when connected - connector 
		valueIdDTOList = editConnectorPPAttributedWhenConnected( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED );
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			// set the power ports as not used
			setPowerPortNotUsed(deviceItem.getItemId());

		}

		
	}


	/* create item and ports in planned state. Create ports in connected state. Change port attributed that is NOT allowed to change */  
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClassEditPortsPhaseWhenPortConnected() throws Throwable {
		
		// Add a new item and ports (in connected state)
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrderAndConnected(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// set the power ports as used
		setPowerPortUsed(deviceItem.getItemId());
		
		// Edit port attributes that are allowed to be changed when connected - connector 
		valueIdDTOList = editPhasePPAttributedWhenConnected( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED );
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			// set the power ports as not used
			setPowerPortNotUsed(deviceItem.getItemId());

		}

		
	}

	/* create item and ports in planned state. Create ports in connected state. Change port attributed that is NOT allowed to change */  
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClassEditPortsVoltWhenPortConnected() throws Throwable {
		
		// Add a new item and ports (in connected state)
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrderAndConnected(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// set the power ports as used
		setPowerPortUsed(deviceItem.getItemId());
		
		// Edit port attributes that are allowed to be changed when connected - connector 
		valueIdDTOList = editVoltPPAttributedWhenConnected( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED );
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			// set the power ports as not used
			setPowerPortNotUsed(deviceItem.getItemId());

		}

		
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                              port name unique                                                               ------ */
	/* ---------------------------------------------------------------------------------------------------- */	
	// Create a new item with duplicate port name
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortNameUnique() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPPWithDuplicateNames( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED );
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.duplicatePortName")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		

	}
	
	/* ----------------------------------------------------------------------------------------------------- */
	/* --                              redundancy test                                                                ------ */
	/* ---------------------------------------------------------------------------------------------------- */	
	
	// test redundancy string not start with N
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRedundancyWithNoN() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editItemRedundancy( deviceItem.getItemId(), "X + 2" );
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.invalidRedundancy")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// test redundancy having 'N + n' where n = # of ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRedundancyWithNoNexact() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editItemRedundancy( deviceItem.getItemId(), "N + 4" ); // model MODEL_ID has 4 power supply ports
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.invalidRedundancy")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// test redundancy having 'N + n' where n > # of ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRedundancyWithNoNmore() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editItemRedundancy( deviceItem.getItemId(), "N + 5" ); // model MODEL_ID has 4 power supply ports
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.invalidRedundancy")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}

	// positive test redundancy 
	@Test
	public final void testRedundancyWithNoNvalid() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit item to have 'N + 3' as the redundancy value
		valueIdDTOList = editItemRedundancy( deviceItem.getItemId(), "N + 3" ); // model MODEL_ID has 4 power supply ports
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       connector same for all ports test                                              ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// connector not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrConnectorDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editConnectorPPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// connector same for all ports - positive test case
	@Test
	public final void testPortCommonAttrConnectorSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editConnectorPPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       phase same for all ports test                                                    ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// phase not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrPhaseDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPhasePPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// phase same for all ports - positive test case
	@Test
	public final void testPortCommonAttrPhaseSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPhasePPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Volt same for all ports test                                                      ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// volt not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrVoltDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port  
		valueIdDTOList = editVoltPPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// volt same for all ports - positive test case
	@Test
	public final void testPortCommonAttrVoltSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editVoltPPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Watts Nameplate same for all ports test                                                      ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// watts nameplate not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrWattsNameplateDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editWattsNameplatePPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// watts nameplate same for all ports - positive test case
	@Test
	public final void testPortCommonAttrWattsNameplateSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editWattsNameplatePPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Watts Budget same for all ports test                                                      ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// watts budget not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrWattsBudgetDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editWattsBudgetPPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// watts budget same for all ports - positive test case
	@Test
	public final void testPortCommonAttrWattsBudgetSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editWattsBudgetPPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Power Factor same for all ports test                                         ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// power factor not same for all ports
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrPFDifferent() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedDifferent(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.commonAttributeVoilations")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
	}
	
	// power factor same for all ports - positive test case
	@Test
	public final void testPortCommonAttrPFSame() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedSame(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Power Factor numeric range test                                               ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// power factor -ve
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrPFNegative() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.incorrectFieldValue")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedNegative(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	// power factor zero - not a valid PF value
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrPFZero() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedZero(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	// power factor one
	@Test
	public final void testPortCommonAttrPFOne() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedOne(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	// power factor > 1
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortCommonAttrPFGTOne() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.incorrectFieldValue")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPFPPAttributedGTOne(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Delete Ports test                                                                    ------ */
	/* ---------------------------------------------------------------------------------------------------- */

	/* create item and ports in planned state. Delete 2 ports and save the item */
	@Test
	public final void testDeviceItemClassDeletePorts() throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Delete 2 power ports 
		valueIdDTOList = deletePPAndEditItemStatusWithSortOrder( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED, 2 );

		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		assertEquals ( getPowerPortCount(componentDTOMap), 2 ); // 2 power ports should be created
	}

	/* create item and ports in planned state. Set the ports as used. Delete 2 ports and save the item */
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClassDeletePortsWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}

		// set the power ports as used
		setPowerPortUsed(deviceItem.getItemId());
		
		// Delete 2 power ports 
		valueIdDTOList = deletePPAndEditItemStatusWithSortOrder( deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED, 2 );

		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.connectedPowerPortCannotDelete")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}
		finally {
			setPowerPortNotUsed(deviceItem.getItemId());
		}

	}


	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       required fields tests                                                               ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// port name not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldName() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names missing 
		valueIdDTOList = editPPNameMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// The Power port field value for 'Port Name' is invalid.
				if (entry.getKey().equals("PortValidator.powerIncorrectFieldValue") && entry.getValue().equals("The Power port field value for 'Port Name' is invalid.")) { 
					throw be;
				}
			}
		}
	}
	

	// port connector not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldConnector() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPPConnectorMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// The Power port field {1} is required.
				if (entry.getKey().equals("PortValidator.powerPortFieldRequired") && entry.getValue().equals("The Power port field 'Connector' is required.")) { 
					throw be;
				}
			}
		}
	}

	// port phase type not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldPhaseType() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit port names to be duplicates 
		valueIdDTOList = editPPPhaseTypeMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.powerPortFieldRequired") && entry.getValue().equals("The Power port field 'Phase' is required.")) { 
					throw be;
				}
			}
		}
	}

	// port phase type not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldVolt() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit volts to be missing 
		valueIdDTOList = editPPVoltMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.powerPortFieldRequired") && entry.getValue().equals("The Power port field 'Volts' is required.")) { 
					throw be;
				}
			}
		}
	}

	// port power factor not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldPowerFactor() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit volts to be missing 
		valueIdDTOList = editPPPFMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if ((entry.getKey().equals("PortValidator.powerIncorrectFieldValue") || entry.getKey().equals("PortValidator.commonAttributeVoilations")) && 
						(entry.getValue().equals("The Power port field value for 'Power Factor' is invalid.") || entry.getValue().equals("'Power Factor' is not the same for all Power ports"))) {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}

	// port watts budget not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldWattsBudget() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit watts budget to be missing 
		valueIdDTOList = editPPWattsBudgetMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if ((entry.getKey().equals("PortValidator.powerIncorrectFieldValue") || entry.getKey().equals("PortValidator.commonAttributeVoilations")) && 
						(entry.getValue().equals("The Power port field value for 'Watts(Budget)' is invalid.") || entry.getValue().equals("'Watts Budget' is not the same for all Power ports"))) {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}

	// port watts nameplate not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testPortRequiredFieldWattsNameplate() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit watts budget to be missing 
		valueIdDTOList = editPPWattsNameplateMissing(deviceItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ //The Power port field value for 'Power Factor' is invalid.
				if ((entry.getKey().equals("PortValidator.powerIncorrectFieldValue") || entry.getKey().equals("PortValidator.commonAttributeVoilations")) && 
						(entry.getValue().equals("The Power port field value for 'Watts(Nameplate)' is invalid.") || entry.getValue().equals("'Watts Nameplate' is not the same for all Power ports."))) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}


	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       sort order tests                                                                     ------ */
	/* ---------------------------------------------------------------------------------------------------- */

	// sort order is invalid (<= 0), server will correct it
	@Test
	public final void testPortSortOrderInvalid() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithInvalidSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithInvalidSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		List<PowerPortDTO> ppDtoList = getPP(componentDTOMap);
		List<Integer> sortOrderList = new ArrayList<Integer>();
		for (PowerPortDTO dto: ppDtoList) {
			if (sortOrderList.contains(dto.getSortOrder())) {
				Assert.fail("sort order not unique");
			}
			sortOrderList.add(dto.getSortOrder());
		}
	}

	
	// sort order is duplicate for a given port type, no error expected. Server will correct the duplicate sort order
	@Test
	public final void testPortSortOrderDuplicate() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithDuplicateSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithDuplicateSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		try {
			componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				// PortValidator.powerPortSortOrderNotUnique
				if (entry.getKey().equals("PortValidator.powerPortSortOrderNotUnique") && entry.getValue().equals("Power Port Index field is not unique.")) {
					throwEx = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwEx) {
				throw be;
			}
		}

		List<PowerPortDTO> ppDtoList = getPP(componentDTOMap);
		List<Integer> sortOrderList = new ArrayList<Integer>();
		for (PowerPortDTO dto: ppDtoList) {
			if (sortOrderList.contains(dto.getSortOrder())) {
				Assert.fail("sort order not unique");
			}
			sortOrderList.add(dto.getSortOrder());
		}
	}


	
	// + => sort order is correct
	@Test
	public final void testPortSortOrderValid() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		List<PowerPortDTO> ppDtoList = getPP(componentDTOMap);
		List<Integer> sortOrderList = new ArrayList<Integer>();
		for (PowerPortDTO dto: ppDtoList) {
			if (sortOrderList.contains(dto.getSortOrder())) {
				Assert.fail("sort order not unique");
			}
			sortOrderList.add(dto.getSortOrder());
		}
	}

	@Test
	public final void testPortDuplicateValidSortOrder() throws Throwable {
		// Add a new item and ports 
		ItItem deviceItem = createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), deviceItem.getModel().getModelDetailId().longValue());
		long modelId = MODEL_ID;
		valueIdDTOList = addPowerPortsWithSortOrder(deviceItem.getItemId(), modelId);
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		componentDTOMap = itemHome.saveItem(deviceItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		List<PowerPortDTO> ppDtoList = getPP(componentDTOMap);
		List<Integer> sortOrderList = new ArrayList<Integer>();
		for (PowerPortDTO dto: ppDtoList) {
			if (sortOrderList.contains(dto.getSortOrder())) {
				Assert.fail("sort order not unique");
			}
			sortOrderList.add(dto.getSortOrder());
		}
	}
	
	// API test
	@Test
	public final void testPortCreateUsingAPI() throws Throwable {
		
		// PowerPortDTO dto = createPPdto(4964L, -1L, "Power Supply", "PS3", 2, "Black", "BS1363", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API");
		
		PowerPortDTO dto = createPPdto(4964L, -1L, "Power Supply", "PS9", 8, "Yellow", "IEC-320-C13", "Three Phase Delta", "208", 0.69, 222, 122, "added via the API again", "N+7");
		
		// PowerPortDTO dto = createPPdto(4714L, -1L, "Power Supply", "PS3", 2, "Black", "AU1-10P", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API");

		try {
			itemHome.createItemPowerPortExtAPI(dto, getTestAdminUser());
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				System.out.println("Exception: " + entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}

		
	}
	
	@Test
	public final void testPortUpdateUsingAPI() throws Throwable {
		
		// PowerPortDTO dto = createPPdto(4964L, -1L, "Power Supply", "PS3", 2, "Black", "BS1363", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API");
		
		PowerPortDTO dto = createPPdto(4964L, 55477L, "Power Supply", "PS9", 8, "Green", "IEC-320-C14", "Three Phase Wye", "380", 0.73, 221, 121, "updated via the API again", "N+7");
		
		// PowerPortDTO dto = createPPdto(4714L, -1L, "Power Supply", "PS3", 2, "Black", "AU1-10P", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API");

		try {
			itemHome.updateItemPowerPortExtAPI(dto, getTestAdminUser()); 
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				System.out.println("Exception: " + entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}

		
	}

	@Test
	public final void testPortDeleteUsingAPI() throws Throwable {
		
		// PowerPortDTO dto = createPPdto(4964L, -1L, "Power Supply", "PS3", 2, "Black", "BS1363", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API");
		
		// PowerPortDTO dto = createPPdto(4964L, -1L, "Power Supply", "PS9", 8, "Green", "IEC-320-C14", "Three Phase Wye", "380", 0.73, 221, 121, "updated via the API again", "N+7");
		
		PowerPortDTO dto = createPPdto(4964L, 55476L, "Power Supply", "PS3", 2, "Black", "AU1-10P", "Single Phase (3-Wire)", "120", 1, 1000, 800, "added via the API", "N+6");

		try {
			itemHome.deleteItemPowerPortExtAPI(dto, getTestAdminUser()); 
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{ 
				System.out.println("Exception: " + entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}

		
	}

	
	private PowerPortDTO createPPdto(Long itemId, Long portId, String portType, String newPortName, int index, String colorCode, String connectorName, 
			String phaseLksDesc, String voltsLksDesc, double powerFactor, long wattsNameplate, long wattsBudget, 
			String comments, String psRedundancy) {
		PowerPortDTO dto = new PowerPortDTO();
		
		dto.setItemId(itemId);
		dto.setPortId(portId);
		dto.setPortSubClassLksDesc(portType);
		dto.setPortName(newPortName);
		dto.setSortOrder(index);
		dto.setColorLkuDesc(colorCode);
		dto.setConnectorName(connectorName);
		dto.setPhaseLksDesc(phaseLksDesc);
		dto.setVoltsLksDesc(voltsLksDesc);
		dto.setPowerFactor(powerFactor);
		dto.setWattsNameplate(wattsNameplate);
		dto.setWattsBudget(wattsBudget);
		dto.setComments(comments);
		dto.setPsRedundancy(psRedundancy);
		
		return dto;
	}
	
	@Test
	public final void testDeleteLocation() {
		
		locationDAO.deleteLocationAndItems(-1L, (String) "admin");
		
		locationDAO.deleteLocationAndItems(-1L, (String) "");
		
		locationDAO.deleteLocationAndItems(-1L, (String) null);
		
	}
	
	@Test
	public final void testLocUnmap() {
		
		// locationDAO.unmapLocationWithPIQ(-1L);
		
	}

}
