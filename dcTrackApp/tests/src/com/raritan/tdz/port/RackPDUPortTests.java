package com.raritan.tdz.port;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class RackPDUPortTests extends PowerPortTests {

	static long MODEL_ID = 1974; // rack pdu ports: 1 input cord and 2 output cords
	
	/* -------------------------------------------------------------------- */
	/* ---------------- Applicable Item Class tests -------------------- */
	/* -------------------------------------------------------------------- */

	@Test
	public final void rpduTestRPDUItemClass() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeviceItemClass() throws Throwable {
		// Item devItem = (Item)createNewTestDevice("UNITTEST-PORT-DEV-Test", null);
		Item devItem = (Item)createNewTestPortDevice("UNITTEST-PORT-PS-Dev-Test", SystemLookup.ItemStatus.PLANNED, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(devItem.getItemId(), MODEL_ID);

		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(devItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getValidationErrorsMap();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.powerPortUnsupportedClass") && 
						!entry.getKey().equals("PortValidator.inputCordPortUnsupportedItemClass") && 
						!entry.getKey().equals("PortValidator.rackPDUOutputUnsupportedClass") &&
						!entry.getKey().equals("PortValidator.inputCordUnsupportedClass") &&
						!entry.getKey().equals("PortValidator.PortUnsupportedClass")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestNWItemClass() throws Throwable {
		Item devItem = (Item)createNewTestPortNetworkBlade("UNITTEST-PORT-NW-Test", 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(devItem.getItemId(), MODEL_ID);

		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(devItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getValidationErrorsMap();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestCabinetItemClass() throws Throwable {
		Item devItem = (Item)createNewTestCabinet("UNITTEST-PORT-NW-Test", null);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(devItem.getItemId(), MODEL_ID);
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(devItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getValidationErrorsMap();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestCRACItemClass() throws Throwable {
		Item devItem = (Item)createNewTestCRAC("UNITTEST-PORT-NW-Test");
		
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(devItem.getItemId(), MODEL_ID);
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(devItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getValidationErrorsMap();
			boolean throwEx = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.powerPortUnsupportedClass")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					throwEx = true;
				}
			}
			if (throwEx) {
				throw be;
			}
		}
	}

	// TODO:: Add more tests for different class of item
	
	/* -------------------------------------------------------------------- */
	/* ---- test invalid volts during creation of new power ports ---  */
	/* -------------------------------------------------------------------- */
	
	/* create a new RPDU with no port. 
	 * try to add power ports that has invalid voltage value. This will throw warning to the user to continue to create item with no power ports 
	 * add skip validation and try to save the item again. It should be created with no power ports */
	@Test
	public final void testRPDUItemClassInvalidVolt() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), 1447); // 1447 has invalid voltage value '120 ~ 240'
		
		// set the correct sort order for power ports
		correctSortOrder(valueIdDTOList, SystemLookup.ItemStatus.PLANNED);
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String,BusinessValidationException.Warning> warningMap = be.getValidationWarningsMap();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				if (!entry.getKey().equals("ItemValidator.invalidPowerPortVolt")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					// set the skip validator and save again ...
					editItemSkipValidation(rpduItem.getItemId(), true);
					Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
					assertNotNull(componentDTOMap);
					assertEquals ( getPowerPortCount(componentDTOMap), 0 ); // no power ports should be created
				}
			}
		}
	}
	
	
	
	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Delete Ports test                                                                    ------ */
	/* ---------------------------------------------------------------------------------------------------- */

	/* test delete input cord and all output */
	/* create item and ports in planned state. Delete 2 ports and save the item */
	@Test
	public final void testRPDUDeviceItemClassDeletePorts() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Input cord and all the corresponding output cords
		valueIdDTOList = deletePPInputCordAndAllOutputWithSortOrder( rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED );

		componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		assertEquals ( getPowerPortCount(componentDTOMap), 0 ); // all power ports must be deleted
	}

	/* create item and ports in planned state. Set the ports as used. Delete 2 ports and save the item */
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRPDUDeviceItemClassDeleteInputPortsNotAllOutput() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
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
		valueIdDTOList = deletePPInputCordAndNotAllOutputWithSortOrder( rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED );

		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (!entry.getKey().equals("PortValidator.inletDeletion")) { 
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			throw be;
		}

	}


	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Port Name tests: unique, required and length <= 64                       ------ */
	/* ---------------------------------------------------------------------------------------------------- */
	
	// port name not provided
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRPDUPortRequiredFieldName() throws Throwable {
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
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
		valueIdDTOList = editPPNameMissing(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// PortValidator.powerPortFieldRequired=The Power port field 'Port Name' is required.
				if (entry.getKey().equals("PortValidator.powerPortFieldRequired") && entry.getValue().equals("The Power port field 'Port Name' is required.")) { 
					throw be;
				}
			}
		}
	}


	// port name length invalid > 64
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRPDUPortNameLengthFieldInvalid() throws Throwable {
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
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
		valueIdDTOList = editPPNameLengthInvalid(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// The Power port field value for 'Port Name' is invalid.
				if (entry.getKey().equals("PortValidator.powerPortNameLength")) { 
					throw be;
				}
			}
		}
	}

	
	// port name length invalid > 64
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testRPDUPortNameFieldNotUnique() throws Throwable {
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
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
		valueIdDTOList = editPPNameNotUnique(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// The Power port field value for 'Port Name' is invalid.
				if (entry.getKey().equals("PortValidator.duplicatePortName")) { 
					throw be;
				}
			}
		}
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Index tests: non-unique, invalid ------ */
	/* ---------------------------------------------------------------------------------------------------- */

	// invalid sort order is corrected in the server
	@Test
	public final void rpduTestInvalidSortOrder() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithInvalidSortOrder(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getValidationWarningsMap();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
	}

	// duplicate sort order is not an error for rack pdus
	@Test
	public final void rpduTestDuplicateSortOrder() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addPowerPortsWithDuplicateSortOrder(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getValidationWarningsMap();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
	}
	
	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Comment tests: length test 													------ */
	/* ---------------------------------------------------------------------------------------------------- */	

	// 
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestCommentMaxLength() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		// Edit comments with length > 500
		valueIdDTOList = editPPCommentsMoreThanMaxLength(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				// The Power port field value for 'Port Name' is invalid.
				if (entry.getKey().equals("PortValidator.powerPortCommentLength")) { 
					throw be;
				}
			}
		}
		
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Port edit when connected test 												------ */
	/* ---------------------------------------------------------------------------------------------------- */	

	// setPowerPortUsed(deviceItem.getItemId());
	@Test
	public final void rpduTestEditNameWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		valueIdDTOList = editPPName(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	@Test
	public final void rpduTestEditIndexWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editPPIndex(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	@Test
	public final void rpduTestEditColorCodeWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editPPColorCode(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	@Test
	public final void rpduTestEditCommentsWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editPPCommentsAndItemStatusWithSortOrder(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			throw be;
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestEditPhaseTypeWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editPhasePPAttributed(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestEditAmpsRWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editAmpsRPPAttributed(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}
		
	}

	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestEditLegsWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editLegsPPAttributed(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void rpduTestEditVoltsWhenConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editVoltPPAttributedDifferent(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}

	/* ----------------------------------------------------------------------------------------------------- */
	/* --                       Port test common attributes 												------ */
	/* ---------------------------------------------------------------------------------------------------- */	

	@Test
	public final void rpduTestEditVoltsDifferentWhenNotConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		// setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editVoltPPAttributedDifferent(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		finally {
			// setPowerPortNotUsed(rpduItem.getItemId());
		}

		
	}
	

	@Test
	public final void rpduTestEditPhaseDifferentWhenNotConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		// setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editPhasePPAttributedDifferent(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
	}

	@Test
	public final void rpduTestEditConnectorDifferentWhenNotConnected() throws Throwable {
		
		// Add a new item and ports 
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		List<ValueIdDTO> valueIdDTOList = addPowerPorts(rpduItem.getItemId(), rpduItem.getModel().getModelDetailId().longValue());
		
		Map<String,UiComponentDTO> componentDTOMap = null;
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
		
		// setPowerPortUsed(rpduItem.getItemId());
		
		// Edit comments with length > 500
		valueIdDTOList = editConnectorPPAttributedDifferent(rpduItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String, String> errorMap = be.getErrors();
			for (Map.Entry<String, String> entry : errorMap.entrySet())
			{
				if (entry.getKey().equals("PortValidator.connectedPowerPortCannotEdit") && 
						entry.getValue().equals("Power Port is connected. Only the Name, Index, Color, Power Factor, Watts Nameplate, Watts Budget and  and Comments fields may be edited.")) { 
					throw be;
				}
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
		}
		
	}

}
