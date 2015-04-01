package com.raritan.tdz.port;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

public class DataPortTests extends PortTests {

	static long MODEL_ID = 90; // 6 data ports
	
	private List<ValueIdDTO> addDataPorts(long itemId, long modelId) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		List<DataPortDTO> dataPortDTOListCopy = new ArrayList<DataPortDTO>();
		dataPortDTOListCopy.addAll(dataPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		List<DataPortDTO> dpModelDTOs = modelHome.getAllDataPort(modelId);
		for (DataPortDTO dpdto: dpModelDTOs) {
			dpdto.setItemId(itemId);
			dpdto.setPortName(dpdto.getPortName() + date.toString() + "-");
			dataPortDTOListCopy.add(dpdto);
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dataPortDTOListCopy);
		return valueIdDTOList;
	}

	private List<ValueIdDTO> addDataPortsWithOrderNumberMultipleTimes(long itemId, long modelId, long numOfTimes) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		List<DataPortDTO> dataPortDTOListCopy = new ArrayList<DataPortDTO>();
		dataPortDTOListCopy.addAll(dataPortDTOList);

		String namePostFix = new String("-");
		// List<DataPortDTO> dpModelDTOs = modelHome.getAllDataPort(modelId);
		int sortOrder = 1;
		for (int i = 0; i < numOfTimes; i++) {
			List<DataPortDTO> dpModelDTOs = modelHome.getAllDataPort(modelId);
			namePostFix = namePostFix + "-"; 
			for (DataPortDTO dpdto: dpModelDTOs) {
				dpdto.setItemId(itemId);
				dpdto.setPortName(dpdto.getPortName() + namePostFix);
				dpdto.setSortOrder(sortOrder++);
				dataPortDTOListCopy.add(dpdto);
			}
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dataPortDTOListCopy);
		return valueIdDTOList;
	}

	
	private List<ValueIdDTO> addDataPortsWithOrderNumber(long itemId, long modelId) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		List<DataPortDTO> dataPortDTOListCopy = new ArrayList<DataPortDTO>();
		dataPortDTOListCopy.addAll(dataPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		List<DataPortDTO> dpModelDTOs = modelHome.getAllDataPort(modelId);
		int sortOrder = 1;
		for (DataPortDTO dpdto: dpModelDTOs) {
			dpdto.setItemId(itemId);
			dpdto.setPortName(dpdto.getPortName() + date.toString() + "-");
			dpdto.setSortOrder(sortOrder++);
			dataPortDTOListCopy.add(dpdto);
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dataPortDTOListCopy);
		return valueIdDTOList;
	}

	private List<ValueIdDTO> addDataPortsWithDuplicateOrderNumber(long itemId, long modelId) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		List<DataPortDTO> dataPortDTOListCopy = new ArrayList<DataPortDTO>();
		dataPortDTOListCopy.addAll(dataPortDTOList);

		// add power ports from the model's library
		java.util.Date date = new java.util.Date();
		List<DataPortDTO> dpModelDTOs = modelHome.getAllDataPort(modelId);
		int sortOrder = 1;
		int numOfDuplicates = 2;
		int portNum = 0;
		for (DataPortDTO dpdto: dpModelDTOs) {
			portNum++;
			dpdto.setItemId(itemId);
			dpdto.setPortName(dpdto.getPortName() + date.toString() + "-");
			dpdto.setSortOrder(sortOrder);
			dataPortDTOListCopy.add(dpdto);
			if ((portNum % numOfDuplicates) == 0) {
				sortOrder++;
			}
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dataPortDTOListCopy);
		return valueIdDTOList;
	}

	private List<ValueIdDTO> prepareItemDataPortValueIdDTOList(Map<String, UiComponentDTO> item, List<DataPortDTO> dataPortDTOListCopy) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
	    @SuppressWarnings("rawtypes")
		Iterator it = item.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue();
			assertNotNull( field );
			/* these have no object trace and is not expected by the server */
			if (((String)pairs.getKey()).equals("tiRailWidth") || 
					((String)pairs.getKey()).equals("tiRearClearance") ||
					((String)pairs.getKey()).equals("tiFrontRailOffset") ||
					((String)pairs.getKey()).equals("tiRearRailOffset") || 
					((String)pairs.getKey()).equals("tiLeftClearance") || 
					((String)pairs.getKey()).equals("tiRightClearance") || 
					((String)pairs.getKey()).equals("tiCustomField") ||
					((String)pairs.getKey()).equals("tiRearDoorPerforation") ||
					((String)pairs.getKey()).equals("tiFrontDoorPerforation") ||
					((String)pairs.getKey()).equals("cmbRowLabel") ||
					((String)pairs.getKey()).equals("tiLoadCapacity") ||
					((String)pairs.getKey()).equals("cmbCabinetGrouping") ||
					((String)pairs.getKey()).equals("cmbRowPosition") ||
					((String)pairs.getKey()).equals("tiFrontClearance") || 
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			/*else if (((String)pairs.getKey()).equals("tabPowerPorts")) {
				dto.setData(dataPortDTOListCopy);
			}*/
			else if (((String)pairs.getKey()).equals("tabDataPorts")) {
				dto.setData(dataPortDTOListCopy);
			}
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
	    return valueIdDTOList;
	}
	
	private List<DataPortDTO> getDP(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		return dataPortDTOList;
	}
	

	
	private List<DataPortDTO> editDPConnInvalid(Map<String, UiComponentDTO> item, int numOfConn) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			if (0 == numOfConn--) {
				break;
			}
			dto.setConnectorLkuId(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editFewConnDPInvalid( long itemId, int numOfInvalidConn ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// edit PP Connector type
		List<DataPortDTO> dpDtoList = editDPConnInvalid(item, numOfInvalidConn);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dpDtoList);
		return valueIdDTOList;
	}

	private List<ValueIdDTO> getDtoList( long itemId ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);

		// edit PP Connector type
		List<DataPortDTO> dpDtoList = getDP(item);

		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dpDtoList);
		return valueIdDTOList;
	}

	private void editItemSkipValidation(Map<String, UiComponentDTO> item, boolean skipValidation) {
		UiComponentDTO itemSkipValidationField = item.get("_tiSkipValidation");
		assertNotNull(itemSkipValidationField);
		itemSkipValidationField.getUiValueIdField().setValueId(skipValidation);
		itemSkipValidationField.getUiValueIdField().setValue(skipValidation);
	}

	private List<ValueIdDTO> editItemSkipValidation( long itemId, boolean skipValidation ) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemSkipValidation(item, skipValidation);
		
		// edit PP Connector type
		List<DataPortDTO> dpDtoList = getDP(item);
		
		// prepare the new value id dto list for the item
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dpDtoList);
		return valueIdDTOList;
	}
	
	private long getDataPortCount( Map<String, UiComponentDTO> item ) {
		// get the data ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		if (null == itemDataPortsField) {
			return 0;
		}
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		return dataPortDTOList.size();
	}

	private void setDataPortUsed(Long itemId) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);
		Set<DataPort> dps = item.getDataPorts();
		for (DataPort dp: dps) {
			dp.setUsed(true);
		}
		session.update(item);
		session.flush();
	}
	
	private void setDataPortNotUsed(Long itemId) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);
		Set<DataPort> dps = item.getDataPorts();
		for (DataPort dp: dps) {
			dp.setUsed(false);
		}
		session.update(item);
		session.flush();
	}
	
	/*private void setItemStatus(Long itemId, long status) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);

		item.setStatusLookup( SystemLookup.getLksData(session, status) );
	}*/

	private Map<String, UiComponentDTO> getItemMap(long itemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		return item;
	}
	
	private void saveItem(Long itemId, List<ValueIdDTO> valueIdDTOList) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		Map<String,UiComponentDTO> componentDTOMap = null;
		componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		
		// get the item for more validation while getting the saved item and ports
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );		
	}
	
	private Long createItemAndPortsConnected() throws Throwable {

		// create the item and ports
		Long itemId = createItemAndPorts();
		
		// set connected as true
		setDataPortUsed(itemId);
		
		return itemId;
	}
	
	private Long createItemAndPorts() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", SystemLookup.ItemStatus.PLANNED, 55);
		Long itemId = rpduItem.getItemId();
		
		// add data ports and save the item
		addPhysicalDataPorts(itemId);
		
		return itemId;
	}

	private Long createItemAndPortsInItemState(String itemName, long itemState) throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU(itemName, itemState, 55);
		Long itemId = rpduItem.getItemId();
		
		// add data ports and save the item
		addPhysicalDataPorts(itemId);
		
		return itemId;
	}

	
	@SuppressWarnings("unused")
	private void addLogicalDataPorts(Long itemId) throws Throwable {
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1020L);
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}
	
	@SuppressWarnings("unused")
	private void addOneLogicalDataPort(Long itemId) throws Throwable {
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 4631L);
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}


	/* -------------------------------------------------------------------- */
	/* ---------------- Applicable Item Class tests -------------------- */
	/* -------------------------------------------------------------------- */

	private void addPhysicalDataPorts(Long itemId) throws Throwable {
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 90L);
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}
	
	
	// Rack PDU 
	@Test
	public final void testDataPortSupportedClassRPDU() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		addPhysicalDataPorts(rpduItem.getItemId());
	}

	// Device - Chassis
	@Test
	public final void testDataPortSupportedClassDevChassis() throws Throwable {
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-PORT-DEV_CHASSIS-Test", null, 55);
		addPhysicalDataPorts(devChassisItem.getItemId());
	}
	
	// Device - Blade
	@Test
	public final void testDataPortSupportedClassDevBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortDeviceBlade("UNITTEST-PORT-DEV_BLADE-Test", 55, null);
		addPhysicalDataPorts(devBladeItem.getItemId());
	}
	
	// Device - Free standing
	@Test
	public final void testDataPortSupportedClassDevFS() throws Throwable {
		ItItem fsDevItem = createNewTestDeviceFS("UNITTEST-PORT-DEV_FS-Test");
		addPhysicalDataPorts(fsDevItem.getItemId());
	}
	
	
	// VM
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortSupportedClassVM() throws Throwable {
		ItItem vmItem = createNewTestDeviceVM("UNITTEST-PORT-DEV_VM-Test");
		Long itemId = vmItem.getItemId();
		
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 90L);
		// addDataPortsForUnsupportedItemClass(vmItem.getItemId());
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortUnsupportedClass");
		expectedErrorCodes.add("PortValidator.physicalUnsupportedClass");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}
	
	// Network //
	
	// Network Chassis
	// ItItem createNewTestNetworkChassis(String itemName)
	@Test
	public final void testDataPortSupportedClassNWChassis() throws Throwable {
		ItItem nwChassisItem = createNewTestPortNetworkChassis("UNITTEST-PORT-NW_CHASSIS-Test", 55);
		addPhysicalDataPorts(nwChassisItem.getItemId());
	}
	
	// Network Blade
	@Test
	public final void testDataPortSupportedClassNWBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortNetworkBlade("UNITTEST-PORT-NW_BLADE-Test", 55);
		addPhysicalDataPorts(devBladeItem.getItemId());
	}
	
	// Network Stack
	@Test
	public final void testDataPortSupportedClassNWStack() throws Throwable {
		ItItem nwStackItem = createNewTestNetworkStack("UNITTEST-PORT-NW_STACK-Test", "StackName");
		addPhysicalDataPorts(nwStackItem.getItemId());
	}
	
	// UPS
	@Test
	public final void testDataPortSupportedClassUPS() throws Throwable {
		MeItem upsItem = createNewTestUPS("UNITTEST-PORT-UPS-Test");
		addPhysicalDataPorts(upsItem.getItemId());
	}

	
	// Floor PDU
	@Test
	public final void testDataPortSupportedClassFloorPDU() throws Throwable {
		MeItem upsItem = createNewTestFloorPDU("UNITTEST-PORT-FloorPDU-Test");
		addPhysicalDataPorts(upsItem.getItemId());
	}
	
	// CRAC
	@Test
	public final void testDataPortSupportedClassCRAC() throws Throwable {
		Item cracItem = createNewTestCRAC("UNITTEST-PORT-CRAC-Test");
		addPhysicalDataPorts(cracItem.getItemId());
	}
	
	// Probe
	// ItItem (String itemName, long cabinetItemId)
	@Test
	public final void testDataPortSupportedClassProbe() throws Throwable {
		ItItem probeItem = createNewPortTestProbe("UNITTEST-PORT-PROBE-Test", 55);
		addPhysicalDataPorts(probeItem.getItemId());
	}
	
	
	/* -------------------------------------------------------------------- */
	/* ---------------- Create ports in different item states -------- */
	/* -------------------------------------------------------------------- */

	// Planned
	@Test
	public final void testDataPortAddInPlannedState() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", SystemLookup.ItemStatus.PLANNED, 55);
		
		// add data ports in planned state
		addPhysicalDataPorts(rpduItem.getItemId());
	}
	
	// Storage
	@Test
	public final void testDataPortAddInStorageState() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", SystemLookup.ItemStatus.STORAGE, 55);
		
		// add data ports in planned state
		addPhysicalDataPorts(rpduItem.getItemId());
		
		// allow item delete: cannot delete item in archived state
		rpduItem.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );

	}

	
	// Installed
	@Test
	public final void testDataPortAddInInstalledState() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", SystemLookup.ItemStatus.INSTALLED, 55);
		
		// add data ports in planned state
		addPhysicalDataPorts(rpduItem.getItemId());
		
		// allow item delete: cannot delete item in archived state
		rpduItem.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );

	}

	
	// Archived
	@Test
	public final void testDataPortAddInArchivedState() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", SystemLookup.ItemStatus.ARCHIVED, 55);
		
		// add data ports in planned state
		addPhysicalDataPorts(rpduItem.getItemId());
		
		// allow item delete: cannot delete item in archived state
		rpduItem.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
	}

	// test data port edit in different states
	@Test
	public final void testDataPortEditInDifferentItemStates() throws Throwable {
		
		List<Long> itemStates = new ArrayList<Long>();
		itemStates.add(SystemLookup.ItemStatus.PLANNED);
		itemStates.add(SystemLookup.ItemStatus.ARCHIVED);
		itemStates.add(SystemLookup.ItemStatus.IN_STORAGE);
		itemStates.add(SystemLookup.ItemStatus.INSTALLED);
		// itemStates.add(SystemLookup.ItemStatus.OFF_SITE);
		// itemStates.add(SystemLookup.ItemStatus.POWERED_OFF);
		// itemStates.add(SystemLookup.ItemStatus.STORAGE);
		
		
		
		for (Long itemState: itemStates) {
			String itemName = "UNITTEST-TEST-EDIT-IN-DIFF-ITEM-STATE" + itemState.toString();
			
			Long itemId = createItemAndPortsInItemState(itemName, itemState);
	
			// edit port name and save the item
			List<ValueIdDTO> valueIdDTOList = editPortCommStr(itemId, "changedCommunityString");
			
			// save the item
			saveItem(itemId, valueIdDTOList);
			
			setItemStatus(itemId, SystemLookup.ItemStatus.PLANNED);
		}
		
	}
	

	
	// test data port delete in different states
	// delete port when connected
	@Test
	public final void testDataPortDeleteInDifferentItemStates() throws Throwable {
		List<Long> itemStates = new ArrayList<Long>();
		itemStates.add(SystemLookup.ItemStatus.PLANNED);
		itemStates.add(SystemLookup.ItemStatus.ARCHIVED);
		itemStates.add(SystemLookup.ItemStatus.IN_STORAGE);
		itemStates.add(SystemLookup.ItemStatus.INSTALLED);
		// itemStates.add(SystemLookup.ItemStatus.OFF_SITE);
		// itemStates.add(SystemLookup.ItemStatus.POWERED_OFF);
		// itemStates.add(SystemLookup.ItemStatus.STORAGE);
		
		for (Long itemState: itemStates) {

			String itemName = "UNITTEST-TEST-DELETE-IN-DIFF-ITEM-STATE" + itemState.toString();
				
			Long itemId = createItemAndPortsInItemState(itemName, itemState);
	
			// delete a couple of ports
			List<ValueIdDTO> valueIdDTOList = deletePort(itemId, 2);
			
			saveItem(itemId, valueIdDTOList);
			
			// allow item delete: cannot delete item in archived state
			setItemStatus(itemId, SystemLookup.ItemStatus.PLANNED);
			
		}
		
	}


	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test field edit when port connected------ */
	/* -------------------------------------------------------------------- */
	

	private List<DataPortDTO> editDPName(Map<String, UiComponentDTO> item, String appendPortName) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setPortName(dto.getPortName() + appendPortName);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortName(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPName(item, "-");
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	// port name - can edit
	@Test
	public final void testDataPortPortNameEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortName(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	
	// index - can edit
	
	private List<DataPortDTO> editDPIndex(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setSortOrder(dto.getSortOrder() + 1);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortIndex(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPIndex(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortIndexEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortIndex(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// color code - can edit

	private static LkuData getLkuData(long lkuId) {
		Session session = sf.getCurrentSession();
		Criteria c = session.createCriteria(LkuData.class);
		c.add(Restrictions.eq("lkuId", lkuId));
		return ((LkuData)c.uniqueResult());
	}
	
	private List<DataPortDTO> editDPColorCode(Map<String, UiComponentDTO> item, long colorCodeLkuId) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			LkuData lkuColorData = getLkuData(colorCodeLkuId);  // Black
			dto.setColorLkuId(lkuColorData.getLkuId());
			dto.setColorLkuDesc(lkuColorData.getLkuValue());
			dto.setColorNumber(lkuColorData.getLkuAttribute());

		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortColorCode(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPColorCode(item, 941L);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortColorCodeEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortColorCode(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}

	
	// grouping / vlan - can edit
	
	
	private List<DataPortDTO> editDPGroupingVLAN(Map<String, UiComponentDTO> item, Long vlanLkuId) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setVlanLkuId(vlanLkuId);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortGroupingVLAN(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPGroupingVLAN(item, 924L); // Cluster
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortGroupingVLANEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortGroupingVLAN(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// mac address - can edit
	
	private List<DataPortDTO> editDPMacAddress(Map<String, UiComponentDTO> item, String macAddress) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setMacAddress(macAddress);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortMacAddress(long itemId, String macAddress) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPMacAddress(item, macAddress); // Cluster
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortMacAddressEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortMacAddress(itemId, "changedMacAddress");
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// ip address - can edit
	
	private List<DataPortDTO> editDPIPAddress(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setIpAddress("changedIPAddress");
			dto.setIpv6Address("changedIPv6Address");
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortIPAddress(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPIPAddress(item); // Cluster
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortIPAddressEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortIPAddress(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// snmp community string - can edit
	
	private List<DataPortDTO> editDPCommStr(Map<String, UiComponentDTO> item, String communityStr) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setCommunityString(communityStr);
			break; // set the community string for only one port
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortCommStr(long itemId, String communityStr) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPCommStr(item, communityStr);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortCommStrEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortCommStr(itemId, "changedCommunityString");
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// comment - can edit
	
	private List<DataPortDTO> editDPComment(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setComments("changed comments");
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortComment(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPComment(item); // Cluster
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortCommentEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortComment(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}
	

	
	// connector - cannot edit

	private List<DataPortDTO> editDPConnector(Map<String, UiComponentDTO> item, Long connectorLkuId) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setConnectorLkuId(connectorLkuId);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortConnector(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPConnector(item, 126L);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	// PortValidator.connectedDataPortCannotEdit
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortConnectorEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortConnector(itemId);
		
		// save the item
		// saveItemCannotSaveWhenConnected(itemId, valueIdDTOList);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.connectedDataPortCannotEdit");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}

	}


	// media  - cannot edit
	
	private List<DataPortDTO> editDPMedia(Map<String, UiComponentDTO> item, Long mediaLksValueCode) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setMediaLksValueCode(mediaLksValueCode);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortMedia(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPMedia(item, 7063L); // Twisted Pair
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	// PortValidator.connectedDataPortCannotEdit
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortMediaEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortMedia(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.connectedDataPortCannotEdit");
		try {
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}

	}


	
	// protocol   - cannot edit
	
	private List<DataPortDTO> editDPProtocol(Map<String, UiComponentDTO> item, Long protocolLkuId) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setProtocolLkuId(protocolLkuId);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortProtocol(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPProtocol(item, 1045L); // HSSI
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	// PortValidator.connectedDataPortCannotEdit
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortProtocolEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortProtocol(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.connectedDataPortCannotEdit");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}

	}


	
	// Data Rate   - cannot edit
	
	private List<DataPortDTO> editDPSpeed(Map<String, UiComponentDTO> item, Long speedLkuId) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setSpeedLkuId(speedLkuId);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortSpeed(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPSpeed(item, 1045L); // HSSI
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	// PortValidator.connectedDataPortCannotEdit
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortSpeedEditWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortSpeed(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.connectedDataPortCannotEdit");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}


	}


	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test required field ----------------------------- */
	/* -------------------------------------------------------------------- */
	
	
	// port name - required
	private List<DataPortDTO> editDPNameNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		int count = 1;
		for (DataPortDTO dto: dataPortDTOList) {
			if (0 == (count % 2)) {
				dto.setPortName(null);
			}
			else {
				dto.setPortName(new String());
			}
			count++;
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortNameNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPNameNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortPortNameAsRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortNameNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		expectedErrorCodes.add("PortValidator.duplicatePortName");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}


	}


	// port type -required
	
	private List<DataPortDTO> editDPPortTypeNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setPortSubClassLksValueCode(null);
			dto.setPortSubClassLksDesc(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortTypeNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPPortTypeNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortTypeAsRequired() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortTypeNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}

	
	// index -required
	
	private List<DataPortDTO> editDPPortIndexNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setSortOrder(-2);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortIndexNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPPortIndexNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test
	public final void testDataPortIndexAsRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortIndexNotProvided(itemId);
		
		// save the item - server will correct the index
		saveItem(itemId, valueIdDTOList);

		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);

	}


		
	// color code - not required
	
	private List<DataPortDTO> editDPColorCodeNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			// LkuData lkuColorData = null; // getLkuData(colorCodeLkuId);  // Black
			dto.setColorLkuId(null);
			dto.setColorLkuDesc(null);
			dto.setColorNumber(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortColorCodeNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPColorCodeNull(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortColorCodeAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortColorCodeNull(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		
	}
		
	// grouping / vlan  - not required
	
	private List<DataPortDTO> editDPGroupingVLANNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setVlanLkuId(null);
			dto.setVlanLkuDesc(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortGroupingVLANNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPGroupingVLANNull(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortGroupingVLANAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortGroupingVLANNull(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		

	}

		
	// mac address  - not required

	private List<DataPortDTO> editDPMacAddressNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setMacAddress(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortMacAddressNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPMacAddress(item, null);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortMacAddressAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortMacAddress(itemId, null);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		

	}


		
	// ip address - not required
	
	private List<DataPortDTO> editDPIPAddressNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setIpAddress(null);
			dto.setIpv6Address(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortIPAddressNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPIPAddressNull(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortIPAddressAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortIPAddressNull(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		

	}


		
	// snmp community string  - not required
	
	private List<DataPortDTO> editDPSNMPCommStrNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setCommunityString(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortSNMPCommStrNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPSNMPCommStrNull(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortSNMPCommStrAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortSNMPCommStrNull(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		

	}


		
	// comment - not required
	
	private List<DataPortDTO> editDPCommentNull(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setCommunityString(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortCommentNull(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPCommentNull(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	

	
	@Test
	public final void testDataPortCommentAsNotRequired() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortCommentNull(itemId);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
		// set data port as not used to allow delete
		setDataPortNotUsed(itemId);
		

	}


	// connector - not required for logical port
	@Test
	public final void testDataPortConnectorAsNotRequiredForLogicalPort() throws Throwable {

		// create the network blade item 
		Long itemId = createNWBladeItem();
	
		// add more than one logical ports - 4
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1020L);
	
		// edit port name and save the item
		valueIdDTOList = editConnectorNotProvided(itemId);
				
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}
	

	
	// connector - not required for virtual port
	@Test
	public final void testDataPortConnectorAsNotRequiredForVirtualPort() throws Throwable {

		ItItem vmItem = createNewTestDeviceVM("UNITTEST-PORT-DEV_VM-Test");
		Long itemId = vmItem.getItemId();
		
		// add more than one virtual ports - 4
		List<ValueIdDTO> valueIdDTOList = addVirtualPorts(itemId);
	
		// edit port name and save the item
		valueIdDTOList = editConnectorNotProvided(itemId);
				
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}
	

		

		
	// connector - required
	
	private List<DataPortDTO> editDPConnectorNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setConnectorLkuId(null);
			dto.setConnector(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editConnectorNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPConnectorNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortConnectorAsRequired() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editConnectorNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}

	// media - not required for logical port
	@Test
	public final void testDataPortMediaAsNotRequiredForLogicalPort() throws Throwable {

		// create the network blade item 
		Long itemId = createNWBladeItem();
	
		// add more than one logical ports - 4
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1020L);
	
		// edit port name and save the item
		valueIdDTOList = editMediaNotProvided(itemId);
				
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}
	

	
	// media - not required for virtual port
	@Test
	public final void testDataPortMediaAsNotRequiredForVirtualPort() throws Throwable {

		ItItem vmItem = createNewTestDeviceVM("UNITTEST-PORT-DEV_VM-Test");
		Long itemId = vmItem.getItemId();
		
		// add more than one virtual ports - 4
		List<ValueIdDTO> valueIdDTOList = addVirtualPorts(itemId);
	
		// edit port name and save the item
		valueIdDTOList = editMediaNotProvided(itemId);
				
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}

		
	// media - required for physical port
	
	private List<DataPortDTO> editDPMediaNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setMediaLksDesc(null);
			dto.setMediaLksValueCode(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editMediaNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPMediaNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortMediaAsRequired() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editMediaNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}


		
	// protocol - required
	
	
	private List<DataPortDTO> editDPProtocolNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setProtocolLkuDesc(null);
			dto.setProtocolLkuId(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editProtocolNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPProtocolNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortProtocolAsRequired() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editProtocolNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}


		
	// Data Rate - required
	
	private List<DataPortDTO> editDPDataRateNotProvided(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setSpeedLkuDesc(null);
			dto.setSpeedLkuId(null);
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editDataRateNotProvided(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPDataRateNotProvided(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortDataRateAsRequired() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editDataRateNotProvided(itemId);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);

	}


	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test field length ------------------------------- */
	/* -------------------------------------------------------------------- */
	
	
	
	// port name - max 64, min 1
	
	
	private List<DataPortDTO> editDPNameMoreThanMax(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		int count = 0;
		String portNameMoreThanMax = StringUtils.rightPad("Invalid Port Name Length", 65, '-');
		String portNameLessThanMin = null;
		for (DataPortDTO dto: dataPortDTOList) {
			if (0 == (count % 2)) {
				dto.setPortName(portNameLessThanMin);
			}
			else {
				dto.setPortName(portNameMoreThanMax);
			}
			count++;
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editPortNameLengthInvalid(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPNameMoreThanMax(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	// port name - length > 64 characters
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortPortNameAsInvalidLength() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortNameLengthInvalid(itemId);
		
		// save the item
		// saveItemCannotSaveWhenPortNameLengthInvalid(itemId, valueIdDTOList);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortFieldRequired");
		expectedErrorCodes.add("PortValidator.duplicatePortName");
		expectedErrorCodes.add("PortValidator.dataPortNameLength");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}

	}


	
	// comment - max 500
	
	
	private List<DataPortDTO> editDPCommentMoreThanMax(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		int count = 0;
		String commentMoreThanMax = StringUtils.rightPad("Invalid Port Comment Length", 501, '-');
		String commentLessThanMin = null;
		for (DataPortDTO dto: dataPortDTOList) {
			if (0 == (count % 2)) {
				dto.setComments(commentLessThanMin);
			}
			else {
				dto.setComments(commentMoreThanMax);
			}
			count++;
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> editCommentLengthInvalid(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPCommentMoreThanMax(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}

	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortCommentAsInvalidLength() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editCommentLengthInvalid(itemId);
		
		// save the item
		// saveItemCannotSaveWhenCommentLengthInvalid(itemId, valueIdDTOList);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortCommentLength");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}

	}



	// mac address field length is max 64
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortMacAddressLengthExcceds() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		String macAddressMoreThanMax = StringUtils.rightPad("Invalid Port Mac Address Length", 65, '-');
		
		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortMacAddress(itemId, macAddressMoreThanMax);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.portMacAddressLength");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}
		
	}
	

	
	// TODO:: snmp community string field length is max 50
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortCommStrLengthExceeds() throws Throwable {
		Long itemId = createItemAndPorts();

		String commStrMoreThanMax = StringUtils.rightPad("Invalid Port Comm Str Length", 51, '-');
		
		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortCommStr(itemId, commStrMoreThanMax);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.portCommStrLength");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
	}
	

	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test only 1 port can have community string ----*/
	/* -------------------------------------------------------------------- */
	
	
	private List<DataPortDTO> setDPCommStr(Map<String, UiComponentDTO> item, long numPorts) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the ports
		Long count = 0L;
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setCommunityString("communityStr" + count.toString());
			count++;
			if (--numPorts == 0) {
				break;
			}
		}

		return dataPortDTOList;
	}
	
	private List<ValueIdDTO> setCommunityStr(long itemId, long numPorts) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = setDPCommStr(item, numPorts);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}


	
	// more than one ports have community string
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortMoreThanOneCommunityStr() throws Throwable {
		
		// create the item and ports
		Long itemId = createItemAndPorts();

		// change the community string of a couple of ports
		List<ValueIdDTO> valueIdDTOList = setCommunityStr(itemId, 2);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortSnmpCommunityCount");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		
	}



	/* -------------------------------------------------------------------- */
	/* ---------------- Test NW blade can have only 1 logical port ----*/
	/* -------------------------------------------------------------------- */

	private void saveItemWithExpectedErrorCode(Long itemId, List<ValueIdDTO> valueIdDTOList, List<String> errorCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		try {
			Map<String,UiComponentDTO> componentDTOMap = null;
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			// get the item for more validation while getting the saved item and ports
			Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
			assertNotNull( item );		
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


	
	private Long createNWBladeItem() throws Throwable {
		ItItem devBladeItem = createNewTestPortNetworkBlade("UNITTEST-PORT-NW_BLADE-Test", 55);
		return devBladeItem.getItemId();

	}

	private Long createDevBladeItem() throws Throwable {
		ItItem devBladeItem = createNewTestPortDeviceBlade("UNITTEST-PORT-DEV_BLADE-Test", 55, null);
		return devBladeItem.getItemId();
	}
	
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortNWBladeMoreThanOneLogicalPorts() throws Throwable {
		
		// create the network blade item 
		Long itemId = createNWBladeItem();

		// add more than one logical ports - 4
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1020L);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.invalidLogicalPortCount");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		
	}

	@Test
	public final void testDataPortNWBladeWithOneLogicalPort() throws Throwable {
		
		// create the item and ports
		Long itemId = createNWBladeItem();

		// add one logical port
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(itemId, 4631L);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortSnmpCommunityCount");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		
	}

	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test add new data ports ------------------------*/
	/* -------------------------------------------------------------------- */

	// add ports when connected
	
	// add ports when not connected
	
	/* -------------------------------------------------------------------- */
	/* ---------------- Test delete data ports --------------------------*/
	/* -------------------------------------------------------------------- */

	
	
	
	private List<DataPortDTO> deletePorts( Map<String, UiComponentDTO> item, long numPorts ) {
		List<DataPortDTO> dataPortDTOList = getDP(item);
		@SuppressWarnings("unused")
		DataPortDTO dto = null;

		// delete the ports
		Iterator<DataPortDTO> itr1 = dataPortDTOList.iterator();
		while (itr1.hasNext()) {
			dto = itr1.next();
			itr1.remove();
			if (--numPorts == 0) {
				break;
			}
		}

		return dataPortDTOList;
	}
	
	
	private List<ValueIdDTO> deletePort( long itemId, long numPorts ) throws Throwable {
		
		// get item information
		
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit the power ports comments
		List<DataPortDTO> ppDtoList = deletePorts( item, numPorts );

		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}


	
	// delete port when connected
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortDeleteWhenConnected() throws Throwable {
		Long itemId = createItemAndPortsConnected();

		// delete a couple of ports
		List<ValueIdDTO> valueIdDTOList = deletePort(itemId, 2);
		
		// save the item
		// saveItemCannotDeleteWhenConnected(itemId, valueIdDTOList);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.connectedDataPortCannotDelete");
		try {
			saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		}
		finally {
			// set data port as not used to allow delete
			setDataPortNotUsed(itemId);
		}
	}


	
	// delete port when not connected
	@Test
	public final void testDataPortDeleteWhenNotConnected() throws Throwable {
		
		// create the item and ports
		Long itemId = createItemAndPorts();

		// delete a couple of ports
		List<ValueIdDTO> valueIdDTOList = deletePort(itemId, 2);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}



	
	
	/* ---------------------------------------------------------------------------- */
	/* ---------------- Test validate max port limit 288 for data ports  ---- */
	/* ---------------------------------------------------------------------------- */
	
	// test max data port limit is 288
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortMaxNumOfPorts() throws Throwable {
		
		// create the item and ports
		Long itemId = createNWBladeItem();

		List<ValueIdDTO> valueIdDTOList = null;

		// add logical ports
		// model id 1652 has 53 ports
		valueIdDTOList = addDataPortsWithOrderNumberMultipleTimes(itemId, 1652L, 6);
		
		// save the item
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.dataPortMaxNumOfPorts");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
		
	}

	
	/* ---------------------------------------------------------------------------- */
	/* ---------------- Test Sort Order  --------------------------------------- */
	/* ---------------------------------------------------------------------------- */
	
	// Sort order unique per port type
	@Test
	public final void testDataPortSortOrderUniquePerPortType() throws Throwable {
		
		// create the item and ports
		Long itemId = createDevBladeItem();

		List<ValueIdDTO> valueIdDTOList = null;

		// add physical ports
		// model id 1652 has 53 physical ports 
		valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1652L);
		
		// add logical ports
		// model id 1020 has 4 logical ports with same sort order
		valueIdDTOList = addDataPortsWithOrderNumber(itemId, 1020L);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}

	
	// Sort order invalid ( <= 0): server should correct it
	@Test
	public final void testDataPortSortOrderInvalid() throws Throwable {
		
		// create the item and ports
		Long itemId = createDevBladeItem();

		List<ValueIdDTO> valueIdDTOList = null;

		// add physical ports
		// model id 1652 has 53 physical ports with invalid sort order (same has sort order 0) 
		valueIdDTOList = addDataPorts(itemId, 1652L);
		
		// add logical ports
		// model id 1020 has 4 logical ports with invalid sort order (same has sort order 0)
		valueIdDTOList = addDataPorts(itemId, 1020L);
		
		// save the item
		saveItem(itemId, valueIdDTOList);
		
	}

	/* -------------------------------------------------------------------- */
	/* test logical ports for blades only  									  */
	/* -------------------------------------------------------------------- */
	
	private void addLogicalPortsWithExpectedErrors(Long itemId) throws Throwable {
		// add 4 logical ports
		List<ValueIdDTO> valueIdDTOList = addDataPorts(itemId, 1020L);
		
		// save item with expected error
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.logicalOnlyForBlades");
		expectedErrorCodes.add("PortValidator.logicalUnsupportedClass");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
	}

	private void addLogicalPortsWithNoErrors(Long itemId, Long modelId) throws Throwable {
		// add 4 logical ports
		List<ValueIdDTO> valueIdDTOList = addDataPorts(itemId, modelId);
		
		// save item with expected error
		saveItem(itemId, valueIdDTOList);
	}
	
	// RPDU
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForRPDU() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		Long itemId = rpduItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}
	
	// Device - Chassis
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForDevChassis() throws Throwable {
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-PORT-DEV_CHASSIS-Test", null, 55);
		Long itemId = devChassisItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}
	
	// Device - Blade
	@Test
	public final void testDataPortLogicalPortForDevBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortDeviceBlade("UNITTEST-PORT-DEV_BLADE-Test", 55, null);
		Long itemId = devBladeItem.getItemId();
		
		addLogicalPortsWithNoErrors(itemId, 1020L);
	}

	// Device - Free standing
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForDevFS() throws Throwable {
		ItItem fsDevItem = createNewTestDeviceFS("UNITTEST-PORT-DEV_FS-Test");
		Long itemId = fsDevItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// VM
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForVM() throws Throwable {
		ItItem vmItem = createNewTestDeviceVM("UNITTEST-PORT-DEV_VM-Test");
		Long itemId = vmItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// Network //
	
	// Network Chassis
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForNWChassis() throws Throwable {
		ItItem nwChassisItem = createNewTestPortNetworkChassis("UNITTEST-PORT-NW_CHASSIS-Test", 55);
		Long itemId = nwChassisItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// Network Blade
	@Test
	public final void testDataPortLogicalPortForNWBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortNetworkBlade("UNITTEST-PORT-NW_BLADE-Test", 55);
		Long itemId = devBladeItem.getItemId();
		
		addLogicalPortsWithNoErrors(itemId, 4631L);
	}

	// Network Stack
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForNWStack() throws Throwable {
		ItItem nwStackItem = createNewTestNetworkStack("UNITTEST-PORT-NW_STACK-Test", "StackName");
		Long itemId = nwStackItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// UPS
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForUPS() throws Throwable {
		MeItem upsItem = createNewTestUPS("UNITTEST-PORT-UPS-Test");
		Long itemId = upsItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// Floor PDU
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForFloorPDU() throws Throwable {
		MeItem floorPDUItem = createNewTestFloorPDU("UNITTEST-PORT-FloorPDU-Test");
		Long itemId = floorPDUItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// CRAC
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForCRAC() throws Throwable {
		Item cracItem = createNewTestCRAC("UNITTEST-PORT-CRAC-Test");
		Long itemId = cracItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	// Probe
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortLogicalPortForProbe() throws Throwable {
		ItItem probeItem = createNewPortTestProbe("UNITTEST-PORT-PROBE-Test", 55);
		Long itemId = probeItem.getItemId();
		
		addLogicalPortsWithExpectedErrors(itemId);
	}

	/* -------------------------------------------------------------------- */
	/* test virtual ports for VM only  									  */
	/* -------------------------------------------------------------------- */
	
	private List<DataPortDTO> createVMPortDto(Long numOfVMPorts) {
		
		List<DataPortDTO> dtos = new ArrayList<DataPortDTO>();
		
		for (Long numPort = 0L; numPort < numOfVMPorts; numPort++) {
		
			DataPortDTO dto = new DataPortDTO();
			dto.setPortName("Dummy VM Name" + numPort.toString());
			dto.setSortOrder(numPort.intValue());
			dto.setPortSubClassLksValueCode(SystemLookup.PortSubClass.VIRTUAL);
			dto.setProtocolLkuId(1042L);
			dto.setSpeedLkuId(1025L);
			
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	
	private List<ValueIdDTO> addDataPorts(long itemId, List<DataPortDTO> dtos) throws Throwable {
		
		// get item information
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		List<DataPortDTO> dataPortDTOListCopy = new ArrayList<DataPortDTO>();
		dataPortDTOListCopy.addAll(dataPortDTOList);

		// add power ports from the model's library
		// java.util.Date date = new java.util.Date();
		for (DataPortDTO dpdto: dtos) {
			dpdto.setItemId(itemId);
			// dpdto.setPortName(dpdto.getPortName() + date.toString() + "-");
			dataPortDTOListCopy.add(dpdto);
		}
		
		List<ValueIdDTO> valueIdDTOList = prepareItemDataPortValueIdDTOList(item, dataPortDTOListCopy);
		return valueIdDTOList;
	}


	private List<ValueIdDTO> addVirtualPorts(Long itemId) throws Throwable {
		List<DataPortDTO> virtualPorts = createVMPortDto(4L);
		
		// add 4 virtual ports
		List<ValueIdDTO> valueIdDTOList = addDataPorts(itemId, virtualPorts);
		
		return valueIdDTOList;
	}
	
	private void addVirtualPortsWithExpectedErrors(Long itemId) throws Throwable {

		// add 4 virtual ports
		List<ValueIdDTO> valueIdDTOList = addVirtualPorts(itemId); 
		
		// save item with expected error
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.virtualPortOnlyForVM");
		expectedErrorCodes.add("PortValidator.virtualUnsupportedClass");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
	}

	private void addVirtualPortsWithNoErrors(Long itemId) throws Throwable {
		
		// add 4 virtual ports
		List<ValueIdDTO> valueIdDTOList = addVirtualPorts(itemId); 
		
		// save item with expected error
		saveItem(itemId, valueIdDTOList);
	}
	
	// VM
	@Test
	public final void testDataPortVirtualPortForNonVMItem() throws Throwable {
		
		ItItem vmItem = createNewTestDeviceVM("UNITTEST-PORT-DEV_VM-Test");
		Long itemId = vmItem.getItemId();
		
		addVirtualPortsWithNoErrors(itemId);
		
	}

	// Device - Chassis
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForDevChassis() throws Throwable {
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-PORT-DEV_CHASSIS-Test", null, 55);
		Long itemId = devChassisItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}
	
	// Device - Blade
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForDevBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortDeviceBlade("UNITTEST-PORT-DEV_BLADE-Test", 55, null);
		Long itemId = devBladeItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Device - Free standing
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForDevFS() throws Throwable {
		ItItem fsDevItem = createNewTestDeviceFS("UNITTEST-PORT-DEV_FS-Test");
		Long itemId = fsDevItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Network //
	
	// Network Chassis
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForNWChassis() throws Throwable {
		ItItem nwChassisItem = createNewTestPortNetworkChassis("UNITTEST-PORT-NW_CHASSIS-Test", 55);
		Long itemId = nwChassisItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Network Blade
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForNWBlade() throws Throwable {
		ItItem devBladeItem = createNewTestPortNetworkBlade("UNITTEST-PORT-NW_BLADE-Test", 55);
		Long itemId = devBladeItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Network Stack
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForNWStack() throws Throwable {
		ItItem nwStackItem = createNewTestNetworkStack("UNITTEST-PORT-NW_STACK-Test", "StackName");
		Long itemId = nwStackItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// UPS
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForUPS() throws Throwable {
		MeItem upsItem = createNewTestUPS("UNITTEST-PORT-UPS-Test");
		Long itemId = upsItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Floor PDU
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForFloorPDU() throws Throwable {
		MeItem floorPDUItem = createNewTestFloorPDU("UNITTEST-PORT-FloorPDU-Test");
		Long itemId = floorPDUItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// CRAC
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForCRAC() throws Throwable {
		Item cracItem = createNewTestCRAC("UNITTEST-PORT-CRAC-Test");
		Long itemId = cracItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}

	// Probe
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortVirtualPortForProbe() throws Throwable {
		ItItem probeItem = createNewPortTestProbe("UNITTEST-PORT-PROBE-Test", 55);
		Long itemId = probeItem.getItemId();
		
		addVirtualPortsWithExpectedErrors(itemId);
	}


	/* -------------------------------------------------------------------- */
	/* port name must be unique  												  */
	/* -------------------------------------------------------------------- */

	private List<DataPortDTO> editDPNameSame(Map<String, UiComponentDTO> item) {
		List<DataPortDTO> dataPortDTOList = getDP(item);

		// edit the power ports
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setPortName("same name to all ports");
		}

		return dataPortDTOList;
	}
	

	
	private List<ValueIdDTO> editPortNameSame(long itemId) throws Throwable {
		// get item information
		Map<String, UiComponentDTO> item = getItemMap(itemId);
		
		// edit PP name
		List<DataPortDTO> ppDtoList = editDPNameSame(item);
		
		// set the changes to the item
		return prepareItemDataPortValueIdDTOList(item, ppDtoList);
	}
	
	// port name - can edit
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDataPortPortNameUnique() throws Throwable {
		Long itemId = createItemAndPorts();

		// edit port name and save the item
		List<ValueIdDTO> valueIdDTOList = editPortNameSame(itemId);
		
		// save item with expected error
		List<String> expectedErrorCodes = new ArrayList<String>();
		expectedErrorCodes.add("PortValidator.duplicatePortName");
		saveItemWithExpectedErrorCode(itemId, valueIdDTOList, expectedErrorCodes);
	}
	

	/* -------------------------------------------------------------------- */
	/* test connector validity and invalid ports not getting created  */
	/* -------------------------------------------------------------------- */
	@Test
	public final void testRPDUItemClassFewInvalidPorts() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithOrderNumber(rpduItem.getItemId(), 90L); // 5 data ports
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		
		valueIdDTOList = editFewConnDPInvalid( rpduItem.getItemId(), 2 ); // set 2 connectors as invalid
		
		// ItemValidator.invalidPortConnector
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String,BusinessValidationException.Warning> warningMap = be.getValidationWarningsMap(); //be.getErrors();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				if (!entry.getKey().equals("ItemValidator.invalidPortConnector")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					// set the skip validator and save again ...
					editItemSkipValidation(rpduItem.getItemId(), true);
					componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
					assertNotNull(componentDTOMap);
					assertEquals ( getDataPortCount(componentDTOMap), 3 ); // 3 data ports must be created, because 2 were set to be invalid connectors
				}
			}
		}
	}

	
	/* ---------------------------------------------------------------------------- */
	/* test ports with duplicate index passed: server will correct the index */
	/* ----------------------------------------------------------------------------- */
	@Test
	public final void testRPDUItemClassWithDuplicatePortIndex() throws Throwable {
		MeItem rpduItem = createNewTestPortRPDU("UNITTEST-PORT-RPDU-Test", null, 55);
		
		List<ValueIdDTO> valueIdDTOList = addDataPortsWithDuplicateOrderNumber(rpduItem.getItemId(), 90L); // 5 data ports
		
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
		
		valueIdDTOList = getDtoList(rpduItem.getItemId()); // editFewConnDPInvalid( rpduItem.getItemId(), 2 ); // set 2 connectors as invalid
		
		// ItemValidator.invalidPortConnector
		try {
			componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		}
		catch (BusinessValidationException be) {
			Map<String,BusinessValidationException.Warning> warningMap = be.getValidationWarningsMap(); //be.getErrors();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet())
			{
				if (!entry.getKey().equals("ItemValidator.invalidPortConnector")) {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
				else {
					// set the skip validator and save again ...
					editItemSkipValidation(rpduItem.getItemId(), true);
					componentDTOMap = itemHome.saveItem(rpduItem.getItemId(), valueIdDTOList, getTestAdminUser());
					assertNotNull(componentDTOMap);
					assertEquals ( getDataPortCount(componentDTOMap), 3 ); // 3 data ports must be created, because 2 were set to be invalid connectors
				}
			}
		}
	}

}
