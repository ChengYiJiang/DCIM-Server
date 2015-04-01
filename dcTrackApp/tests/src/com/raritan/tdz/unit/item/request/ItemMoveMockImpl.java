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
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.item.ItemMockImpl;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.item.request.ItemRequest.ItemRequestType;

public class ItemMoveMockImpl extends ItemMockImpl implements ItemMoveMock {
	@Autowired
	private ItemRequest itemRequest;
	
	@Autowired
	ItemRequestDAO itemRequestDAO;
	
	@Autowired
	RequestExpectations requestExpectations;
	
	@Autowired
	ItemRequestMock itemRequestMock;
	
	Item itemToMove;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemMoveMock#getItemToMove()
	 */
	@Override
	public Item getItemToMove() {
		return itemToMove;
	}

	boolean pendingRequest;
	
	@Override
	public boolean isPendingRequest() {
		return pendingRequest;
	}
	
	@Override
	public void setPendingRequest(boolean pendingRequest) {
		this.pendingRequest = pendingRequest;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemMoveMock#setItemToMove(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void setItemToMove(Item itemToMove) {
		this.itemToMove = itemToMove;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.request.ItemMoveMock#addExpectations(com.raritan.tdz.domain.Item, org.jmock.Mockery)
	 */
	@Override
	public void addExpectations(final Item item, Mockery currentJmockContext){
		super.addExpectations(item, currentJmockContext);
		
		final long itemId = item.getItemId();
		final long itemToMoveId = itemToMove != null ? itemToMove.getItemId() : -99L;
		
		Map<String, String> errorMap = new HashMap<String, String>();
		final Errors errors = new MapBindingResult(errorMap, ItemRequest.class.getName());

		final List<Request> requestList = new ArrayList<Request>();
		final List<Long> itemIdsList = new ArrayList<Long>();
		final Map<Long,Long> moveItemResults = new HashMap<Long,Long>();
		
		itemRequestMock.setJmockContext(currentJmockContext);
		
		final Request requestObj = itemRequestMock.createRequestMove(item, false);
		final Long requestId = requestObj.getRequestId();
		
		
		final Map<Long, Boolean> pendingRequests = new HashMap<Long, Boolean>();
		
		if(itemToMoveId > 0){
			itemIdsList.add(itemToMoveId);
			moveItemResults.put(itemToMoveId, requestId);
			requestObj.setItemId(itemToMoveId);
			requestExpectations.createGetWhenMovedItemId(currentJmockContext, itemToMoveId, itemId);
			requestExpectations.createGetRequest(currentJmockContext, errors, itemToMoveId, requestList);
			requestExpectations.createIsPendingRequest(currentJmockContext, errors, itemToMoveId, pendingRequest);
			requestExpectations.createIsPendingRequests(currentJmockContext, errors, itemIdsList, pendingRequest);
		}
		else{
			itemIdsList.add(itemId);
			moveItemResults.put(itemId, requestId);
			requestObj.setItemId(itemId);
		}
		
		requestExpectations.createInsertMoveRequest(currentJmockContext, errors, itemIdsList, moveItemResults);		
	}
}
