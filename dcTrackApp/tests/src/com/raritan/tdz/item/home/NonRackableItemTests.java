package com.raritan.tdz.item.home;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Tests for the NonRackable tests 
 */
public class NonRackableItemTests extends TestBase {
	
	protected ItemService itemService;
	protected ItemDomainAdaptor itemAdaptor;
	
	/**
	 * @throws java.lang.Exception
	 */

	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemService = (ItemService)ctx.getBean("itemService");
		itemAdaptor = (ItemDomainAdaptor)ctx.getBean("itemDomainAdaptor");
	}
	
	//@Test
	public void getAvailUPositionsForItemTest() throws Throwable {
		long parenItemId = 3;
		int itemRUHeight = 2;
		int itemUPosition = -1;
		int blockSize = -1;
		String itemMounting = "Non-Rackable";
		long railsCode = SystemLookup.RailsUsed.FRONT;
		boolean placeDesktop = true;
		Collection<ValueIdDTO> uPos = itemService.getAvailUPositionsForNRItem(parenItemId, itemRUHeight, railsCode, -1);
		System.out.println("available u pos for NR item = " + uPos.toString());
		
		parenItemId = 3;
		itemRUHeight = 4;
		itemUPosition = -1;
		blockSize = -1;
		itemMounting = "Non-Rackable";
		railsCode = SystemLookup.RailsUsed.FRONT;
		placeDesktop = true;
		uPos = itemService.getAvailUPositionsForNRItem(parenItemId, itemRUHeight, railsCode, -1);
		System.out.println("available u pos for NR item = " + uPos.toString());

		parenItemId = 3;
		itemRUHeight = 1;
		itemUPosition = -1;
		blockSize = -1;
		itemMounting = "Non-Rackable";
		railsCode = SystemLookup.RailsUsed.FRONT;
		placeDesktop = true;
		uPos = itemService.getAvailUPositionsForNRItem(parenItemId, itemRUHeight, railsCode, -1);
		System.out.println("available u pos for NR item = " + uPos.toString());
	}
	
	//@Test
	public void getAvailableShelfPositionTest() {
		Long cabinetId = 3L;
		int uPosition = 24;
		long railsUsed = SystemLookup.RailsUsed.FRONT;
		long editItemId = -1;
		Collection<ValueIdDTO> shelfPos = itemService.getAvailableShelfPosition(cabinetId, uPosition, railsUsed, editItemId);
		System.out.println("available shelf for NR item = " + shelfPos.toString());
		
		cabinetId = 3L;
		uPosition = 24;
		railsUsed = SystemLookup.RailsUsed.FRONT;
		editItemId = 5185;
		shelfPos = itemService.getAvailableShelfPosition(cabinetId, uPosition, railsUsed, editItemId);
		System.out.println("available shelf for NR item = " + shelfPos.toString());
	}
	
	
	//@Test
	public void nonRackableShelfItemTests() throws InterruptedException  {
		long cabinetId = 4; //1B
		long uPosition = 9;
		long railsLkpValueCode = 8001; //front
		long itemId = 9951;
		
		//TODO: improve tests to create items on the fly for testing.
		
		// get list of items before change
		List<Item> itemsListBefore = getListOfNonRackableItemsOnShelf (cabinetId, uPosition, railsLkpValueCode);

		// Editing existing items
		// Move item to right from position 2 -> 5
		moveItemToRight_N_updateShelfPosition (cabinetId, uPosition, railsLkpValueCode, itemId, 5);
		
		Thread.sleep (2000);
		// Move item to left from position  5 -> 2 
		moveItemToLeft_N_updateShelfPosition (cabinetId, uPosition, railsLkpValueCode, itemId, 2);

		
		Thread.sleep (2000);
		// Move item 2 temporarily from the shelf (deleting item) 
		updateShelfPositionDeletedItem (cabinetId, uPosition, railsLkpValueCode, itemId, 2);

		Thread.sleep (2000);
		// put back item 2 back to shelf (adding item)
		updateShelfPositionAddItem (cabinetId, uPosition, railsLkpValueCode, itemId, 2);

		// get list of items after change
		List<Item> itemsListafter = getListOfNonRackableItemsOnShelf (cabinetId, uPosition, railsLkpValueCode);
		
		// Verify that the items positions are same after performing shelf test
	}

	//
	//<-- Private methods
	//

	private void moveItemToRight_N_updateShelfPosition (long cabinetId,
					long uPosition, long railsLkpValueCode, long itemId, int pos) {
		
		try {
			Item item = (Item)session.get(Item.class, itemId);
			item.setNumPorts(pos);
			session.update(item);
			session.flush();
			
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, item, null);
			session.flush();
		} catch (Throwable e) {
			System.out.println ("Failed to update shelf position \n");
			fail();
		}
	}

	private void moveItemToLeft_N_updateShelfPosition(long cabinetId,
					long uPosition, long railsLkpValueCode, long itemId, int pos) {
		
		try {
			Item item = (Item)session.get(Item.class, itemId);
			item.setNumPorts(pos);
			session.update(item);
			session.flush();
			
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, item, null);
			session.flush();
		} catch (Throwable e) {
			System.out.println ("Failed to update shelf position \n");
			fail();
		}
	}

	private void updateShelfPositionAddItem (long cabinetId,
			long uPosition, long railsLkpValueCode, long itemId, int pos) {
		
		try {
			Item cabinetItem = (Item)session.get(Item.class, cabinetId);
			//simulating the delete item by temporarily moving item to different cabinet.
			Item item = (Item)session.get(Item.class, itemId);
			item.setParentItem(cabinetItem);
			session.update(item);
			session.flush();
			
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, item, null);
			
		} catch (Throwable e) {
			System.out.println ("Failed to update shelf position \n");
			fail();
		}
	}
	
	private void updateShelfPositionDeletedItem (long cabinetId,
			long uPosition, long railsLkpValueCode, long itemId, int pos) {
		
		long newCabinetItemId = 9028;
		
		try {
			Item cabinetItem = (Item)session.get(Item.class, newCabinetItemId);
			//simulating the delete item by temporarily moving item to different cabinet.
			Item item = (Item)session.get(Item.class, itemId);
			item.setParentItem(cabinetItem);
			session.update(item);
			session.flush();
			
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, null, null);
			
		} catch (Throwable e) {
			System.out.println ("Failed to update shelf position \n");
			fail();
		}
	}
	
	private List<Item> getListOfNonRackableItemsOnShelf(long cabinetId, long uPosition, long railsLkpValueCode) {
		Session s = sf.openSession();
		Criteria c = s.createCriteria(Item.class);

		c.createAlias("model", "model", Criteria.LEFT_JOIN);
		c.createAlias("mountedRailLookup", "mountedRailLookup", Criteria.LEFT_JOIN);
		c.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		c.add(Restrictions.eq("parentItem.itemId", cabinetId));
		c.add(Restrictions.eq("uPosition", uPosition));
		c.add(Restrictions.eq("mountedRailLookup.lkpValueCode", railsLkpValueCode));
		c.add(Restrictions.eq("model.mounting", SystemLookup.Mounting.NON_RACKABLE));
		c.addOrder(Order.asc("numPorts"));
		c.addOrder(Order.desc("itemId"));
		return (List<Item>)c.list();
	}
	
	private List<ValueIdDTO> getValueIdDTOListForNewItem(String tiName, String serial,
					String assetTag, String eAssetTag, String uPosition, long status) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		valueIdDTOList.add(createValueIdDTOObj("cmbMake", 20L)); // HP
		valueIdDTOList.add(createValueIdDTOObj("cmbModel", 1L)); // Proliant DL320 G5p
		valueIdDTOList.add(createValueIdDTOObj("tiSerialNumber", serial));
		valueIdDTOList.add(createValueIdDTOObj("tiAssetTag", assetTag));
		valueIdDTOList.add(createValueIdDTOObj("tieAssetTag", eAssetTag));
		//Identity Panel Data
		valueIdDTOList.add(createValueIdDTOObj("tiName", tiName));
		valueIdDTOList.add(createValueIdDTOObj("tiAlias", "HELLO THERE!"));
		valueIdDTOList.add(createValueIdDTOObj("cmbType", 1L));
		valueIdDTOList.add(createValueIdDTOObj("cmbFunction", 970L));
		valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdmin", 20L));
		valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdminTeam", 555L));
		valueIdDTOList.add(createValueIdDTOObj("cmbCustomer", 566L));
		valueIdDTOList.add(createValueIdDTOObj("cmbStatus", status /*SystemLookup.ItemStatus.IN_STORAGE*/));
		// Placement Panel Data
		valueIdDTOList.add(createValueIdDTOObj("cmbLocation", 2L));
		valueIdDTOList.add(createValueIdDTOObj("radioRailsUsed", 8003L));
		valueIdDTOList.add(createValueIdDTOObj("cmbCabinet", 27L));
		valueIdDTOList.add(createValueIdDTOObj("cmbUPosition", uPosition));
		valueIdDTOList.add(createValueIdDTOObj("cmbOrientation", 7081L));
		valueIdDTOList.add(createValueIdDTOObj("tiLocationRef", "locRef"));
		// Custom fields
		valueIdDTOList.add(createValueIdDTOObj("tiCustomField", createCustomFields()));
		
		return valueIdDTOList;
 	}
	
}
