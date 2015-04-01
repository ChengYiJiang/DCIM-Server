/**
 * 
 */
package com.raritan.tdz.item.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author bunty
 *
 */

// Installed 
// Device / Network
// Rackable

public class ItemMoveValidMove extends ItemMoveTestBase {

	
	
	@SuppressWarnings("serial")
	public static final Map<String, TestOperations> validDataCollection  =
			Collections.unmodifiableMap(new HashMap<String, TestOperations>() {{
				
				// Device Standard Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(233L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CLARITY
				
				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // DCT3-HP-CHASSIS-01

				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(941L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 34L, new ArrayList<String>(), new ArrayList<String>()) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 34L, new ArrayList<String>(), new ArrayList<String>()) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 34L, new ArrayList<String>(), new ArrayList<String>()) 
				); // "NJDMZA-01"
				
				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // DCT3-HP-CHASSIS-01

				// Device Blade Server Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4954L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // BLADE01

				// Network Blade Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(341L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // NJA01A-1

				
				// Device Standard Rackable Powered-Off
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(274L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CSG05 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CSG05 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CSG05

				// Network Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4914L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CAB4-SWITCH1
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, -1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CAB4-SWITCH1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, -1L, new ArrayList<String>(), new ArrayList<String>()) 
				); // CAB4-SWITCH1
				
				// Cabinet Installed Free-Standing
				put(
						new Long(SystemLookup.Class.CABINET).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(3L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), new ArrayList<String>())
				); // 1A

				// Cabinet Installed Free-Standing
				put(
						new Long(SystemLookup.Class.CABINET).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.PLANNED,
						new TestOperations(20L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), new ArrayList<String>())
				); // 3B

				// Cabinet Installed Free-Standing
				put(
						new Long(SystemLookup.Class.CABINET).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(40L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), new ArrayList<String>())
				); // AF

			}});

	@DataProvider(name = "validData")
	public Object[][] validDataProviders() {
		
		int dataSize = validDataCollection.size();
		
		Object[][] dataObjects = new Object[dataSize][2];
		
		int index = 0;
		for (Map.Entry<String, TestOperations> entry: validDataCollection.entrySet()) {
			dataObjects[index][0] = entry.getKey();
			dataObjects[index][1] = entry.getValue();
			index++;
		}
		
		return dataObjects;

	}

	@Test(dataProvider="validData")
	public void testAllValidMove(String testedOn, TestOperations testData) throws Throwable {
		
		System.out.println("Test Key = " + testedOn + "\nTest Value = " + testedOn.toString() + "\n");
		
		// This is required because of timeOut, if no timeout is used, this can be removed
		// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));

		Long newItemId = -1L;
		
		try {
			// Get item details
			Item item = itemDAO.getItem(testData.getMovingItemId());
	
			// initiate the move
			newItemId = testNewItemWithMove(item, testData.getErrorCode(), testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode(), testData.getNewUPosition(), true, testData.getWarningCode());
		
			// validate the save of moved item
			validateItemMove(testData.getMovingItemId(), newItemId, testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode());
			
 		}
		finally {
			if (-1 == newItemId) {
				return;
			}
			
			List<Long> itemIds = new ArrayList<Long>();
			itemIds.add(newItemId);
			itemHome.deleteItems(itemIds, userInfo);
			
			session.flush();
			session.clear();
		}
		
	}

	@SuppressWarnings("serial")
	public static final Map<String, TestOperations> validDataCollectionInitialWarning  =
			Collections.unmodifiableMap(new HashMap<String, TestOperations>() {{
				
				// Device Standard Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(233L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CLARITY
				
				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // DCT3-HP-CHASSIS-01
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(941L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // "NJDMZA-01"

				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // DCT3-HP-CHASSIS-01

				// Device Blade Server Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4954L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // BLADE01

				// Network Blade Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(341L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // NJA01A-1

				
				// Device Standard Rackable Powered-Off
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(274L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CSG05 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CSG05 failed
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CSG05
				
				// Network Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4914L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CAB4-SWITCH1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CAB4-SWITCH1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited")) 
				); // CAB4-SWITCH1

				// Cabinet Installed Free-Standing
				put(
						new Long(SystemLookup.Class.CABINET).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT,
						new TestOperations(3L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited"))
				); // 1A

				// Cabinet Installed Free-Standing
				put(
						new Long(SystemLookup.Class.CABINET).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.PLANNED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT,
						new TestOperations(20L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemEdited"))
				); // 3B


			}});

	@DataProvider(name = "validDataInitialWarning")
	public Object[][] validDataInitialWarningProviders() {
		
		int dataSize = validDataCollectionInitialWarning.size();
		
		Object[][] dataObjects = new Object[dataSize][2];
		
		int index = 0;
		for (Map.Entry<String, TestOperations> entry: validDataCollectionInitialWarning.entrySet()) {
			dataObjects[index][0] = entry.getKey();
			dataObjects[index][1] = entry.getValue();
			index++;
		}
		
		return dataObjects;

	}

	
	@Test(dataProvider="validDataInitialWarning")
	public void testAllValidInitialMoveWithSkipValidation(String testedOn, TestOperations testData) throws Throwable {
		
		System.out.println("Test Key = " + testedOn + "\nTest Value = " + testedOn.toString() + "\n");
		
		// This is required because of timeOut, if no timeout is used, this can be removed
		// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		Long newItemId = -1L;

		try {
			// Get item details
			Item item = itemDAO.getItem(testData.getMovingItemId());
					
			try {
				// initiate the move
				newItemId = testNewItemWithMove(item, testData.getErrorCode(), testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode(), testData.getNewUPosition(), false, testData.getWarningCode());
			}
			catch(BusinessValidationException be) {
				Map<String, String> errorMap = be.getErrors();
				boolean throwbe = false;
				if (errorMap.size() > 0) {
					throwbe = true;
					throw be;
				}
				Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
				for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet()) {
					if (entry.getKey().equals("ItemMoveValidator.MoveItemEdited")) {
						session.clear();
						newItemId = testNewItemWithMove(item, testData.getErrorCode(), testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode(), testData.getNewUPosition(), true, new ArrayList<String>());
					}
					else {
						Assert.fail("Add test to handle the warning message" + entry.getKey() + " / " + entry.getValue());
					}
				}
				if (throwbe) {
					throw be;
				}
			}
		
			// validate the save of moved item
			validateItemMove(testData.getMovingItemId(), newItemId, testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode());
			
 		}
		finally {

			if (-1 == newItemId) {
				return;
			}
			// clean the when moved item
			List<Long> itemIds = new ArrayList<Long>();
			itemIds.add(newItemId);
			itemHome.deleteItems(itemIds, userInfo);
			
			session.flush();
			session.clear();
			
		}
		
	}

	@SuppressWarnings("serial")
	public static final Map<String, TestOperations> validDataCollectionCabinetWarning  =
			Collections.unmodifiableMap(new HashMap<String, TestOperations>() {{
				
				// Device Standard Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(233L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CLARITY 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(233L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CLARITY
				
				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // DCT3-HP-CHASSIS-01
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(941L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // "NJDMZA-01" 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(941L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 34L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // "NJDMZA-01"

				// Device Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4976L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // DCT3-HP-CHASSIS-01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4976L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // DCT3-HP-CHASSIS-01

				// Device Blade Server Installed
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4954L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // BLADE01 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_SERVER + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4954L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // BLADE01

				// Network Blade Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(341L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // NJA01A-1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.BLADE + SystemLookup.Mounting.BLADE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(341L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // NJA01A-1
				
				// Device Standard Rackable Powered-Off
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(274L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CSG05 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CSG05 
				
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.POWERED_OFF + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(274L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 27L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CSG05
				
				// Network Chassis Rackable Installed
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.DONT_CONNECT + SystemLookup.MoveAction.DONT_CONNECT, 
						new TestOperations(4914L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CAB4-SWITCH1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET + SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_POWER_OUTLET , SystemLookup.MoveAction.RECONNECT_TO_DATA_PANEL, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CAB4-SWITCH1 
				
				put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.SubClass.CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED + SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU + SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, 
						new TestOperations(4914L, SystemLookup.MoveAction.RECONNECT_TO_RACK_PDU, SystemLookup.MoveAction.RECONNECT_TO_NETWORK_ITEM, -1L, new ArrayList<String>(), Arrays.asList("ItemMoveValidator.MoveItemCabinetChanged")) 
				); // CAB4-SWITCH1


			}});

	@DataProvider(name = "validDataCabinetWarning")
	public Object[][] validDataCabinetWarningProviders() {
		
		int dataSize = validDataCollectionCabinetWarning.size();
		
		Object[][] dataObjects = new Object[dataSize][2];
		
		int index = 0;
		for (Map.Entry<String, TestOperations> entry: validDataCollectionCabinetWarning.entrySet()) {
			dataObjects[index][0] = entry.getKey();
			dataObjects[index][1] = entry.getValue();
			index++;
		}
		
		return dataObjects;

	}


	
	@Test(dataProvider="validDataCabinetWarning")
	public void testAllValidMoveWithCabinetChanged(String testedOn, TestOperations testData) throws Throwable {
		
		System.out.println("Test Key = " + testedOn + "\nTest Value = " + testedOn.toString() + "\n");
		
		// This is required because of timeOut, if no timeout is used, this can be removed
		// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));

		Long newItemId = -1L; 
		
		try {
			// Get item details
			Item item = itemDAO.getItem(testData.getMovingItemId());
	
			
					
			// initiate the move
			newItemId = testNewItemWithMove(item, testData.getErrorCode(), testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode(), testData.getNewUPosition(), true, new ArrayList<String>());
		
			// validate the save of moved item
			validateItemMove(testData.getMovingItemId(), newItemId, testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode());
			
			session.flush();
			session.clear();
			
			if (-1 == newItemId) {
				return;
			}
			
			// Change the cabinet on the when moved item and test the warning message
			try {
				// edit the cabinet and expect a warning
				newItemId = testEditMovedItemCabinetChanged(newItemId, false, testData.getErrorCode(), testData.getWarningCode()); 
			}
			catch(BusinessValidationException be) {
				Map<String, String> errorMap = be.getErrors();
				boolean throwbe = false;
				if (errorMap.size() > 0) {
					throwbe = true;
					throw be;
				}
				Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
				for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet()) {
					if (entry.getKey().equals("ItemMoveValidator.MoveItemCabinetChanged")) {
						// edit the cabinet and expect a success
						newItemId = testEditMovedItemCabinetChanged(newItemId, true, new ArrayList<String>(), new ArrayList<String>()); 
					}
					else {
						Assert.fail("Add test to handle the warning message" + entry.getKey() + " / " + entry.getValue());
					}
				}
				if (throwbe) {
					throw be;
				}
			}
			
 		}
		finally {
			
			// TransactionSynchronizationManager.unbindResource(sf);
			if (-1 == newItemId) return;
			
			List<Long> itemIds = new ArrayList<Long>();
			itemIds.add(newItemId);
			itemHome.deleteItems(itemIds, userInfo);
			
			session.flush();
			session.clear();

			
		}
		
	}

	// @Test
	public void testWorkFlow() throws BusinessValidationException, DataAccessException {
		
		Request request = (Request) session.get(Request.class, 480L);
		// Request request1 = (Request) session.get(Request.class, 1515L);
		
		// List<Request> requests = Arrays.asList(request, request1);
		List<Request> requests = Arrays.asList(request);
		
		boolean requestByPass = true;
		Errors errors = getErrorObject();
		// requestManager.process(requests, getTestAdminUser(), errors, requestByPass);
		requestHome.processRequests(getTestAdminUser(), requests, errors);
		
		if (null != errors && errors.hasErrors()) {
			throwBusinessValidationException(errors, null);
		}
		
	}
	
	private void throwBusinessValidationException(Errors errors, String warningCallBack) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}
	
	@Test
	public final void testRequestViaService() throws BusinessValidationException, DataAccessException {
		
		List<Long> itemIds = Arrays.asList(853L);
		String typeOfRequest = "req_resubmit";
		
		
		UserInfo userInfo = FlexUserSessionContext.getUser();
		userInfo.setRequestBypass(true);
		itemService.itemRequest(itemIds, typeOfRequest);
		
		// itemHome.processRequest(requestDTOs);
		
		/*List<RequestDTO>  requestDTOs = itemHome.itemRequest(itemIds, typeOfRequest);

		itemHome.processRequest(requestDTOs);*/
		
		return;

	}
	
}

