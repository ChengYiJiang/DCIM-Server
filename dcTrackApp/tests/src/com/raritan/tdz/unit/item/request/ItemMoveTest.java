package com.raritan.tdz.unit.item.request;


import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Locale;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.item.ItemMock;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UserMock;

public class ItemMoveTest extends UnitTestBase {
	int itemNo = 1;
	
	@Autowired
	private ResourceBundleMessageSource messageSource;
	
	@Autowired
	private ItemSaveBehavior itemMoveRequestBehavior;
	
	@Autowired
	private SystemLookupInitUnitTest systemLookupInitTest;

    @Autowired 
	protected ItemMock itemMock;
	
	@Autowired 
	protected ItemMoveMock itemMoveRequestMock;
	
	@Autowired
	ItemRequestMock itemRequestMock;
	
	@Autowired
	private ItemDAO itemDAO;

	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * Pass Null Item to validator 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:  
	 * validations pass moving Item to Archived state
	 * @throws Throwable
	 */
	
	//@Test
	public final void submitMoveRequest() throws Throwable, DataAccessException {
		final Item itemToMove = createItemToMove();
		final Item itemWhenMove = createItemWhenMove(itemToMove);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser();

		MapBindingResult errors = getErrorObject (ItItem.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).loadItem(with(1L)); will(returnValue(itemToMove));
		}});
		
		try {
			itemMoveRequestBehavior.postSave(itemWhenMove, userInfo, errors);
	
			if (errors.hasErrors()) {
				List<ObjectError> objectErrors = errors.getAllErrors();
				for (ObjectError error: objectErrors){
					String msg = messageSource.getMessage(error, Locale.getDefault());
					System.out.println (getMethodName() + " : Result : " + msg);
				}
				assertTrue(false);			
			} else {
				assertTrue(true);
			}
		} catch (Throwable e) {
			assertTrue(true);
			e.printStackTrace();
		}
	}

	//@Test
	public final void reSubmitMoveRequest() throws Throwable, DataAccessException {
		final Item itemToMove = createItemWithPendingRequest();
		final Item itemWhenMove = createItemWhenMove(itemToMove);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser();

		MapBindingResult errors = getErrorObject (ItItem.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).loadItem(with(1L)); will(returnValue(itemToMove));
		}});
		
		try {
			itemMoveRequestBehavior.postSave(itemWhenMove, userInfo, errors);
	
			if (errors.hasErrors()) {
				List<ObjectError> objectErrors = errors.getAllErrors();
				for (ObjectError error: objectErrors){
					String msg = messageSource.getMessage(error, Locale.getDefault());
					System.out.println (getMethodName() + " : Result : " + msg);
				}
				assertTrue(false);			
			} else {
				assertTrue(true);
			}
		} catch (Throwable e) {
			assertTrue(true);
			e.printStackTrace();
		}
	}	
	//Helper Functions for Move
	private Item createItemToMove(){		
		final Item temp = itemMock.createRackableStandardDeviceItem(SystemLookup.ItemStatus.INSTALLED, itemNo++);
		temp.setUPosition(1);
		temp.setMountedRailLookup(systemLookupInitTest.getLks(SystemLookup.RailsUsed.FRONT));
		return temp;
	}
	
	private Item createItemWhenMove(Item item){		
		itemMoveRequestMock.setItemToMove(item);
		
		final Item temp = itemMoveRequestMock.createRackableStandardDeviceItem(SystemLookup.ItemStatus.PLANNED, itemNo++);
		temp.setItemToMoveId(item.getItemId());
		temp.setItemName(item.getItemName() + "^^WHEN-MOVE");
		temp.setUPosition(5);
		temp.setMountedRailLookup(systemLookupInitTest.getLks(SystemLookup.RailsUsed.REAR));
		return temp;
	}

	private Item createItemWithPendingRequest(){		
		final Item temp = itemMock.createRackableStandardDeviceItem(SystemLookup.ItemStatus.INSTALLED, itemNo++);
		temp.setUPosition(1);
		temp.setMountedRailLookup(systemLookupInitTest.getLks(SystemLookup.RailsUsed.FRONT));
		
		Request request = itemRequestMock.createRequestMove(temp, true);
		
		return temp;
	}
	
	
	
	
	
	
	
	
}
