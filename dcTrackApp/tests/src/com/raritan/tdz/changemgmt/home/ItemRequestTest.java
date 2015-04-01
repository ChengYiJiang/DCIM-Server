package com.raritan.tdz.changemgmt.home;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestValidationAspect;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Change management tests.
 * @author Santo Rosario
 */
public class ItemRequestTest extends TestBase {
	private ItemRequest itemRequest;
	private UserInfo testUser;
	private ItemRequestValidationAspect validationAspect;
	
	protected UnitTestItemDAO unitTestItemDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemRequest = (ItemRequest)ctx.getBean("itemRequest");
		testUser = this.getTestAdminUser();
		unitTestItemDAO = (UnitTestItemDAO) ctx.getBean("unitTestItemDAO");
		validationAspect = (ItemRequestValidationAspect) ctx.getBean("itemRequestValidationAspect");
		validationAspect.setDisableValidate(true);
	}

	private class RequestData {
		public Item item; 
		public long requestId;
		public String requestNumber;
		public String requestDescription;
	};
	
	private void updateItemRequestStageToWorkOrderIssued (RequestData rd) throws Throwable {
		Long requestId = rd.requestId;

		// update original request stage fro install request to workOrder issued through SQL. 
		// Normally you move to next stage from CV.
		
		Session localSession = sf.openSession();
		Query updateQuery = localSession.createSQLQuery(
				"UPDATE tblrequesthistory set current = false where requestid = :id");
		updateQuery.setParameter("id", requestId);

		@SuppressWarnings("unused")
		int updated = updateQuery.executeUpdate();
		localSession.flush();
		
		// move to workorder issued stage by adding new entry into requesthistory table. 
		
		Query insertQuery = localSession.createSQLQuery(
				"INSERT into tblrequesthistory (requestid, requestedby, requestedon, stageid, current, comment) VALUES(:requestid, 'dct30', '2013-03-03 23:57:57-05',935, true, 'Test' )");
		insertQuery.setParameter("requestid", requestId);

		@SuppressWarnings("unused")
		int inserted = insertQuery.executeUpdate();
		localSession.flush();			
		localSession.close();
	}

	private String buildExpectedErrorMsg (RequestData rd) {
		StringBuilder expectedMsg = new StringBuilder();
		expectedMsg.append(rd.item.getItemName());
		expectedMsg.append(", Request failed, Item has outstanding request.\n");
		expectedMsg.append("Request Number: ");
		expectedMsg.append(rd.requestNumber);
		expectedMsg.append(", ");
		expectedMsg.append(rd.requestDescription);
		expectedMsg.append(" \nTo update the request use \"Resubmit Request\".");

		return expectedMsg.toString();
	}
	
	private String buildExpectedErrorIssueMsg (RequestData rd) {
		StringBuilder expectedMsg = new StringBuilder();
		expectedMsg.append(rd.item.getItemName());
		expectedMsg.append(", Request could not be issued, Item has outstanding request.\n");
		expectedMsg.append("Request Number: ");
		expectedMsg.append(rd.requestNumber);
		expectedMsg.append(", ");
		expectedMsg.append(rd.requestDescription);
		expectedMsg.append(" \nTo update the request use \"Resubmit Request\".");

		return expectedMsg.toString();
	}
	
	@Test
	public final void testInstallItemRequest() throws Throwable {
		InstallItemRequest();
	}
	
	@Test
	public final void testInstallItemRequestWhilePending_Message() throws Throwable {
		RequestData rd = InstallItemRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.installItemRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}

	private RequestData InstallItemRequest() throws Throwable{
		ItItem item = this.createNewTestDevice("NewItemRequestTest-001", SystemLookup.ItemStatus.PLANNED);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		Map<Long,Long> request = itemRequest.installItemRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		Request r = (Request)session.get(Request.class, requestId);
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}

	@Test
	public final void testBringItemOnSiteRequest() throws Throwable {
		clearRequest(bringItemOnSiteRequest());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public final void testBringItemOnSiteRequestWhilePending_Message() throws Throwable {
		RequestData rd = bringItemOnSiteRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.bringItemOnsiteRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	

	private RequestData bringItemOnSiteRequest() throws Throwable {
		ItItem item = this.createNewTestDevice("BringItemOnSite-001", SystemLookup.ItemStatus.OFF_SITE);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
				
		Map<Long,Long> request = itemRequest.bringItemOnsiteRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));

		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;

	}
	
	@Test
	public final void testPowerOffItemRequest() throws Throwable {
		clearRequest(powerOffItemRequest());
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void ttestPowerOffItemRequestWhilePending_Message() throws Throwable {
		RequestData rd = powerOffItemRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			
			Map<Long,Long> request = itemRequest.powerOffItemRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	private RequestData powerOffItemRequest() throws Throwable {
		ItItem item = this.createNewTestDevice("PowerOffItemRequestTest-001", SystemLookup.ItemStatus.INSTALLED);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		//itemIds.add(304L);
				
		Map<Long,Long> request = itemRequest.powerOffItemRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;

	}

	@Test
	public final void testTakeItemOffSiteRequest() throws Throwable {
		clearRequest(takeItemOffSiteRequest());
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testTakeItemOffSiteRequestWhilePending_Message() throws Throwable {
		RequestData rd = takeItemOffSiteRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			
			Map<Long,Long> request = itemRequest.takeItemOffsiteRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	
	private RequestData takeItemOffSiteRequest() throws Throwable {
		ItItem item = this.createNewTestDevice("NewItemRequestTest-001", SystemLookup.ItemStatus.INSTALLED);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		Map<Long,Long> request = itemRequest.takeItemOffsiteRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}
	
	@Test
	public final void testPowerOnItemRequest() throws Throwable {
		clearRequest(powerOnItemRequest());
	}	

	@SuppressWarnings("deprecation")
	@Test
	public final void testPowerOnItemRequestWhilePending_Message() throws Throwable {
		RequestData rd = powerOnItemRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.powerOnItemRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	private RequestData powerOnItemRequest() throws Throwable {
		ItItem item = this.createNewTestDevice("NewItemRequestTest-001", SystemLookup.ItemStatus.POWERED_OFF);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());

		Map<Long,Long> request = itemRequest.powerOnItemRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}

	@Test
	public final void testConvertToVMRequest() throws Throwable {
		clearRequest(convertToVMRequest());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public final void testConvertToVMRequestWhilePending_Message() throws Throwable {
		RequestData rd = convertToVMRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.convertToVMRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	private RequestData convertToVMRequest() throws Throwable {
		//vm item request
		ItItem item = this.createNewTestDevice("ConvertToVMRequestTest-001", SystemLookup.ItemStatus.INSTALLED);
		//ItItem item = (ItItem)this.getItem(150L);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		Map<Long,Long> request = itemRequest.convertToVMRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		Request r = (Request)session.get(Request.class, requestId);
		
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}

	@Test
	public final void testDecommisionItemToArchiveRequest() throws Throwable {
		clearRequest(decommisionItemToArchiveRequest());
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testDecommisionItemToArchiveRequestWhilePending_Message() throws Throwable {
		RequestData rd = decommisionItemToArchiveRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.decommisionItemToArchiveRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	private RequestData decommisionItemToArchiveRequest() throws Throwable {
		//vm item request
		ItItem item = this.createNewTestDevice("DiscardItemRequestTest-001", SystemLookup.ItemStatus.INSTALLED);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		Map<Long,Long> request = itemRequest.decommisionItemToArchiveRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}	

	@Test
	public final void testPlaceItemInStorageRequest() throws Throwable {
		RequestData rd = null;
		try {
			rd  = placeItemInStorageRequest();
		}
		finally {
			
			if (null != rd && null != rd.item)	deleteRequest(rd.item.getItemId());
			session.flush();
			rd = null;
		}
	}	

	@SuppressWarnings("deprecation")
	@Test
	public final void testPlaceItemInStorageRequestWhilePending_Message() throws Throwable {
		boolean disableValidateCurrentState = validationAspect.isDisableValidate();
		validationAspect.setDisableValidate(false);
		session.clear();
		itemRequest.clearErrors();
		RequestData rd = placeItemInStorageRequest();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		List<String> expectedMsgs = Arrays.asList(buildExpectedErrorMsg(rd), buildExpectedErrorIssueMsg(rd));
		session.clear();
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			session.clear();
			Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
			session.clear();
			requestId = request.get(rd.item.getItemId());
			
			Assert.assertNull(requestId, "request id shall be null");
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertTrue(expectedMsgs.contains(e.getValidationErrors().get(0)), "expected: [" + expectedMsgs.toString() + "] but got [" + e.getValidationErrors().get(0) + "]");
			// assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			
			if (null != rd && null != rd.item) deleteRequest(rd.item.getItemId());

			rd = null;
			itemIds = null;
			validationAspect.setDisableValidate(disableValidateCurrentState);
		}
	}
	
	private RequestData placeItemInStorageRequest() throws Throwable {
		//ItItem item = this.createNewTestDevice("PlaceItemInStorageRequest-001", SystemLookup.ItemStatus.INSTALLED);
		long itemId = 325;
	
		// delete any stale request left behind from previous test execution
		deleteRequest(itemId);
		
		Item item = this.getItem(itemId);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
						
		Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}	

	@Test
	public final void testPlaceItemInStorageRequestCabinet() throws Throwable {
		clearRequest(placeItemInStorageRequestCabinet());
	}

	@SuppressWarnings("deprecation")
	@Test
	public final void testPlaceItemInStorageRequestCabinetWhilePending_Message() throws Throwable {
		RequestData rd = placeItemInStorageRequestCabinet();
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(rd.item.getItemId());
		String expectedMsg = buildExpectedErrorMsg(rd);
		
		try {
			Long requestId = rd.requestId;
			
			updateItemRequestStageToWorkOrderIssued (rd);
			
			Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
			requestId = request.get(rd.item.getItemId());
			
			assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		} catch (BusinessValidationException e) {
			System.out.println (e.getValidationErrors());
			assertEquals(e.getValidationErrors().get(0), expectedMsg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			clearRequest(rd);
			rd = null;
			itemIds = null;
		}
	}
	
	
	private RequestData placeItemInStorageRequestCabinet() throws Throwable {
		CabinetItem item = this.createNewTestCabinetWithItems("PlaceItemInStorageRequestCabinet-001", SystemLookup.ItemStatus.INSTALLED);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
						
		Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		Request r = (Request)session.get(Request.class, requestId);
		RequestData rd = new RequestData();
		rd.item = item;
		rd.requestId = requestId;
		rd.requestNumber = r.getRequestNo();
		rd.requestDescription = r.getDescription();
		return rd;
	}	

	//@Test
	public final void testChildItemWithPendingRequest() throws Throwable {
		//create request for child item fist
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(57L);
						
		Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		/*CabinetItem cabinet = this.createNewTestCabinet("ItemWithPendingRequest-001", SystemLookup.ItemStatus.INSTALLED);
		
		ItItem child = this.createNewTestDevice("PlaceItemInStorageRequest-001", SystemLookup.ItemStatus.INSTALLED);
		child.setParentItem(cabinet);
		session.update(child);
		session.flush();
		
		//create request for child item fist
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(child.getItemId());
						
		Map<Long,Long> request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		
		long requestId = request.get(child.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		
		//create request for parent item
		itemIds.clear();
		itemIds.add(cabinet.getItemId());
						
		request = itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		
		requestId = request.get(cabinet.getItemId());
		
		//should fail here
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));	
		
		request = null;*/
	}	
		
	@Test
	public final void testFunctions() throws Throwable {
		getChildrenItemIds(3L);
		
		//RequestHistory requestHistory = getCurrentHistory(57L);
		
		//System.out.println(requestHistory.toString());

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(340L);
		itemIds.add(40L);
		itemIds.add(343L);
		
		List<Long> newList = getRequestItemsForItem(itemIds);
		
		for(Long r:newList){
			System.out.println(r);
		}
		
		newList = getRequestItemsForItem(newList);
		
		for(Long r1:newList){
			System.out.println(r1);
		}
		
		newList = getRequestItemsForItem(null);
		
		for(Long r2:newList){
			System.out.println(r2);
		}
		/*List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());

		List<Request> rList = getItemRequest(325L); itemRequest.getRequests(itemIds, testUser);
		
		for(Request r:rList){
			System.out.println(r.getDescription());
		}
		
		System.out.println(getItemRequestStage(5068L));
		
		disconnectAll(340L);*/
	}
	
	
	////@Test
	public final void testValidateCabinetItemRequestForInstall() throws Throwable {
		try {
			testValidateCabinetInstallRequest(SystemLookup.ItemStatus.PLANNED);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() == 0);
		}
		
		try {
			testValidateCabinetInstallRequest(SystemLookup.ItemStatus.STORAGE);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() == 0);
		}
		
		try {
			testValidateCabinetInstallRequest(SystemLookup.ItemStatus.INSTALLED);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
		
		try {
			testValidateCabinetInstallRequest(SystemLookup.ItemStatus.POWERED_OFF);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
	}

	private void testValidateCabinetInstallRequest(Long statusLkpValueCode)
			throws Throwable, BusinessValidationException, Exception {
		Long cabinetId = new Long(-1);
		List<Long> itemIds = new ArrayList<Long>();
		DataCenterLocationDetails location = null;
		try {
		
			//Create a location
			location = unitTestItemDAO.createUnitTestLocation();
			
			//Create a cabinet
			List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
			Session session = sf.getCurrentSession();
			LksData statusLks = SystemLookup.getLksData(session, statusLkpValueCode);
			if (statusLks != null)
				valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", valueIdDTOList);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);
			
			itemIds.add(cabinetId);
			
			itemRequest.installItemRequest(itemIds, testUser);
		
		} catch (Exception t){
			 throw t;
		}finally {
			//unitTestItemDAO.deleteItem(cabinetId);
			Query query = session.createQuery("from CabinetItem where itemId = :itemId");
			query.setLong("itemId", cabinetId);
			if (query.uniqueResult() != null)
				session.delete(query.uniqueResult());
			session.flush();
			unitTestItemDAO.deleteUnitTestLocation(location);
		}
	}
	

	
	//@Test
	public final void testValidateItemConvertToVMRequest() throws Throwable {
		try {
			testValidateItemConvertToVMRequest("PowerEdge 1550",SystemLookup.ItemStatus.PLANNED);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
		
		try {
			testValidateItemConvertToVMRequest("PowerEdge 1550",SystemLookup.ItemStatus.STORAGE);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
		
		try {
			testValidateItemConvertToVMRequest("PowerEdge 1550",SystemLookup.ItemStatus.INSTALLED);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() == 0);
		}
		
		try {
			testValidateItemConvertToVMRequest("PowerEdge 1550",SystemLookup.ItemStatus.POWERED_OFF);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() == 0);
		}
	}

	private void testValidateItemConvertToVMRequest(String model, Long statusLkpValueCode)
			throws Throwable, BusinessValidationException, Exception {
		Long cabinetId = new Long(-1);
		List<Long> itemIds = new ArrayList<Long>();
		List<Long> itemIdsToBeDeleted = new ArrayList<Long>();
		DataCenterLocationDetails location = null;
		try {
		
			validationAspect.setDisableValidate(false);
			//Create a location
			location = unitTestItemDAO.createUnitTestLocation();
			
			//Create a cabinet
			List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
			Session session = sf.getCurrentSession();
		
			
			
			
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", valueIdDTOList);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);
			
			LksData statusLks = SystemLookup.getLksData(session, statusLkpValueCode);
			if (statusLks != null)
				valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			
			Map<String, UiComponentDTO> itemDTO = unitTestItemDAO.createStandardItem(location.getDataCenterLocationId(), cabinetId, 2, model, valueIdDTOList);
			Long itemId = (Long) ((UiComponentDTO)itemDTO.get("tiName")).getUiValueIdField().getValueId();
			
			
			
			assertTrue(itemId > 0);
			
			itemIds.add(itemId);
			itemIdsToBeDeleted.add(itemId);
			
			itemRequest.convertToVMRequest(itemIds, testUser);
		
		} catch (Exception t){
			 throw t;
		}finally {
			//unitTestItemDAO.deleteItem(itemIds.get(0));

			if (itemIdsToBeDeleted.size() > 0){
				Query query = session.createQuery("from ItItem where itemId = :itemId");
				query.setLong("itemId", itemIdsToBeDeleted.get(0));
				if (query.uniqueResult() != null)
					session.delete(query.uniqueResult());
			}

			
			Query query = session.createQuery("from CabinetItem where itemId = :itemId");
			query.setLong("itemId", cabinetId);
			if (query.uniqueResult() != null)
				session.delete(query.uniqueResult());
			
			
			session.flush();
			unitTestItemDAO.deleteUnitTestLocation(location);
			validationAspect.setDisableValidate(true);
		}
	}
	
	//@Test
	public final void testValidateItemParentChildConstraintInstall() throws Throwable {
		try {
			testValidateItemParentChildConstraintInstall("PowerEdge 1550",SystemLookup.ItemStatus.PLANNED,false);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
		
		try {
			testValidateItemParentChildConstraintInstall("PowerEdge 1550",SystemLookup.ItemStatus.PLANNED,true);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
	}

	private void testValidateItemParentChildConstraintInstall(String model, Long statusLkpValueCode, boolean installCabinet)
			throws Throwable, BusinessValidationException, Exception {
		Long cabinetId = new Long(-1);
		List<Long> itemIds = new ArrayList<Long>();
		List<Long> itemIdsToBeDeleted = new ArrayList<Long>();
		DataCenterLocationDetails location = null;
		validationAspect.setDisableValidate(false);
		try {
		
			//Create a location
			location = unitTestItemDAO.createUnitTestLocation();
			
			//Create a cabinet
			List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
			Session session = sf.getCurrentSession();
			if (installCabinet){
				LksData statusLks = SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED);
				if (statusLks != null)
					valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			}
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", valueIdDTOList);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);
			
			LksData statusLks = SystemLookup.getLksData(session, statusLkpValueCode);
			if (statusLks != null)
				valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			
			Map<String, UiComponentDTO> itemDTO = unitTestItemDAO.createStandardItem(location.getDataCenterLocationId(), cabinetId, 2, model, valueIdDTOList);
			Long itemId = (Long) ((UiComponentDTO)itemDTO.get("tiName")).getUiValueIdField().getValueId();
			
			
			
			assertTrue(itemId > 0);
			
			itemIds.add(itemId);
			itemIdsToBeDeleted.add(itemId);
			
			itemRequest.installItemRequest(itemIds, testUser);
		
		} catch (Exception t){
			 throw t;
		}finally {
			//unitTestItemDAO.deleteItem(itemIds.get(0));

			if (itemIdsToBeDeleted.size() > 0){
				Query query = session.createQuery("from ItItem where itemId = :itemId");
				query.setLong("itemId", itemIdsToBeDeleted.get(0));
				if (query.uniqueResult() != null)
					session.delete(query.uniqueResult());
			}

			
			Query query = session.createQuery("from CabinetItem where itemId = :itemId");
			query.setLong("itemId", cabinetId);
			if (query.uniqueResult() != null)
				session.delete(query.uniqueResult());
			
			
			session.flush();
			unitTestItemDAO.deleteUnitTestLocation(location);
			validationAspect.setDisableValidate(true);
		}
	}

	//@Test
	public final void testValidateItemParentChildConstraintStorage() throws Throwable {
		//Negative case
		try {
			testValidateItemParentChildConstraintStorage("PowerEdge 1550",SystemLookup.ItemStatus.INSTALLED,false);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() > 0);
		}
		
		//Positive case
		try {
			testValidateItemParentChildConstraintStorage("PowerEdge 1550",SystemLookup.ItemStatus.INSTALLED,true);
		} catch (BusinessValidationException e){
			assertTrue(e.getValidationErrorsList().size() == 0);
		}
	}

	private void testValidateItemParentChildConstraintStorage(String model, Long statusLkpValueCode, boolean deviceToStorage)
			throws Throwable, BusinessValidationException, Exception {
		Long cabinetId = new Long(-1);
		List<Long> itemIds = new ArrayList<Long>();
		List<Long> itemIdsToBeDeleted = new ArrayList<Long>();
		DataCenterLocationDetails location = null;
		validationAspect.setDisableValidate(false);
		try {
		
			//Create a location
			location = unitTestItemDAO.createUnitTestLocation();
			
			//Create a cabinet
			List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
			Session session = sf.getCurrentSession();
			LksData statusLks = SystemLookup.getLksData(session, statusLkpValueCode);
			if (statusLks != null)
					valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			
			Map<String, UiComponentDTO> cabinetDTO = unitTestItemDAO.createCabinet(location.getDataCenterLocationId(), "22RU-Cabinet HP 10622", valueIdDTOList);
			cabinetId = (Long) ((UiComponentDTO)cabinetDTO.get("tiName")).getUiValueIdField().getValueId();
			
			assertTrue(cabinetId > 0);
			
			if (deviceToStorage){
				statusLks = SystemLookup.getLksData(session, SystemLookup.ItemStatus.STORAGE);
				if (statusLks != null)
					valueIdDTOList.add(unitTestItemDAO.createValueIdDTOObj("cmbStatus", statusLks.getLkpValueCode()));
			}
			Map<String, UiComponentDTO> itemDTO = unitTestItemDAO.createStandardItem(location.getDataCenterLocationId(), cabinetId, 2, model, valueIdDTOList);
			Long itemId = (Long) ((UiComponentDTO)itemDTO.get("tiName")).getUiValueIdField().getValueId();
			
			
			
			assertTrue(itemId > 0);
			
			itemIds.add(cabinetId);
			itemIdsToBeDeleted.add(itemId);
			
			itemRequest.decommisionItemToStorageRequest(itemIds, testUser);
		
		} catch (Exception t){
			 throw t;
		}finally {
			//unitTestItemDAO.deleteItem(itemIds.get(0));

			if (itemIdsToBeDeleted.size() > 0){
				Query query = session.createQuery("from ItItem where itemId = :itemId");
				query.setLong("itemId", itemIdsToBeDeleted.get(0));
				if (query.uniqueResult() != null)
					session.delete(query.uniqueResult());
			}

			
			Query query = session.createQuery("from CabinetItem where itemId = :itemId");
			query.setLong("itemId", cabinetId);
			if (query.uniqueResult() != null)
				session.delete(query.uniqueResult());
			
			
			session.flush();
			unitTestItemDAO.deleteUnitTestLocation(location);
			validationAspect.setDisableValidate(true);
		}
	}


	//@Test
	public final void tesRebsumitItemRequest() throws Throwable {
		ItItem item = this.createNewTestDevice("RebsumitItemRequest-001", null);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());

		Session session = sf.getCurrentSession();

		Map<Long,Long> request = itemRequest.installItemRequest(itemIds, testUser);
		
		long requestId = request.get(item.getItemId());
		
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_ISSUED));
		
		itemRequest.resubmitRequest(new ArrayList(request.values()), testUser);
			
		assertTrue(checkRequest(requestId, SystemLookup.RequestStage.REQUEST_UPDATED));
	}	
	
	boolean checkRequest(long requestId, long requestType){
		Session session = sf.getCurrentSession();
		
		Request req = (Request)session.get(Request.class, requestId);
		
		for(RequestHistory h:req.getRequestHistories()){
			if(h.isCurrent() && h.getStageIdLookup().getLkpValueCode().longValue() == requestType){
				return true;
			}
		}
		
		return false;
	}

	
	private List<Long> getChildrenItemIds(long itemId){
		Session session = this.sf.getCurrentSession();
		
		String exCludeClasses = " and dct_lks_data.lkp_value_code not in (" + SystemLookup.Class.PASSIVE + ") ";
		
		Query q = session.createSQLQuery(new StringBuffer()
		.append("select i.item_id from dct_items as i inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id where i.parent_item_id = :itemId ")
		.append(exCludeClasses)
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id  where it.chassis_id = :itemId ")
		.append(exCludeClasses)
		.append(" UNION select i.item_id from dct_items as i inner join dct_items_it as it on i.item_id = it.item_id inner join dct_items as chassis on it.chassis_id = chassis.item_id  inner join dct_lks_data on i.class_lks_id = dct_lks_data.lks_id where chassis.parent_item_id = :itemId ")
		.append(exCludeClasses)
		.append(" order by item_id")
		.toString()
	    );
		
		q.setLong("itemId", itemId);
		
		List<Long> recList = new ArrayList<Long>();	
		
		for(Object rec:q.list()){
			BigInteger id = (BigInteger)rec;
			recList.add(id.longValue());
		}
		
		return recList;
	}

	
	public RequestHistory getCurrentHistory(Long itemId) throws DataAccessException {
		RequestHistory requestHistory = null;
		final String rTypeList[] = {"Item","Item Remove","Convert to VM", "Item Move"};	
		final String itemToStorageSuffix = "-TO-STORAGE";

		try {
			Session session = this.sf.getCurrentSession();
			//This criteria will only get the max requestId
			Criteria criteria = getRequestCriteria(itemId, rTypeList, session);
			criteria.setProjection(Projections.max("requestId"));
			Long requestId = (Long) criteria.uniqueResult();
			
			if (null != requestId) {
				//This will actually load the Request. Did this to avoid nested query!
				Criteria historyCriteria = session.createCriteria(RequestHistory.class);
				historyCriteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
				historyCriteria.createAlias("requestDetail", "request");
				historyCriteria.add(Restrictions.eq("request.requestId", requestId));
				historyCriteria.add(Restrictions.eq("current", true));
				requestHistory = (RequestHistory) historyCriteria.uniqueResult();
			}
			
		} catch (HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.REQUEST_FETCH_FAIL, this.getClass(), e));
		}
		
		return requestHistory;
	}

	private Criteria getRequestCriteria(long itemId, String[] rTypeList,
			Session session) {
		Criteria criteria = session.createCriteria(Request.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.createAlias("requestHistories", "history");
		criteria.createAlias("requestPointers", "pointer");
		criteria.createAlias("requestHistories.stageIdLookup", "historyStages");
		criteria.add(Restrictions.eq("history.current", true));		    
		criteria.add(Restrictions.eq("itemId", itemId));
		if (rTypeList != null)
			criteria.add(Restrictions.in("requestType", rTypeList));
		return criteria;
	}
	public  List<Long> getRequestItemsForItem(List<Long> itemList){
		//List<Long> recList = new ArrayList<Long>();
		
		if(itemList == null || itemList.size() == 0) return new ArrayList<Long>();
		
    	Session session = this.sf.getCurrentSession();
    	Query query =  session.getNamedQuery("getRequestItemsForItem");
    	query.setParameterList("itemList", itemList);
    	
    	return query.list();
    	/*
		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			
			recList.add((Long)row[0]);
		}

	    return recList;*/
	}
	
	private void deleteRequest(Long itemId)	 {
		
		Query q1 = session.createSQLQuery("delete from tblrequesthistory where requestid in (select id from tblrequest where itemid = :itemId)");
		q1.setLong("itemId", itemId);
		q1.executeUpdate();
		
		Query q2 = session.createSQLQuery("delete from tblrequestpointer where requestid in (select id from tblrequest where itemid = :itemId)");
		q2.setLong("itemId", itemId);
		q2.executeUpdate();
		
		Query q3 = session.createSQLQuery("delete from tblrequest where itemid = :itemId");
		q3.setLong("itemId", itemId);
		q3.executeUpdate();

		session.flush();
		
	}	

	private void clearRequest(RequestData rd) {

		if (null != rd && null != rd.item) deleteRequest(rd.item.getItemId());
		
	}

}
