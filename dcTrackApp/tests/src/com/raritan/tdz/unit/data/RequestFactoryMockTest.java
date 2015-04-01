package com.raritan.tdz.unit.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.unit.item.ItemMock;
import com.raritan.tdz.unit.item.request.RequestExpectations;
import com.raritan.tdz.unit.tests.UnitTestBase;

public class RequestFactoryMockTest extends UnitTestBase  {
	@Autowired
	ItemFactory itemFactMock;
	
	@Autowired 
	protected ItemMock itemMock;

	@Autowired
	RequestFactory requestFactMock;
	
	@Autowired
	RequestExpectations requestExpectations;
	
	@Test
	public void testCreateAnyRequest() throws Throwable {
		Item item = itemFactMock.createDevice(null, null);
		itemMock.addExpectations(item, jmockContext);
		
		Request request = requestFactMock.createRequestInstalled(item);
		requestExpectations.addExpectations(request, false);
		
		System.out.println(item.getItemName());
		System.out.println(item.getItemId());
		System.out.println(request.getRequestNo());
		System.out.println(request.getRequestId());
	}
}
