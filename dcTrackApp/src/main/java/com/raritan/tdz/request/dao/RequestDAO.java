package com.raritan.tdz.request.dao;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.WorkOrder;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.RequestDTO;

/**
 * This will be a DAO class for Requests object
 * @author Santo Rosario
 */
	
public interface RequestDAO extends Dao<Request> {	
	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return Request
	 */	
	public Request loadRequest(Long id); 

	/**
	 * Load/Read an existing record from database using lazying loading and a new hibernate session
	 * 
	 * @param id - Id of record to be loaded
	 * @return Request
	 */	
	public Request loadRequest(Long id, boolean readOnly); 
	
	/**
	 * Load/Read an item from the database using current hiberante session
	 * 
	 * @param id - request id
	 * @return Request
	 */
	public Request getRequest(Long id);	

	/**
	 * Delete an item from the database using current hiberante session
	 * 
	 * @param id - request id
	 * @return none
	 */
	public void delete(Long id);

	public RequestHistory createReqHist(Request request, long requestStageValueCode, UserInfo userInfo);
	
	public WorkOrder createWorkOrder(Request request, UserInfo userInfo);

	public String getNextRequestNo();

	/**
	 * execute the work order item move 
	 * @param request
	 * @param userInfo TODO
	 */
	public void itemMoveWorkOrderComplete(Request request, UserInfo userInfo);

	public List<Request> getRequestForItem(long itemId, Long[] rStage);

	/**
	 * execute the work order for circuit install
	 * @param request
	 */
	public void circuitInstallWorkOrderComplete(Request request);
	
	/**
	 * get Request by requestId
	 * @param requestId
	 * @return
	 * @throws DataAccessException
	 */
	public Request getRequestById(Long requestId);

	/**
	 * get the requests for a given request list
	 * @param requestIds
	 * @return
	 */
	public List<Request> getRequests(List<Long> requestIds);

	/**
	 * set the current history as not current
	 * @param request
	 */
	public void setRequestHistoryNotCurrent(Request request);

	/**
	 * set work order complete
	 * @param request
	 */
	public void setWorkOrderComplete(Request request);

	/**
	 * genearte the work order number
	 * @return
	 */
	public String getWorkOrderNo(Request request);

	/**
	 * Return all pending requests associated with the itemId 
	 * @param itemId
	 * @return
	 */
	public List<Request> getAllPendingRequestsForAnItem(long itemId);

	/**
	 * Return all associated request IDs for a request
	 * @param Request object
	 * @return List of request IDs
	 */
	public List<Long> getAssociatedRequestIdsForRequest(Request request);

	/**
	 * Return all associated pending requests for a request
	 * @param Request object
	 * @return List of request objects
	 */
	public List<Request> getAssociatedPendingReqsForReq(Request request);

	/**
	 * Archive the item before the move
	 * @param request
	 * @param userInfo
	 */
	public void itemArchived(Request request, UserInfo userInfo);

	/**
	 * Archive the circuit before the disconnect
	 * @param circuitListId
	 * @param request
	 * @param userInfo
	 */
	public void circuitArchived(Long circuitListId, Request request, UserInfo userInfo);

	/**
	 * Archive cabinet elevation information
	 * @param request
	 * @param userInfo
	 */
	public void cabinetElevationArchived(Request request, UserInfo userInfo);

	/**
	 * get map of itemIds and its associated pending requests
	 * @param itemIds
	 * @param requestStageFilters
	 * @param errors
	 * @return
	 */
	public Map<Long, List<Request>> getRequestsForItem(List<Long> itemIds, 	List<Long> requestStageFilters, Errors errors);

	/**
	 * get all requests for the list of item of a given request type 
	 * @param itemIds
	 * @param requestStageFilters
	 * @param requestType
	 * @return
	 */
	public List<Request> getPendingRequestsForItem(List<Long> itemIds, List<Long> requestType);

	/**
	 * get list of non-power request for the item
	 * @param itemIds
	 * @param requestType
	 * @return
	 */
	public List<Request> getPendingNonPowerRequestsForItem(List<Long> itemIds, List<Long> requestType);

	/**
	 * get the last index generated for the associated request
	 * @param request
	 * @return
	 */
	public Integer getLastAssociatedRequestCount(Request request);

	/**
	 * get associated requests in the Request DTO for the client
	 * @param requestNos
	 * @param requestStages
	 * @return
	 */
	public List<RequestDTO> getAssociatedRequestDTO(List<String> requestNos,
			List<Long> requestStages);

	/**
	 * get list of associated request Ids for a given request
	 * @param request
	 * @return
	 */
	public List<Long> getAssociatedPendingReqIdsForReq(Request request);

	/**
	 * get list of associated request ids for an item
	 * @param itemId
	 * @return
	 */
	public List<Long> getAssociatedItemRequest(Long itemId);

	/**
	 * get list of pending associated requests for the given list of requests 
	 * @param requests
	 * @return
	 */
	public List<Request> getAssociatedPendingReqsForReqs(List<Request> requests);

	/**
	 * get list of pending requests for an item of a given request type
	 * @param itemIds
	 * @param requestType
	 * @return
	 */
	public List<Long> getPendingRequestIdsForItem(List<Long> itemIds, List<Long> requestType);

	/**
	 * get the request using request number
	 * @param requestNo
	 * @param readOnly
	 * @return
	 */
	public Request loadRequest(String requestNo, boolean readOnly);

	/**
	 * 
	 * @param request
	 * @param requestStageValueCode
	 * @param userInfo
	 * @param comment
	 * @return
	 */
	public RequestHistory createReqHist(Request request, long requestStageValueCode,
			UserInfo userInfo, String comment);

	/**
	 * get list of item ids for a given list of request ids
	 * @param requestIds
	 * @return
	 */
	public List<Long> getItemIdsForRequests(List<Long> requestIds);

	/**
	 * get request using request number
	 * @param requestNo
	 * @param readOnly
	 * @return
	 */
	public Request getRequest(String requestNo, boolean readOnly);

	/**
	 * get item related requests
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public List<Request> getItemRequest(long itemId) throws DataAccessException;

}

