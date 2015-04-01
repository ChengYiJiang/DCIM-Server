package com.raritan.tdz.helper;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.SensorPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.util.RequestDTO;

/**
 * 
 * @author bunty
 *
 */

public abstract class TestHelperBase {

	private ItemHome itemHome;
	private UserInfo userInfo;
	
	public TestHelperBase(ItemHome itemHome, UserInfo userInfo) {
		super();
		this.itemHome = itemHome;
		this.userInfo = userInfo;
	}

	public List<ValueIdDTO> getDto(Long itemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}
	
	public List<ValueIdDTO> prepareItemValueIdDTOList( Map<String, UiComponentDTO> item ) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
		/*for (Map.Entry<String, UiComponentDTO> pairs : item.entrySet() ) {
			
		}*/
		
	    // @SuppressWarnings("rawtypes")
		// Iterator it = item.entrySet().iterator();
	    // while (it.hasNext()) {
		for (Map.Entry<String, UiComponentDTO> pairs : item.entrySet() ) {
	        // @SuppressWarnings("rawtypes")
			// Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        // it.remove(); // avoids a ConcurrentModificationException
			
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
					// ((String)pairs.getKey()).equals("cmbRowLabel") ||
					((String)pairs.getKey()).equals("tiLoadCapacity") ||
					((String)pairs.getKey()).equals("cmbCabinetGrouping") ||
					// ((String)pairs.getKey()).equals("cmbRowPosition") ||
					((String)pairs.getKey()).equals("tiFrontClearance") ||
					// ((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			/*else if (((String)pairs.getKey()).equals("tabPowerPorts")) {
				dto.setData(powerPortDTOListCopy);
			}*/
			/*else if (((String)pairs.getKey()).equals("tabDataPorts")) {
				dto.setData(dataPortDTOListCopy);
			}*/
			else if (((String)pairs.getKey()).equals("cmbRowLabel")) {
				String rowLabel = (String) field.getUiValueIdField().getValue();
				dto.setData(rowLabel + "temp");
			}
			else if (((String)pairs.getKey()).equals("cmbSlotPosition")) {
				// Integer slotPosition = (Integer) field.getUiValueIdField().getValue();
				dto.setData("");
			}
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
	    return valueIdDTOList;
	}
	
	
	// Save the Item 
	public Long saveNewItemWithExpectedErrorCode(Long itemId, List<ValueIdDTO> valueIdDTOList, List<String> errorCodes, List<String> warningCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		Long newItemId = -1L;
		try {
			Map<String,UiComponentDTO> componentDTOMap = null;
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, userInfo);
			assertNotNull(componentDTOMap);
			
			newItemId = getItemId(componentDTOMap);
			
			// get the item for more validation while getting the saved item and ports
			// Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
			// assertNotNull( item );	
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
				if (warningCodes.contains(entry.getKey())) { 
					throwbe = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwbe) {
				throw be;
			}
		}
		return newItemId;
	}
	
	
	public void saveItemWithExpectedErrorCode(Long itemId, List<ValueIdDTO> valueIdDTOList, List<String> errorCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		try {
			Map<String,UiComponentDTO> componentDTOMap = null;
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, userInfo);
			assertNotNull(componentDTOMap);
			
			// get the item for more validation while getting the saved item and ports
			Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );
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

	private Long getItemId(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		Long itemId = (Long) itemNameField.getUiValueIdField().getValueId();
		return itemId;
	}

	// Set Item Name
	public List<ValueIdDTO> setItemName(Map<String, UiComponentDTO> item, String itemName) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item name
		editItemName(item, itemName);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	public List<ValueIdDTO> setItemName(Long itemId, String itemName) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );

		return setItemName(item, itemName);
	}
	
	public void editItemName(Map<String, UiComponentDTO> item, String itemName) {
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull(itemNameField);
		String statusStr = (String) itemNameField.getUiValueIdField().getValue();
		Long statusId = (Long) itemNameField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemNameField.getUiValueIdField().setValue(itemName);
	}

	// Set Item cmbLocation
	public List<ValueIdDTO> setItemLocation(Map<String, UiComponentDTO> item, Long locationId) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item name
		editItemLocation(item, locationId);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	public List<ValueIdDTO> setItemLocation(Long itemId, Long locationId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, userInfo );

		return setItemLocation(item, locationId);
	}
	
	public void editItemLocation(Map<String, UiComponentDTO> item, Long locationId) {
		UiComponentDTO itemNameField = item.get("cmbLocation");
		assertNotNull(itemNameField);
		String statusStr = (String) itemNameField.getUiValueIdField().getValue();
		Long statusId = (Long) itemNameField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemNameField.getUiValueIdField().setValue(locationId);
		
	}

	// set item id
	public void editItemId(Map<String, UiComponentDTO> item, Long itemId) {
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull(itemNameField);
		String nameStr = (String) itemNameField.getUiValueIdField().getValue();
		Long oldItemId = (Long) itemNameField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + oldItemId.toString() + "str = " + nameStr);
		itemNameField.getUiValueIdField().setValueId(itemId);
	}
	
	// set U position
	public void editItemUPosition(Map<String, UiComponentDTO> item, long newUPosition) {
		UiComponentDTO itemStatusField = item.get("cmbUPosition");
		assertNotNull(itemStatusField);
		Long uPos = (Long) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + uPos);
		itemStatusField.getUiValueIdField().setValue(newUPosition);
	}

	// set Chassis
	public void editItemPlacedChassis(Map<String, UiComponentDTO> item, String chassisName, long chassisId) {
		UiComponentDTO itemChassisField = item.get("cmbChassis");
		assertNotNull(itemChassisField);

		itemChassisField.getUiValueIdField().setValue(chassisName);
		itemChassisField.getUiValueIdField().setValueId(chassisId);
	}

	// set Chassis
	public void editItemPlacedSlotPosition(Map<String, UiComponentDTO> item, String slotLabel, long slotNumber) {
		UiComponentDTO itemChassisField = item.get("cmbSlotPosition");
		assertNotNull(itemChassisField);

		itemChassisField.getUiValueIdField().setValue(slotLabel);
		itemChassisField.getUiValueIdField().setValueId(slotNumber);
	}

	
	// set Cabinet
	public void editItemPlacedCabinet(Map<String, UiComponentDTO> item, String cabName, long cabinetId) {
		UiComponentDTO itemStatusField = item.get("cmbCabinet");
		assertNotNull(itemStatusField);

		itemStatusField.getUiValueIdField().setValue(cabName);
		itemStatusField.getUiValueIdField().setValueId(cabinetId);
	}

	// set Location
	public void editItemPlacedLocation(Map<String, UiComponentDTO> item, String locName, long locId) {
		UiComponentDTO itemLocationField = item.get("cmbLocation");
		assertNotNull(itemLocationField);

		itemLocationField.getUiValueIdField().setValue(locName);
		itemLocationField.getUiValueIdField().setValueId(locId);
	}

	
	// set item status
	public void editItemStatus(Map<String, UiComponentDTO> item, long itemStatus) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValueId(itemStatus);
	}
	
	public String getItemStatusStr(Map<String, UiComponentDTO> item) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		return statusStr;
	}
	
	public Long getWhenMovedId(Map<String, UiComponentDTO> item) {
		UiComponentDTO itemStatusField = item.get("_whenMoveItemId");
		assertNotNull(itemStatusField);
		Long whenMovedItemId = (Long) itemStatusField.getUiValueIdField().getValue();
		return whenMovedItemId;
	}
	
	public Long getMovingItemId(Map<String, UiComponentDTO> item) {
		UiComponentDTO itemStatusField = item.get("_itemToMoveId");
		assertNotNull(itemStatusField);
		Long whenMovedItemId = (Long) itemStatusField.getUiValueIdField().getValue();
		return whenMovedItemId;
	}

	public List<RequestDTO> getRequestDTOs(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemRequestField = item.get("_itemRequests");
		if (null == itemRequestField) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<RequestDTO> requestDTOList = (List<RequestDTO>) itemRequestField.getUiValueIdField().getValue();
		return requestDTOList;
	}


	// set the port ids and the item ids
	public void editPowerPortItemId(Map<String, UiComponentDTO> item, long itemId, long portId) {
		
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		if (null == itemPowerPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPortId(portId);
			dto.setItemId(itemId);
			// This will work for Rack PDU tests that has only one input cord. For multiple input cords, get the data from the models library 
			if (dto.isInputCord()) {
				dto.setModelPowerPortId(1L);
			}
			if (dto.isRackPduOutPut()) {
				dto.setInputCordModelPowerPortId(1L);
			}
		}
	}
	
	public List<PowerPortDTO> getPowerPortDTOs(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		if (null == itemPowerPortsField) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		return powerPortDTOList;
	}

	public List<DataPortDTO> getDataPortDTOs(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		if (null == itemDataPortsField) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		return dataPortDTOList;
	}

	public void editPowerPortAction(Map<String, UiComponentDTO> item, long moveActionLkpValueCode) {
		
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		if (null == itemPowerPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setMoveActionLkpValueCode(moveActionLkpValueCode);
		}
	}


	public void editDataPortItemId(Map<String, UiComponentDTO> item, long itemId, long portId) {
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		if (null == itemDataPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setPortId(portId);
			dto.setItemId(itemId);
		}
	}

	public void editSensorPortItemId(Map<String, UiComponentDTO> item, long itemId, long portId) {
		
		// get the sensor ports 
		UiComponentDTO itemSensorPortsField = item.get("tabSensorPorts");
		if (null == itemSensorPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<SensorPortDTO> sensorPortDTOList = (List<SensorPortDTO>) itemSensorPortsField.getUiValueIdField().getValue();
		for (SensorPortDTO dto: sensorPortDTOList) {
			dto.setPortId(portId);
			dto.setItemId(itemId);
		}
	}

	public void editDataPortAction(Map<String, UiComponentDTO> item, long moveActionLkpValueCode) {
		
		// get the power ports 
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		if (null == itemDataPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		for (DataPortDTO dto: dataPortDTOList) {
			dto.setMoveActionLkpValueCode(moveActionLkpValueCode);
		}
	}

}
