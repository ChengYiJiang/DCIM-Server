package com.raritan.tdz.unit.item.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class ItemRequestMockImpl implements ItemRequestMock {
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;

	@Autowired
	private SystemLookupInitUnitTest systemLookupInitTest;

	@Autowired
	RequestExpectations requestExpectations;
		
	@Autowired
	protected Mockery jmockContext;
	
	public Mockery getJmockContext() {
		return jmockContext;
	}

	public void setJmockContext(Mockery jmockContext) {
		this.jmockContext = jmockContext;
	}

	RequestHistory requestHistory;
	RequestPointer requestPointer;
	boolean pendingRequest;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestInstalled(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestInstalled(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.installItem);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestMove(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestMove(Item item, boolean pendingRequest){
		this.pendingRequest = pendingRequest;
		
		return createRequest(item, ItemRequest.ItemRequestType.moveItem);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestOnSite(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestOnSite(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.bringItemOnSite);
	}
	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestToArchive(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestToArchive(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.decomissionToArchive);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestToStorage(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestToStorage(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.decomissionToStorage);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestPowerOff(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestPowerOff(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.powerOff);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestPowerOn(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestPowerOn(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.powerOn);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequestOffSite(com.raritan.tdz.domain.Item)
	 */
	public Request createRequestOffSite(Item item){
		return createRequest(item, ItemRequest.ItemRequestType.takeItemOffSite);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequest(com.raritan.tdz.domain.Item, java.lang.String)
	 */
	public Request createRequest(Item item, String reqType){
		Request requestObj = new Request();
		requestObj.setRequestId(unitTestIdGenerator.nextId());
		requestObj.setItemId(item.getItemId());
		requestObj.setRequestNo("Test Request No");
		requestObj.setRequestType(reqType);
		requestObj.setDescription("Testing " + reqType);
		
		createHistory(requestObj, SystemLookup.RequestStage.REQUEST_ISSUED);
		createRequetPointer(requestObj, "dct_items");
		
		addExpectations(requestObj, jmockContext);
		
		return requestObj;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createHistory(com.raritan.tdz.domain.Request, java.lang.Long)
	 */
	public RequestHistory createHistory(Request req, Long stageValueCode){
		RequestHistory h = new RequestHistory();
		h.setRequestHistoryId(unitTestIdGenerator.nextId());
		h.setCurrent(true);
		h.setRequestDetail(req);
		h.setStageIdLookup(systemLookupInitTest.getLks(stageValueCode));
		
		req.addRequestHistory(h);
		
		requestHistory = h;
		
		return h;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemRequestMock#createRequetPointer(com.raritan.tdz.domain.Request, java.lang.String)
	 */
	public RequestPointer createRequetPointer(Request req, String tableName){
		RequestPointer p = new RequestPointer();
		p.setRecordId(req.getItemId());
		p.setRequestDetail(req);
		p.setRequestPointerId(unitTestIdGenerator.nextId());
		p.setSortOrder(1);
		p.setTableName(tableName);
		
		req.addRequestPointer(p);
		
		requestPointer = p;
		
		return p;		
	}
	
	@Override
	public void addExpectations(final Request requestObj, Mockery currentJmockContext){		
		final long itemId = requestObj.getItemId();
		final Long requestId = requestObj.getRequestId();
		
		Map<String, String> errorMap = new HashMap<String, String>();
		final Errors errors = new MapBindingResult(errorMap, ItemRequest.class.getName());

		final List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		
		final List<Request> requestList = new ArrayList<Request>();
		final List<Long> itemIdsList = new ArrayList<Long>();
		final LksData itemStatusLks = systemLookupInitTest.getLks(SystemLookup.ItemStatus.INSTALLED);
		final Map<Long, Boolean> pendingRequests = new HashMap<Long, Boolean>();
	
		itemIdsList.add(itemId);
		requestObj.setItemId(itemId);
		pendingRequests.put(itemId, pendingRequest);	
		
		if(pendingRequest){
			requestList.add(requestObj);
		}
		
		requestExpectations.createLoadLks(currentJmockContext, itemStatusLks);
		requestExpectations.createLoadRequest(currentJmockContext, requestId, requestObj);
		requestExpectations.createReSubmitRequest(currentJmockContext, requestId);
		requestExpectations.createGetWhenMovedItemId(currentJmockContext, itemId, -1L);
		requestExpectations.createGetRequest(currentJmockContext, errors, itemId, requestList);
		requestExpectations.createIsPendingRequest(currentJmockContext, errors, itemId, pendingRequest);
		requestExpectations.createIsPendingRequests(currentJmockContext, errors, itemIdsList, pendingRequest);
		requestExpectations.createGetCurrentHistory(currentJmockContext, itemId, requestHistory);
		requestExpectations.createGetLatestRequest(currentJmockContext, itemId, requestObj);
		
	}	
}
