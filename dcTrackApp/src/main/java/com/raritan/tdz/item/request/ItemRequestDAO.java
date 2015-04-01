/**
 * 
 */
package com.raritan.tdz.item.request;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.RequestDTO;

/**
 * @author prasanna
 * This will handle the requests to the database in terms of item request
 * 
 */
public interface ItemRequestDAO {
	
	/**
	 * Insert the a request given the itemId and the request Description
	 * @param itemIds
	 * @param requestType TODO
	 * @param requestDescPrefix TODO
	 * @param errors TODO
	 * @param disconnect - remove all connections
	 * @param newStatusValueCode - item future status
	 * @param processChildren TODO
	 * @return TODO
	 * @throws DataAccessException
	 */
	public Map<Long, Long> insertRequests(List<Long> itemIds, String requestType, String requestDescPrefix, Errors errors, boolean disconnect, Long newStatusValueCode, boolean processChildren) throws DataAccessException;
	
	/**
	 * Insert/Update the request record to the database
	 * @param requests - These are the requests that need to be inserted/updated
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public void insertOrUpdateRequests(List<Request> requests, Errors errors) throws DataAccessException;
	
	/**
	 * Insert/Update the request record to the database
	 * @param requests - These are the requests that need to be inserted/updated
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public void insertOrUpdateRequest(Request request, Errors errors) throws DataAccessException;
	
	/**
	 * Get the request record from database
	 * @param itemIds item ids for which we seek requests.
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @return
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public Map<Long,List<Request>> getRequest(List<Long> itemIds, Errors errors) throws DataAccessException;
	
	/**
	 * Get the request record from database
	 * @param itemId item id for which we seek requests.
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @return
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public List<Request> getRequest(Long itemId, Errors errors) throws DataAccessException;
	
	/**
	 * Get the request record from database
	 * @param itemIds item ids for which we seek requests.
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @return
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public Map<Long,List<Request>> getRequest(List<Long> itemIds, List<Long> requestStageFilters, Errors errors) throws DataAccessException;
	
	/**
	 * Get the request record from database
	 * @param itemId item id for which we seek requests.
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @return
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public List<Request> getRequest(Long itemId, List<Long> requestStageFilters, Errors errors) throws DataAccessException;
	
	/**
	 * delete the requests.
	 * @param requests
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @return - list of deleted request Ids
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public List<Long> deleteRequests(List<Request> requests, Errors errors) throws DataAccessException;
	
	/**
	 * delete a single request
	 * @param request
	 * @param errors - Any errors on any of the requests will be collected in the errors.
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public void deleteRequest(Request request, Errors errors) throws DataAccessException;
	
	/**
	 * Checks to see if there is a pending request on the given item. 
	 * If the request stage is "Request Completed" then there are no pending requests
	 * @param requestType - request made by the user
	 * @param errors - Can be used for giving a user friendly error for database errors
	 * @param itemId
	 * @return - Map of itemId to the resultant boolean value.
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public Map<Long,Boolean> isPendingRequests(List<Long> itemIds, Long requestType, Errors errors) throws DataAccessException;
	
	/**
	 * Checks to see if there is a pending request on the given item. 
	 * If the request stage is "Request Completed" then there are no pending requests
	 * @param itemId
	 * @param requestType - current request made by the user
	 * @param errors - Can be used for giving a user friendly error for database errors
	 * @return - Map of itemId to the resultant boolean value.
	 * @throws - Data Access exception. <b>NOTE: This has to be thrown only after collecting errors</b>
	 */
	public Boolean isPendingRequest(Long itemId, Long requestType, Errors errors) throws DataAccessException;
	
	/**
	 * Get Latest Request for the given item. This will provide the latest request pending for the 
	 * item based on the max(requestId)
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public Request getLatestRequest(Long itemId) throws DataAccessException;

	/**
	 * Re-Submit a request 
	 * @param requestId
	 * @return
	 * @throws DataAccessException
	 */
	public void reSubmitRequest(Long requestId) throws DataAccessException;	

	/**
	 * Get the current request history of an item
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public RequestHistory getCurrentHistory(Long itemId) throws DataAccessException;
	
	
	/**
	 * Given lkpValue code load LksData
	 * @param lkpValueCode
	 * @return
	 */
	public LksData loadLks(Long lkpValueCode);
	
	
	/**
	 * Loads the request given request ID
	 * @param requestId
	 * @return
	 */
	public Request loadRequest(Long requestId);
	
	/**
	 * Create an VM Item 
	 * @param itemId
	 * @return
	 */
	public Long createVmItem(Long itemId) throws BusinessValidationException;
	
	/**
	 * Get requestDTO for given item Ids. If none are available returns empty list
	 * @param itemIds
	 * @return
	 */
	public List<RequestDTO> getRequestDTOForItems(List<Long> itemIds);
	
	/**
	 * Get requestDTO for given requestIds. If none are available returns empty list
	 * @param requestIds
	 * @return
	 */
	public List<RequestDTO> getRequestDTOForRequests(List<Long> requestIds);

	/**
	 * Get item Ids associated with an item. If none are available returns empty list
	 * Associated items are not in Planned or Archived or Hidden state. Neither they are of class Passive.
	 * @param itemId list
	 * @return
	 */
	public List<Long> getRequestItemsForItem(List<Long> itemList);


	/**
	 * get the original moved item id against the new item
	 * @param newItemId
	 * @return
	 */
	public Long getMovingItemId(Long newItemId);

	/**
	 * get the when moved item id against the original item id
	 * @param oldItemId
	 * @return
	 */
	public Long getWhenMovedItemId(Long oldItemId);

	/**
	 * get the current request for the port
	 * @param portId
	 * @param tableName
	 * @return
	 * @throws DataAccessException
	 */
	public Request getLatestPortRequest(Long portId, String tableName)
			throws DataAccessException;

	/**
	 * get the current request history of the request
	 * @param requestId
	 * @return
	 * @throws DataAccessException
	 */
	public RequestHistory getCurrentHistoryUsingRequest(Long requestId)
			throws DataAccessException;

	/**
	 * get the request against the item
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public List<Request> getItemRequest(long itemId) throws DataAccessException;

	
	/**
	 * get the request against the item
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public List<Request> getAllRequestForAnItem(long itemId, String[] requestTypes) throws DataAccessException;

	/**
	 * get the list of request stages that all requests for an item is in.
	 * @param itemId
	 * @param requestStageValueCodes
	 * @return
	 * @throws DataAccessException
	 */
	public List<Long> getItemRequestStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException;

	/**
	 * informs if any of the items request exist in the list of stages
	 * @param itemId
	 * @param requestStageValueCodes
	 * @return
	 * @throws DataAccessException
	 */
	public boolean itemRequestExistInStages(long itemId,
			List<Long> requestStageValueCodes) throws DataAccessException;

	/**
	 * get request dto for the item ids in the provided stages
	 * @param requestIds
	 * @return
	 */
	public List<RequestDTO> getRequestDTO(List<Long> itemIds, List<Long> requestStages);

	/**
	 * delete request
	 * @param requestId
	 */
	public void deleteRequest(Long requestId);

	/**
	 * delete a list of requests
	 * @param requestIds
	 */
	public void deleteRequestList(List<Long> requestIds);

	/**
	 * get list of pending item request
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	public List<Request> getPendingItemRequests(long itemId) 	throws DataAccessException;

}
