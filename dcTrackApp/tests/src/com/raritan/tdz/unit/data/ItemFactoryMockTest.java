package com.raritan.tdz.unit.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.unit.item.ItemMock;
import com.raritan.tdz.unit.tests.UnitTestBase;

public class ItemFactoryMockTest  extends UnitTestBase  {
	@Autowired
	ItemFactory itemFactMock;

	@Autowired 
	protected ItemMock itemMock;
    
	@Test
	public void testCreateAnyItem() throws Throwable {
		for(int i=0; i<5; i++){
			Item item = itemFactMock.createDevice(null, null);
			
			itemMock.addExpectations(item, jmockContext);
			
			System.out.println(item.getItemName());
			System.out.println(item.getItemId());
		}
		
	}
}
