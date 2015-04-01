package com.raritan.tdz.unit.item.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;

public class RequestExpectationsImpl implements RequestExpectations {	
	@Autowired
	ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private SystemLookupInitUnitTest systemLookupInitTest;
	
	@Autowired
	protected Mockery jmockContext;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createPendingRequestForItem(org.jmock.Mockery, org.springframework.validation.Errors, java.lang.Long, boolean)
	 */
	@Override
	public void createIsPendingRequest(Mockery jmockContext, final Errors errors, final Long itemId, final boolean retValue){
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).isPendingRequest(with(itemId), with(any(Long.class)), with(errors)); will(returnValue(retValue));
			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createPendingRequestForItemList(org.jmock.Mockery, org.springframework.validation.Errors, java.util.List, boolean)
	 */
	@Override
	public void createIsPendingRequests(Mockery jmockContext,  final Errors errors, final List<Long> itemIdsList,  final boolean retValue){
		final Map<Long, Boolean> pendingRequests = new HashMap<Long, Boolean>();
		
		for(Long itemId:itemIdsList){
			pendingRequests.put(itemId, retValue);
		}
		
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).isPendingRequests(with(itemIdsList), with(any(Long.class)), with(errors)); will(returnValue(pendingRequests));
			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createGetRequestForItem(org.jmock.Mockery, org.springframework.validation.Errors, java.lang.Long, java.util.List)
	 */
	@Override
	public void createGetRequest(Mockery jmockContext,  final Errors errors, final Long itemId,  final List<Request> retRequestList ){
		final List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
			
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).getRequest(with(itemId), with(requestStages), with(errors)); will(returnValue(retRequestList));				

			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createInsertMoveRequestForItemList(org.jmock.Mockery, org.springframework.validation.Errors, java.util.List, java.util.Map)
	 */
	@Override
	public void createInsertMoveRequest(Mockery jmockContext,  final Errors errors, final List<Long> itemIdsList,  final Map<Long,Long> moveItemResults ){				
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).insertRequests(with(itemIdsList), with(ItemRequest.ItemRequestType.moveItem), with("Move"), with(errors), with(true), with((Long)null), with(false)); will(returnValue(moveItemResults));				

			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createGetWhenMovedItemId(org.jmock.Mockery, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void createGetWhenMovedItemId(Mockery jmockContext, final Long itemId, final Long retValue){
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).getWhenMovedItemId(with(itemId)); will(returnValue(retValue));
			}});
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createLoadRequestForReqId(org.jmock.Mockery, java.lang.Long, java.lang.Object)
	 */
	@Override
	public void createLoadRequest(Mockery jmockContext,  final Long requestId, final Request retValue){
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).loadRequest(with(requestId)); will(returnValue(retValue));
			}});
	}	


	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.RequestExpectations#createLoadLks(org.jmock.Mockery, java.lang.Object)
	 */
	@Override
	public void createLoadLks(Mockery jmockContext,  final LksData retValue){
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).loadLks(with(any(Long.class))); will(returnValue(retValue));
			}});
	}	
	
	@Override
	public void createInsertRequests(Mockery jmockContext,  final Errors errors, final List<Long> itemIdsList,  final Map<Long,Long> itemResults ){				
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).insertRequests(with(itemIdsList), with("Test Request"), with("Tester"), with(errors), with(true), with((Long)null), with(false)); will(returnValue(itemResults));				

			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void createGetLatestRequest(Mockery jmockContext,  final Long itemId, final Request retValue){
			try {
				jmockContext.checking(new Expectations() {{			
					allowing(itemRequestDAO).getLatestRequest(with(itemId)); will(returnValue(retValue));
				}});
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}	

	@Override
	public void createGetCurrentHistory(Mockery jmockContext,  final Long itemId, final RequestHistory retValue){
			try {
				jmockContext.checking(new Expectations() {{			
					allowing(itemRequestDAO).getCurrentHistory(with(itemId)); will(returnValue(retValue));
				}});
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@Override
	public void createReSubmitRequest(Mockery jmockContext,  final Long requestId){
		try {
			jmockContext.checking(new Expectations() {{			
				allowing(itemRequestDAO).reSubmitRequest(with(requestId));
			}});
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	@Override
	public void addExpectations(final Request requestObj, boolean pendingRequest){		
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
		
		createLoadLks(jmockContext, itemStatusLks);
		createLoadRequest(jmockContext, requestId, requestObj);
		createReSubmitRequest(jmockContext, requestId);
		createGetWhenMovedItemId(jmockContext, itemId, -1L);
		createGetRequest(jmockContext, errors, itemId, requestList);
		createIsPendingRequest(jmockContext, errors, itemId, pendingRequest);
		createIsPendingRequests(jmockContext, errors, itemIdsList, pendingRequest);
		//createGetCurrentHistory(jmockContext, itemId, requestHistory);
		createGetLatestRequest(jmockContext, itemId, requestObj);
		
	}	
}
