/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.request.dao.*;

public class RequestDaoTest extends TestBase {
	RequestDAO requestDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		requestDAO = (RequestDAO)ctx.getBean("requestDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void testGetAssociatedRequestIdsForRequest() throws Throwable {
		Request request = new Request();
		request.setRequestId(265);
		request.setRequestNo("1400034");
		
		List<Long> recList = requestDAO.getAssociatedRequestIdsForRequest(request);
		
		for(Long id:recList){
			System.out.println(id);					
		}
	}

	@Test
	public final void testGetAssociatedPendingReqsForReq() throws Throwable {
		Request request = new Request();
		request.setRequestId(577);
		request.setRequestNo("1400053");
		
		List<Request> recList = requestDAO.getAssociatedPendingReqsForReq(request);
		
		for(Request r:recList){
			System.out.println(r.getRequestNo());					
		}
	}	
}





















