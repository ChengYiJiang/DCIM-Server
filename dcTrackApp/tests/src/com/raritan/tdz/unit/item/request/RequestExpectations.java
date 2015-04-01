package com.raritan.tdz.unit.item.request;

import java.util.List;
import java.util.Map;

import org.jmock.Mockery;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;

public interface RequestExpectations {

	public abstract void createIsPendingRequest(Mockery jmockContext, Errors errors, Long itemId, boolean retValue);

	public abstract void createIsPendingRequests(Mockery jmockContext,	Errors errors, List<Long> itemIdsList, boolean retValue);

	public abstract void createGetRequest(Mockery jmockContext,	Errors errors, Long itemId, List<Request> retRequestList);

	public abstract void createInsertMoveRequest(Mockery jmockContext, Errors errors, List<Long> itemIdsList,	Map<Long, Long> moveItemResults);

	public abstract void createGetWhenMovedItemId(Mockery jmockContext,		Long itemId, Long retValue);

	public abstract void createLoadRequest(Mockery jmockContext, Long requestId, Request retValue);

	public abstract void createLoadLks(Mockery jmockContext, LksData retValue);

	public void createInsertRequests(Mockery jmockContext, Errors errors,	List<Long> itemIdsList, Map<Long, Long> itemResults);

	public void createGetLatestRequest(Mockery jmockContext, Long itemId, Request retValue);

	public void createGetCurrentHistory(Mockery jmockContext,  final Long itemId, final RequestHistory retValue);
	
	public void createReSubmitRequest(Mockery jmockContext, Long requestId);

	public void addExpectations(Request requestObj,  boolean pendingRequest);

}