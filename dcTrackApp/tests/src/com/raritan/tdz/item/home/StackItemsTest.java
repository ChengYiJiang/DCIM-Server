/**
 * 
 */
package com.raritan.tdz.item.home;

 import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dto.StackItemDTO;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.tests.TestBase;


public class StackItemsTest extends TestBase {
	
	protected ItemService itemService;
	protected ItemDomainAdaptor itemAdaptor;
	
	
	protected UnitTestItemDAO unitTestItemDAO;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemService = (ItemService)ctx.getBean("itemService");
		itemAdaptor = (ItemDomainAdaptor)ctx.getBean("itemDomainAdaptor");
		unitTestItemDAO = (UnitTestItemDAO) ctx.getBean("unitTestItemDAO");
	}

	@Test
	public void getAddableStackItems() throws Throwable {
		try {
			List<StackItemDTO> stackableList = itemService.getAddableStackItems();
			System.out.println ("\n\n ######### getAddableStackItems() count = " + stackableList.size() + "\n");
			
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}			
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getAllStackItems() throws Throwable {
		Long itemId = 3913L; /* assumes this item exists in the database */
		// TODO: dont hardcode item id.
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( itemId );
			System.out.println ("\n\n ######### getAllStackItems() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	//@Test
	public void removeStackItem() throws Throwable {

		//TODO: dont hardcode item id
		long itemId = 3930;
		String newName = "USROSS4-4I-SCR-seven";
		System.out.println ("\n\n ######### removeStackItem() ##########\n");
		try {
			/* remove the non-primary item from the network stack */
			itemId = 3930;
			newName = "USROSS4-4I-SCR-seven";
			List<StackItemDTO> stackableList = itemService.removeStackItem(itemId, newName);
			System.out.println ("StackableList count = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}
			// add the item back to the network stack
			long siblingItemId = 3925;
			System.out.println ("Try to add an invalid item id. this should do nothing to the list");
			stackableList = itemService.addStackItem(itemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}
			
			/* remove the primary item from the network stack */
			itemId = 3925;
			newName = "USROSS4-4I-SCR-00";
			stackableList = itemService.removeStackItem(itemId, newName);
			System.out.println ("StackableList count = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}
			// add the item back to the network stack. However, it will not become the primary item
			siblingItemId = 3926;
			System.out.println ("Try to add an invalid item id. this should do nothing to the list");
			stackableList = itemService.addStackItem(itemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}
			
			/* remove the primary item and is the only item in the stack */
			itemId = 103;
			newName = "SAN Switch 1-01";
			stackableList = itemService.removeStackItem(itemId, newName);
			System.out.println ("StackableList count = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}

		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addInvalidItemToStackTest() throws Throwable {
		Long siblingItemId = 3913L; /* assumes this item exists in the database */
		Long newItemId = -1L;
		// TODO: dont hardcode item id.
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + " name: " + dto.getName());
			}

			// Try to add an invalid item id
			System.out.println ("Try to add an invalid item id. this should do nothing to the list");
			stackableList = itemService.addStackItem(newItemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			boolean itemadded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemadded = true; 
			}
			assertFalse(itemadded, "added invalid network item to stack");
			
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// Expected to fail just report business validation error
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void addExistingSiblingToStackTest() throws Throwable {
		Long siblingItemId = 3913L; /* assumes this item exists in the database */
		Long newItemId = -1L;
		// TODO: dont hardcode item id.
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### addExistingSiblingToStackTest() - StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName());
			}

			// Try to add the same sibling item to the list. It should do nothing
			System.out.println ("Try to add the same sibling item to the list. It should do nothing");
			newItemId = siblingItemId;
			stackableList = itemService.addStackItem(newItemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			boolean itemadded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemadded = true; 
			}
			assertFalse(itemadded, "added existing sibling item to stack");
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected to fail because the item is already part of stack 
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addExistingStackItemToStackTest() throws Throwable {
		long siblingItemId = 3913L; /* assumes this item exists in the database */
		long newItemId = -1L;
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### addExistingStackItemToStackTest() -StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if ((dto.getId().longValue() != dto.getsiblingId().longValue() && (newItemId == -1))) {
					System.out.println(">>>> dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
					newItemId = dto.getId();
				}
			}
			
			// Try to add an item that is part of the same group. It should do nothing
			System.out.println ("Try to add an item ("+ newItemId + ") that is part of the same group. It should do nothing");
			//newItemId = 3917L;
			stackableList = itemService.addStackItem(newItemId, siblingItemId);
			boolean itemadded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemadded = true; 
			}
			assertFalse(itemadded, "added existing stack Item to stack");
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected to fail if we are adding existing stack item.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addNonStackItemToStackTest() throws Throwable {
		long siblingItemId = 3913L; /* assumes this item exists in the database */
		long newItemId = 3041L; /* NOT A NETWORK ITEM */
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### addNonStackItemToStackTest() - StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
			}
			
			// Try to add an item that is not a network item. It should do nothing
			System.out.println ("Try to add an item that is not a network item. It should do nothing");
			stackableList = itemService.addStackItem(newItemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			boolean itemadded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemadded = true; 
			}
			assertFalse(itemadded, "added non stack Item to stack");
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected to fail when trying to add invalid stack item to sack.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addValidNetworkItemToStackTest() throws Throwable {
		long siblingItemId = 3913L; /* assumes this item exists in the database */
		long newItemId = 103L; /* VALID NETWORK ITEM */
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### addValidNetworkItemToStackTest() - StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
			}
			
			// Try to add an item which is a network item. It should be added, with the new name and sibling info updated
			System.out.println ("Try to add an item which is a network item. It should be added, with the new name and sibling info updated");
			newItemId = 103L;
			stackableList = itemService.addStackItem(newItemId, siblingItemId);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			// Verify if the item is added succesfully
			boolean itemSuccesfullyAdded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemSuccesfullyAdded = true; 
			}
			assertTrue(itemSuccesfullyAdded, "Failed to add new network item to stack");
			
			
			// Remove the item just added from the list. This the keep the list as it was originally
			System.out.println ("Remove the item just added from the list. This is to keep the list as it was originally");
			String newName = "SAN Switch 1-01";
			stackableList = itemService.removeStackItem(newItemId, newName);
			System.out.println ("StackableList count = " + stackableList.size());
			
			//verify if the item is removed succesfully
			boolean itemFound = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemFound = true; 
			}
			assertFalse(itemFound, "Failed to remove and item from stack");

		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addInvalidPrimaryItemToStackTest() throws Throwable {
		long siblingItemId = 3913L; /* assumes this item exists in the database */
		long newItemId = 505L; /* INVALID NETWORK PRIMARY ITEM */
		try {
			List<StackItemDTO> stackableList = itemService.getAllStackItems( siblingItemId );
			System.out.println ("\n\n ######### addInvalidPrimaryItemToStackTest() - StackableList count before adding new sibling = " + stackableList.size());
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
			}
			
			// Try to add an item which is a network item to the wrong primary item. It should not be added
			System.out.println ("Try to add an item which is a network item to the wrong primary item. It should not be added");
			
			stackableList = itemService.addStackItem(newItemId, 3914);
			System.out.println ("StackableList count before adding new sibling = " + stackableList.size());
			boolean itemadded = false;
			for (StackItemDTO dto: stackableList) {
				System.out.println("dto = " + "id: " + dto.getId() +" name: " + dto.getName() + "siblingId = " + dto.getsiblingId() );
				if (newItemId == dto.getId()) itemadded = true; 
			}
			assertFalse(itemadded, "added InvalidPrimaryItem to stack");
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected to fail when trying to add invalid primary item
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	

	@Test
	public void setStackNameFailureCase() throws Throwable {
		Long siblingItemId = 3919L;
		try {
			System.out.println ("\n\n ######### setStackNameFailureCase() ######### \n");
			Map<String, UiComponentDTO> componentDTOMap  = itemService.getItemDetails(siblingItemId);
			UiComponentDTO nameDto = componentDTOMap.get("tiName");
			String originalStackName = (String)nameDto.getUiValueIdField().getValue();
			int idx = originalStackName.lastIndexOf('-');
			if (idx > 0) {
				originalStackName = originalStackName.substring(0, idx);
			}			
			
			List<StackItemDTO> stackableList = itemService.setStackName( siblingItemId, originalStackName );
			System.out.println ("\n\n ######### setStackName() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected because item name already exists.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void setStackName() throws Throwable {
		Long siblingItemId = 3919L;
		String newStackName = "UnitTesting";
		try {
			System.out.println ("\n\n ######### setStackName() ######### \n");
			
			Map<String, UiComponentDTO> componentDTOMap  = itemService.getItemDetails(siblingItemId);
			UiComponentDTO nameDto = componentDTOMap.get("tiName");
			String originalStackName = (String)nameDto.getUiValueIdField().getValue();
			int idx = originalStackName.lastIndexOf('-');
			if (idx > 0) {
				originalStackName = originalStackName.substring(0, idx);
			}
			
			List<StackItemDTO> stackableList = itemService.setStackName( siblingItemId, newStackName );
			System.out.println ("\n\n ######### Set new stackname - setStackName() count = " + stackableList.size() + "\n");
			String name = null; 

			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
				
				if (name == null) name = dto.getName();
			}
			
			// verify if the stack name is set correctly as expected 
			idx = name.lastIndexOf('-');
			if (idx > 0) {
				name = name.substring(0, idx);
			}
			assertTrue((newStackName.equals(name) == true) , "stack stack name is not set correctly");
			
			// set the item name back to original 
			stackableList = itemService.setStackName( siblingItemId, originalStackName );
			System.out.println ("\n\n ######### setting original name - setStackName() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void setStackItemNumberFailureCase() throws Throwable {
		Long siblingItemId = 3913L;
		Long newStackNumber=1L;
		try {
			List<StackItemDTO> stackableList = itemService.setStackNumber( siblingItemId, newStackNumber );
			System.out.println ("\n\n ######### setStackItemNumberFailureCase() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected because item name already exists.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}
	
	@Test
	public void setStackItemNumberLowerBoundryFailureCase() throws Throwable {
		Long siblingItemId = 3913L;
		Long newStackNumber=0L;
		try {
			List<StackItemDTO> stackableList = itemService.setStackNumber( siblingItemId, newStackNumber );
			System.out.println ("\n\n ######### setStackItemNumberLowerBoundryFailureCase() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected because stack # invalid.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}

	@Test
	public void setStackItemNumberUpperBoundryFailureCase() throws Throwable {
		Long siblingItemId = 3913L;
		Long newStackNumber=65L;
		try {
			List<StackItemDTO> stackableList = itemService.setStackNumber( siblingItemId, newStackNumber );
			System.out.println ("\n\n ######### setStackItemNumberUpperBoundryFailureCase() count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected because stack # invalid.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}

	@Test
	public void setStackItemNumber() throws Throwable {
		Long siblingItemId = 3920L;
		Long OriginalStackNumber=8L; 
		Long newStackNumber=64L;
		try {
			List<StackItemDTO> stackableList = itemService.setStackNumber( siblingItemId, newStackNumber );
			System.out.println ("\n\n ######### setStackItemNumber() ItemId =" + siblingItemId + " count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}

			stackableList = itemService.setStackNumber( siblingItemId, OriginalStackNumber );
			System.out.println ("\n\n ######### setStackItemNumber() ItemId =" + siblingItemId + " count = " + stackableList.size() + "\n");
			for (StackItemDTO dto: stackableList) {
				System.out.println( "StockDto: id = " + dto.getId() +
						", siblingId = " + dto.getsiblingId() +
						", name = " + dto.getName() +
						", modelName = " + dto.getModelName() +
						", cabinetName = " + dto.getCabinetName() +
						", UPosition = " + dto.getUPosition() +
						", status = " + dto.getStatus());
			}

			
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			// expected because item name already exists.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}
	

	
	//@Test
	public void testCreateSingleStackable() throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long cabinetId = new Long(-1);
		Long itemId = new Long(-1);
		DataCenterLocationDetails location = null;
		try {
			
			//TODO: This can be put into setup method of the test base.
			location = unitTestItemDAO.createUnitTestLocation();
			
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", null);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);
			
			Map<String, UiComponentDTO> itemDTO = unitTestItemDAO.createStackableItem(location.getDataCenterLocationId(), cabinetId, 1, "PowerConnect 6248", null);
			itemId = (Long) ((UiComponentDTO)itemDTO.get("tiName")).getUiValueIdField().getValueId();
			assertTrue(itemId > 0);
			
			Criteria criteria = session.createCriteria(Item.class);
			criteria.add(Restrictions.eq("itemId", itemId));
			
			List list = criteria.list();
			
			assertTrue(list.size() == 1);
			
			Item savedItem = (Item) list.get(0);
			
			assertEquals(savedItem.getItemName(), savedItem.getCracNwGrpItem().getItemName(),"The item name and logical name should be same, however it is not: itemName = " + 
							savedItem.getItemName() + " logicalName = " + savedItem.getCracNwGrpItem().getItemName());

		} finally {
			//Then delete the stackable, cabinet and location.
			unitTestItemDAO.deleteItem(itemId);
			unitTestItemDAO.deleteItem(cabinetId);
			unitTestItemDAO.deleteUnitTestLocation(location);
		}
	}
	
	
	//@Test
	public void testCreateMultipleStackable() throws BusinessValidationException, ClassNotFoundException, Throwable{
		Long cabinetId = new Long(-1);
		List<Long> itemIds = new ArrayList<Long>();
		DataCenterLocationDetails location = null;
		try {
			final String stackModelName = "PowerConnect 6248";
			ModelDetails modelDetails = getModel(stackModelName);
			int ruHeight = modelDetails.getRuHeight();

			//Create a location
			location = unitTestItemDAO.createUnitTestLocation();
			
			//Create a cabinet
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", null);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);

	
			//Create primary item
			Map<String, UiComponentDTO> itemDTO = unitTestItemDAO.createStackableItem(location.getDataCenterLocationId(), cabinetId, 1, stackModelName, null);
			itemIds.add((Long) ((UiComponentDTO)itemDTO.get("tiName")).getUiValueIdField().getValueId());
			assertTrue(itemIds.get(0) > 0);

			//Check to see if the itemName and logical Name are same
			Criteria criteria = session.createCriteria(Item.class);
			criteria.add(Restrictions.eq("itemId", itemIds.get(0)));
			
			List list = criteria.list();
			
			assertTrue(list.size() == 1);
			Item savedItem = (Item) list.get(0);
			assertEquals(savedItem.getItemName(), savedItem.getCracNwGrpItem().getItemName(),"The item name and logical name should be same, however it is not: itemName = " + 
					savedItem.getItemName() + " logicalName = " + savedItem.getCracNwGrpItem().getItemName());
		
			//Create multiple stack item and add to the original
			
			long uPosition = 1;
			for (int i = 1; i < 3; i++){
				uPosition += ruHeight;
				Map<String, UiComponentDTO> itemDTOResult = unitTestItemDAO.createStackableItem(location.getDataCenterLocationId(), cabinetId, uPosition, stackModelName, null);
				itemIds.add((Long) ((UiComponentDTO)itemDTOResult.get("tiName")).getUiValueIdField().getValueId());
				assertTrue(itemIds.get(i) > 0);
				//We have to update the tiNoIn group as we are adding another stack item to the original
			
				
				itemHome.addStackItem(itemIds.get(i), itemIds.get(0));
				
				itemDTO = unitTestItemDAO.updateValue("tiNoInGroup",itemDTO,i+1, false);
				itemDTO = unitTestItemDAO.updateValue("tiClass",itemDTO,itemDTO.get("tiClass").getUiValueIdField().getValue(),false);
				List<ValueIdDTO> valueIdDTOList = unitTestItemDAO.getItemUpdateValueIdDTOList(itemDTO);
				unitTestItemDAO.updateItem(valueIdDTOList);
			}
			
			//Let us propogate the values such as alias, customer,etc to other dtos.
			itemDTO = unitTestItemDAO.updateValue("cbPropagate", itemDTO, Boolean.TRUE, false);
			Long lkuId = new Long(-1);
			if ( (lkuId = getLkuId("TYPE", modelDetails.getClassLookup().getLkpValueCode(), 1)) > 0){
				itemDTO = unitTestItemDAO.updateValue("cmbType", itemDTO, lkuId, true);
			}
			List<ValueIdDTO> valueIdDTOList = unitTestItemDAO.getItemUpdateValueIdDTOList(itemDTO);
			Map<String,UiComponentDTO> origItemDTO = unitTestItemDAO.updateItem(valueIdDTOList);
			
			//Check if the values are propogated. For example Type field
			
			Map<String,UiComponentDTO> stackItemDTO = unitTestItemDAO.getItemDTO((Long)itemIds.get(1));
			
			if (origItemDTO.get("cmbType").getUiValueIdField() != null){
				assertNotNull (stackItemDTO.get("cmbType").getUiValueIdField());
				assertNotNull (origItemDTO.get("cmbType").getUiValueIdField());
				assertEquals(stackItemDTO.get("cmbType").getUiValueIdField().getValueId(), origItemDTO.get("cmbType").getUiValueIdField().getValueId(), "The customer in the original should be same as the first stack item");
			}

		} catch (Exception t){
			 throw t;
		}finally {
			//Then delete the stackable, cabinet and location.
			Collections.reverse(itemIds);
			for (Long itemId: itemIds)
				unitTestItemDAO.deleteItem(itemId);
			unitTestItemDAO.deleteItem(cabinetId);
			unitTestItemDAO.deleteUnitTestLocation(location);
		}
	}
	
	private long getLkuId(String type, Long classLkpValueCode, int index){
		long lkuId = -1;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(LkuData.class);
		criteria.createAlias("lksData", "lksData");
		criteria.add(Restrictions.eq("lkuTypeName", type));
		criteria.add(Restrictions.eq("lksData.lkpValueCode", classLkpValueCode));
		
		List list = criteria.list();
		if (list.size() >= index){
			LkuData lkuData = (LkuData) list.get(index);
			lkuId = lkuData.getLkuId();
		}
		
		return lkuId;
	}
	
	private ModelDetails getModel(String modelName) {
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelName", modelName));
		
		ModelDetails modelDetails = (ModelDetails) criteria.uniqueResult();
		
		return modelDetails;
	}
	
	//TODO: create stack item instead of hardcoding itemIds.
	//      add more tests to handle all positive/negative tests.

}
