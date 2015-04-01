package com.raritan.tdz.item.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * 
 * @author bunty
 *
 */

public class ItemMoveInvalidMove extends ItemMoveTestBase {

	@SuppressWarnings("serial")
	public static final Map<String, TestOperations> invalidDataCollection  =
			Collections.unmodifiableMap(new HashMap<String, TestOperations>() {{
				
				// Device Std Rackable Archived
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.ARCHIVED,
						new TestOperations(3140L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemStatusSupportedForMove", "ItemValidator.mandatoryForState.movingItem"), new ArrayList<String>())
				); // AGFTP2_TO_BE_RETIRED 

				// Device Chassis Planned
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.BLADE_CHASSIS + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.PLANNED,
						new TestOperations(5014L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemStatusSupportedForMove"), new ArrayList<String>())
				); //  

				// Device Chassis Storage
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.STORAGE,
						new TestOperations(3303L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemStatusSupportedForMove"), new ArrayList<String>())
				); // NJ0PR04 

				// Rack PDU, ZeroU
				put(
						new Long(SystemLookup.Class.RACK_PDU).toString() + SystemLookup.Mounting.ZERO_U + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(4913L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove", "ItemMoveValidator.ItemClassSupportedForMove"), new ArrayList<String>())
				); // 1A-RPDU-L, Error

				// Rack PDU, Rackable
				put(
						new Long(SystemLookup.Class.RACK_PDU).toString() + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(4153L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove", "ItemMoveValidator.ItemClassSupportedForMove"), new ArrayList<String>())
				); // 1A-RPDU-L, Error

				// Device Free-Standing
				/*put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(5225L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove"), new ArrayList<String>())
				); // USNJ0TL2 */
				
				// Network Free-Standing
				/*put(
						new Long(SystemLookup.Class.NETWORK).toString() + SystemLookup.Mounting.FREE_STANDING + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(3196L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove"), new ArrayList<String>())
				); // USNJ0TL2 */
				
				// Power Outlet Non-Rackable Installed
				put(
						new Long(SystemLookup.Class.FLOOR_OUTLET).toString() + SystemLookup.Mounting.NON_RACKABLE + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(4912L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove", "ItemMoveValidator.ItemClassSupportedForMove"), new ArrayList<String>())
				); // PDU1/PB1:1
				
				// Power Outlet, Busway, Installed
				put(
						new Long(SystemLookup.Class.FLOOR_OUTLET).toString() + SystemLookup.Mounting.BUSWAY + SystemLookup.ItemStatus.INSTALLED,
						new TestOperations(4338L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemMountingSupportedForMove", "ItemMoveValidator.ItemClassSupportedForMove"), new ArrayList<String>())
				); // S05-A1
				
			}});

	@SuppressWarnings("serial")
	public static final Map<String, TestOperations> invalidDataCollection1  =
			Collections.unmodifiableMap(new HashMap<String, TestOperations>() {{
				
				// Device Std Rackable Archived
				put(
						new Long(SystemLookup.Class.DEVICE).toString() + SystemLookup.SubClass.RACKABLE + SystemLookup.Mounting.RACKABLE + SystemLookup.ItemStatus.ARCHIVED,
						new TestOperations(3140L, SystemLookup.MoveAction.DONT_CONNECT, SystemLookup.MoveAction.DONT_CONNECT, -1L, Arrays.asList("ItemMoveValidator.ItemStatusSupportedForMove", "ItemValidator.mandatoryForState.movingItem"), new ArrayList<String>())
				); // AGFTP2_TO_BE_RETIRED 
				
			}});


	@DataProvider(name = "invalidData")
	public Object[][] invalidDataProviders() {
		
		int dataSize = invalidDataCollection.size();
		
		Object[][] dataObjects = new Object[dataSize][2];
		
		int index = 0;
		for (Map.Entry<String, TestOperations> entry: invalidDataCollection.entrySet()) {
			dataObjects[index][0] = entry.getKey();
			dataObjects[index][1] = entry.getValue();
			index++;
		}
		
		return dataObjects;

	}

	@Test(dataProvider="invalidData", expectedExceptions=BusinessValidationException.class)
	public void testAllInValidMove(String testedOn, TestOperations testData) throws Throwable {
		
		// This is required because of timeOut, if no timeout is used, this can be removed
		// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		
		try {
			// Get item details
			Item item = itemDAO.getItem(testData.getMovingItemId());
	
			Long newItemId = -1L; 
					
			// initiate the move
			newItemId = testNewItemWithMove(item, testData.getErrorCode(), testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode(), testData.getNewUPosition(), true, testData.getWarningCode());
		
			// validate the save of moved item
			validateItemMove(testData.getMovingItemId(), newItemId, testData.getPowerPortMoveActionLkpValueCode(), testData.getDataPortMoveActionLkpValueCode());
			
			if (-1 == newItemId) {
				return;
			}
			// clean the when moved item
			List<Long> itemIds = new ArrayList<Long>();
			itemIds.add(newItemId);
			itemHome.deleteItems(itemIds, userInfo);
		}
		finally {
  			// TransactionSynchronizationManager.unbindResource(sf);
		}
		
	}
	
}

