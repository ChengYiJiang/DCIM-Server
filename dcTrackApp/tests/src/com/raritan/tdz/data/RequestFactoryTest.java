package com.raritan.tdz.data;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.tests.TestBase;

public class RequestFactoryTest  extends TestBase  {
	RequestFactory requetFact;
	ItemFactory itemFact;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemFact = (ItemFactory)ctx.getBean("itemFact");
		requetFact = (RequestFactory)ctx.getBean("requestFact");
	}
	
	@Test
	public void testCreateAnyRequest() throws Throwable {
		Item item = itemFact.createDevice(null, null);
		Request request = requetFact.createRequestInstalled(item);
			
		System.out.println(item.getItemName());
		System.out.println(item.getItemId());
		System.out.println(request.getRequestNo());
		System.out.println(request.getRequestId());
		
		this.addTestItemList(itemFact.getCreatedItemList());
	}
}
