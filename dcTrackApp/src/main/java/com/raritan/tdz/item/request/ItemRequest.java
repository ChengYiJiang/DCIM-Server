/**
 * 
 */
package com.raritan.tdz.item.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 * <p>This will handle the actual item requests</p>
 * 
 * <p><em>NOTE: In the future when we have Bonita workflow running, we can still
 * use this interface to perform the actual actions against the request 
 * (meaning submitting request into the database)</em><p>
 * 
 * This will also be using ItemStateContext beans to perform state based validations.
 * 
 */
public interface ItemRequest {
	
	public static class ItemRequestType {
		public static final String convertToVM = "Convert to VM";
		public static final String takeItemOffSite = "Take Off-site";
		public static final String bringItemOnSite = "Bring on-site";
		public static final String powerOff = "Power off";
		public static final String powerOn = "Power on";
		public static final String decomissionToStorage = "Item Remove";
		public static final String decomissionToArchive = "Item Remove";
		public static final String installItem = "Item";
		public static final String disconnect = "Disconnect";
		public static final String moveItem = "Item Move";
		public static final String connect = "Connect";
		public static final String reconnect = "Reconnect";
		
		public static final List<String> itemRequestTypeList = Collections.unmodifiableList(new ArrayList<String>() {{
			add(convertToVM);
			add(takeItemOffSite);
			add(bringItemOnSite);
			add(powerOff);
			add(powerOn);
			add(decomissionToStorage);
			add(decomissionToArchive);
			add(installItem);
			add(disconnect);
			add(moveItem);
			add(connect);
			add(reconnect);
		}});
	}
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - itemId to request id map
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.CONVERT_TO_VM)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED,
			SystemLookup.ItemStatus.POWERED_OFF,
			SystemLookup.ItemStatus.OFF_SITE
			})
	public Map<Long, Long> convertToVMRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_OFF_SITE)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.OFF_SITE)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED,
			SystemLookup.ItemStatus.POWERED_OFF,
			SystemLookup.ItemStatus.STORAGE
			})
	public Map<Long, Long> takeItemOffsiteRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_ON_SITE)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.INSTALLED)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.OFF_SITE
			})
	public Map<Long, Long> bringItemOnsiteRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return 
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_POWER_OFF)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.POWERED_OFF)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED
			})
	public Map<Long, Long> powerOffItemRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_POWER_ON)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.INSTALLED)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.POWERED_OFF
			})
	public Map<Long, Long> powerOnItemRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.IN_STORAGE)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED,
			SystemLookup.ItemStatus.POWERED_OFF,
			SystemLookup.ItemStatus.OFF_SITE
			})
	public Map<Long, Long> decommisionItemToStorageRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.ARCHIVED)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED,
			SystemLookup.ItemStatus.POWERED_OFF,
			SystemLookup.ItemStatus.OFF_SITE,
			SystemLookup.ItemStatus.STORAGE
			})
	public Map<Long, Long> decommisionItemToArchiveRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.NEW_ITEM)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.INSTALLED)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.PLANNED,
			SystemLookup.ItemStatus.STORAGE
			})
	public Map<Long, Long> installItemRequest(List<Long> itemIds, UserInfo user) throws BusinessValidationException;
	
	/**
	 * Covert the given item to VM. Validate if you can before 
	 * doing so.
	 * @param requestIds - a bunch of request database ids to perform this operation on
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - requestid and requestStatus.
	 */
	public Map<Long, Long> resubmitRequest(List<Long> requestIds, UserInfo user) throws BusinessValidationException, DataAccessException;
	

	/**
	 * Given the item ids get the request objects.
	 * @param itemIds
	 * @param userInfo - to check if the user has permission to view requests. For now this can be null
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public Map<Long,List<Request>> getRequests(List<Long> itemIds, UserInfo user) throws BusinessValidationException, DataAccessException;
	
	/**
	 * Given the item ids get the request objects.
	 * @param itemIds
	 * @param requestStages - Send in the list of request stages that should be included.
	 * @param userInfo - to check if the user has permission to view requests. For now this can be null
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public Map<Long,List<Request>> getRequests(List<Long> itemIds, List<Long> requestStages, UserInfo user) throws BusinessValidationException, DataAccessException;
	
	/**
	 * This gets the latest request stage for the latest history of the request for a given item
	 * @param itemId
	 * @return
	 * @throws DataAccessException - This will be used internally and not exposed to user and thus we dont have BusinessValidationException
	 */
	public LksData getLatestRequestStage(Long itemId) throws DataAccessException;
	
	/**
	 * Gets errors if any created during the above interface calls.
	 * @return
	 */
	public Errors getErrors();
	
	/**
	 * Clear errors once we have captured errors
	 * This will be generally called just before throwing the businessValidationException.
	 */
	public void clearErrors();
	
	/**
	 * Create a move item request. Validate if you can before 
	 * doing so.
	 * @param itemIds - a bunch of item database ids to perform this operation on
	 * @param disconnect TODO
	 * @param userInfo - to check if the user has permission to submit this request.
	 * @return - request id.
	 */
	@RequestedOperation(requestLkpValueCode=SystemLookup.RequestTypeLkp.ITEM_MOVE)
	@RequestedItemStatus(statusLkpValueCode=SystemLookup.ItemStatus.TO_BE_REMOVED)
	@AllowableItemStatusForRequest(statusLkpValueCodes={
			SystemLookup.ItemStatus.INSTALLED,
			SystemLookup.ItemStatus.POWERED_OFF
			})
	public Map<Long, Long> moveItemRequest(Map<Long, Long> itemIds, UserInfo user, boolean disconnect) throws BusinessValidationException;

	/**
	 * Return requests associated with an item with matching request stages.
	 * @param itemId
	 * @param requestStages
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public List<Request> getRequests(Long itemId, List<Long> requestStages) throws BusinessValidationException,
			DataAccessException;
	
	/**
	 * Check if item move request is allowed for itemId(s)
	 * The function name starts with 'get' because all functions except one 
	 * with one begnining with get is part of aop. 
	 * @param itemIds
	 * @param userInfo TODO
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public Boolean getIsMoveRequestAllowed(List<Integer> itemIds, UserInfo userInfo) throws BusinessValidationException, DataAccessException;

	public void setItemStatus(Long itemId, Long statusValueCode) throws Throwable;

	public List<Long> getItemRequestStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException;

	public boolean itemRequestExistInStages(long itemId,
			List<Long> requestStageValueCodes) throws DataAccessException;
}
