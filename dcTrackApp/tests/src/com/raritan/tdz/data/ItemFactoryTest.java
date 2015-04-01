package com.raritan.tdz.data;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.tests.TestBase;

public class ItemFactoryTest  extends TestBase  {
	ItemFactory itemFact;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		itemFact = (ItemFactory)ctx.getBean("itemFact");
	}
	
	@Test
	public void testCreateAnyItem() throws Throwable {
		for(int i=0; i<5; i++){
			Item item = itemFact.createDevice(null, null);
			
			System.out.println(item.getItemName());
			System.out.println(item.getItemId());
		}	
		this.addTestItemList(itemFact.getCreatedItemList());
	}
}
