package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class RequestLookup {

	public static final String RquestIssueValidate = "RequestIssueValidate";
	
	@SuppressWarnings("serial")
	public static final Map<Long, List<Long>> itemRequestTypeAndFinalStatusMap = 
			Collections.unmodifiableMap(new HashMap<Long, List<Long>>() {{
				put(SystemLookup.RequestTypeLkp.CONVERT_TO_VM, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_MOVE, Arrays.asList(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF));
				put(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE, Arrays.asList(SystemLookup.ItemStatus.OFF_SITE));
				put(SystemLookup.RequestTypeLkp.ITEM_ON_SITE, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF));
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_ON, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE, Arrays.asList(SystemLookup.ItemStatus.ARCHIVED));
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE, Arrays.asList(SystemLookup.ItemStatus.STORAGE));
				put(SystemLookup.RequestTypeLkp.NEW_ITEM, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.CONNECT, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.DISCONNECT, null);
				
				
			}});

	@SuppressWarnings("serial")
	public static final Map<Long, List<Long>> itemRequestTypeAndFinalStatusCheckMap = 
			Collections.unmodifiableMap(new HashMap<Long, List<Long>>() {{
				put(SystemLookup.RequestTypeLkp.CONVERT_TO_VM, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_MOVE, Arrays.asList(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF));
				put(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE, Arrays.asList(SystemLookup.ItemStatus.OFF_SITE));
				put(SystemLookup.RequestTypeLkp.ITEM_ON_SITE, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, Arrays.asList(SystemLookup.ItemStatus.POWERED_OFF));
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_ON, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE, Arrays.asList(SystemLookup.ItemStatus.ARCHIVED));
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE, Arrays.asList(SystemLookup.ItemStatus.STORAGE));
				put(SystemLookup.RequestTypeLkp.NEW_ITEM, Arrays.asList(SystemLookup.ItemStatus.INSTALLED));
				put(SystemLookup.RequestTypeLkp.CONNECT, Arrays.asList(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.ItemStatus.OFF_SITE));
				put(SystemLookup.RequestTypeLkp.DISCONNECT, null);
				
				
			}});

	@SuppressWarnings("serial")
	public static final Map<Long, String> itemStatusToErrorString = 
			Collections.unmodifiableMap(new HashMap<Long, String>() {{
				put(SystemLookup.ItemStatus.INSTALLED, "Installed");
				put(SystemLookup.ItemStatus.OFF_SITE, "Off Site");
				put(SystemLookup.ItemStatus.POWERED_OFF, "Powered Off");
				put(SystemLookup.ItemStatus.ARCHIVED, "Archived");
				put(SystemLookup.ItemStatus.STORAGE, "Storage");
				
			}});

	@SuppressWarnings("serial")
	public static final Map<Long, Long> itemMoveRequestToFakeRequestType = 
			Collections.unmodifiableMap(new HashMap<Long, Long>() {{
				put(SystemLookup.ItemStatus.INSTALLED, SystemLookup.RequestTypeLkp.NEW_ITEM);
				put(SystemLookup.ItemStatus.POWERED_OFF, SystemLookup.RequestTypeLkp.ITEM_POWER_OFF);
				
			}});

	
	public static final List<Long> installRequests = Arrays.asList(SystemLookup.RequestTypeLkp.NEW_ITEM, 
																							SystemLookup.RequestTypeLkp.CONVERT_TO_VM, 
																							SystemLookup.RequestTypeLkp.ITEM_MOVE, 
																							SystemLookup.RequestTypeLkp.ITEM_ON_SITE, 
																							SystemLookup.RequestTypeLkp.ITEM_POWER_ON,
																							SystemLookup.RequestTypeLkp.CONNECT);
	
	public static final List<Long> moveRequests = Arrays.asList(SystemLookup.RequestTypeLkp.NEW_ITEM, 
																						SystemLookup.RequestTypeLkp.CONVERT_TO_VM, 
																						SystemLookup.RequestTypeLkp.ITEM_MOVE, 
																						SystemLookup.RequestTypeLkp.ITEM_ON_SITE, 
																						SystemLookup.RequestTypeLkp.ITEM_POWER_ON,
																						SystemLookup.RequestTypeLkp.ITEM_POWER_OFF,
																						SystemLookup.RequestTypeLkp.CONNECT);
	
	public static final List<Long> archiveRequests = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE);	
	public static final List<Long> storageRequests = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE);
	public static final List<Long> disconnectRequests = Arrays.asList(SystemLookup.RequestTypeLkp.DISCONNECT,
																	SystemLookup.RequestTypeLkp.CONNECT, 
																	SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE, 
																	SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE);
	public static final List<Long> connectRequests = Arrays.asList(SystemLookup.RequestTypeLkp.NEW_ITEM, 
			SystemLookup.RequestTypeLkp.CONVERT_TO_VM, 
			SystemLookup.RequestTypeLkp.ITEM_MOVE, 
			SystemLookup.RequestTypeLkp.ITEM_ON_SITE, 
			SystemLookup.RequestTypeLkp.ITEM_POWER_ON,
			SystemLookup.RequestTypeLkp.CONNECT,
			SystemLookup.RequestTypeLkp.DISCONNECT, 
			SystemLookup.RequestTypeLkp.ITEM_POWER_OFF,
			SystemLookup.RequestTypeLkp.ITEM_OFF_SITE);

	
	public static final List<Long> powerOffRequests = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, SystemLookup.RequestTypeLkp.CONNECT);
	
	public static final List<Long> offSiteRequests = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE, SystemLookup.RequestTypeLkp.CONNECT);
	
	public static final List<Long> itemRequests = Arrays.asList(SystemLookup.RequestTypeLkp.CONVERT_TO_VM, 
			SystemLookup.RequestTypeLkp.ITEM_MOVE, 
			SystemLookup.RequestTypeLkp.ITEM_OFF_SITE, 
			SystemLookup.RequestTypeLkp.ITEM_ON_SITE, 
			SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, 
			SystemLookup.RequestTypeLkp.ITEM_POWER_ON, 
			SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE, 
			SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE, 
			SystemLookup.RequestTypeLkp.NEW_ITEM);
	
	@SuppressWarnings("serial")
	public static final Map<Long, List<Long>> nonConflictingRequestType = 
			Collections.unmodifiableMap(new HashMap<Long, List<Long>>() {{
				put(SystemLookup.RequestTypeLkp.CONVERT_TO_VM, installRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_MOVE, moveRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE, offSiteRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_ON_SITE, installRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, powerOffRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_POWER_ON, installRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE, archiveRequests);
				put(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE, storageRequests);
				put(SystemLookup.RequestTypeLkp.NEW_ITEM, installRequests);
				put(SystemLookup.RequestTypeLkp.CONNECT, connectRequests);
				put(SystemLookup.RequestTypeLkp.DISCONNECT, disconnectRequests);
				
			}});

	public static boolean allRequestIncluded(List<Request> masterRequests, List<Request> subSetRequests) {
		
		List<Long> masterRequestsId = new ArrayList<Long>();
		for (Request masterRequest: masterRequests) {
			masterRequestsId.add(masterRequest.getRequestId());
		}
		
		List<Long> subsetRequestsId = new ArrayList<Long>();
		for (Request subsetRequest: subSetRequests) {
			subsetRequestsId.add(subsetRequest.getRequestId());
		}
		
		if (masterRequestsId.containsAll(subsetRequestsId)) return true;
		
		return false;
		
	}

	private static List<Request> getRequestList(Map<Long, List<Request>> requestsMap) {

		List<Request> requests = new ArrayList<Request>();
		
		for (Map.Entry<Long, List<Request>> entry: requestsMap.entrySet()) {
			
			List<Request> subsetRequests = entry.getValue();
			
			if (null == subsetRequests || subsetRequests.size() == 0) continue;
			
			requests.addAll(subsetRequests);
			
		}
		
		return requests;

	}
	
	public static boolean allChildrenIncluded(Long numOfChildren, Map<Long, List<Request>> requestsMap) {
		
		List<Request> requests = getRequestList(requestsMap);
		
		return (numOfChildren == requests.size());
		
	}
	
	public static boolean allRequestIncluded(List<Request> masterRequests, Map<Long, List<Request>> requestsMap) {

		if (null == masterRequests || masterRequests.size() == 0) return true;
		
		List<Request> requests = getRequestList(requestsMap);

		if (requests.size() == 0) return false;
		
		if (!allRequestIncluded(masterRequests, requests)) return false;
		
		return true;
		
	}

	
	public static String getConflictingRequestNumber(Request currentRequest, List<Request> requests, ItemDAO itemDAO) {
		
		String conflictingRequestNumber = null;
		
		
		// Long currentRequestTypeLkp = (null != currentRequest.getRequestTypeLookup()) ? currentRequest.getRequestTypeLookup().getLkpValueCode() : null;
		Long currentRequestTypeLkp = (null != currentRequest.getRequestTypeLookup()) ? getRequestTypeValueCode(currentRequest, itemDAO) : null;
		
		for (Request request: requests) {

			// Long requestTypeLkp = (null != request.getRequestTypeLookup()) ? request.getRequestTypeLookup().getLkpValueCode() : null;
			Long requestTypeLkp = (null != request.getRequestTypeLookup()) ? getRequestTypeValueCode(request, itemDAO) : null;
			
			// special handling for move requests
			if (request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
				// if installed, all install and power off states are allowed
				if (requestTypeLkp.equals(SystemLookup.RequestTypeLkp.NEW_ITEM)) {
					if (!  (RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.NEW_ITEM).contains(currentRequestTypeLkp) || 
							RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF).contains(currentRequestTypeLkp)) ) {
						conflictingRequestNumber = request.getRequestNo();
						return conflictingRequestNumber;
					}
				}
				
				// if power off, all valid power off request is allowed
				else if (requestTypeLkp.equals(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF)) {
					if (! RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF).contains(currentRequestTypeLkp)) {
						conflictingRequestNumber = request.getRequestNo();
						return conflictingRequestNumber;
					}
				}
				
			}
			else if (currentRequest.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
				// if installed, all install and power off states are allowed
				if (currentRequestTypeLkp.equals(SystemLookup.RequestTypeLkp.NEW_ITEM)) {
					if (! RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.NEW_ITEM).contains(requestTypeLkp)) {
						conflictingRequestNumber = request.getRequestNo();
						return conflictingRequestNumber;
					}
				}
				
				// if power off, all valid power off request is allowed
				else if (currentRequestTypeLkp.equals(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF)) {
					if (!  (RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.NEW_ITEM).contains(requestTypeLkp) || 
							RequestLookup.nonConflictingRequestType.get(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF).contains(requestTypeLkp)) ) {
						conflictingRequestNumber = request.getRequestNo();
						return conflictingRequestNumber;
					}
				} 
			}
			else if (!RequestLookup.nonConflictingRequestType.get(requestTypeLkp).contains(currentRequestTypeLkp)) {
				conflictingRequestNumber = request.getRequestNo();
				return conflictingRequestNumber;
			}
			
		}
		
		return conflictingRequestNumber;
		
	}
	
	public static String getConflictingRequestNumber(Request currentRequest, Map<Long, List<Request>> requestsMap, ItemDAO itemDAO) {
		
		String conflictingRequest = null;
		
		for (Map.Entry<Long, List<Request>> entry: requestsMap.entrySet()) {
			List<Request> requests = entry.getValue();
			
			conflictingRequest = getConflictingRequestNumber(currentRequest, requests, itemDAO);
			if (null != conflictingRequest) return conflictingRequest;
		}

		return null;
		
	}

	
	public static boolean itemInDesiredState(Item item, Request request, Long currentItemStatus, ItemDAO itemDAO) {
		
		Long itemStatus = item.getStatusLookup().getLkpValueCode();
		Long requestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		
		// If the request type is disconnect a circuit: the circuit can be disconnected in all item state and therefore the map will return null.
		// null means request valid for all item status
		if (null == requestDesiredStatus) return true;

		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(request.getRequestTypeLookup().getLkpValueCode());
		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(getRequestTypeValueCode(request, itemDAO));
		List<Long> validItemStatus = getValidItemStatus(request, itemDAO);
		// getRequestTypeValueCode(currentRequest, itemDAO)
		
		return validItemStatus.contains(itemStatus);
		
		// return itemStatus.equals(requestDesiredStatus);
		
	}
	
	public static boolean itemGoingToDesiredState(Item item, Request request, List<Request> requests, Long currentItemStatus, ItemDAO itemDAO) {
		
		Long requestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(request.getRequestTypeLookup().getLkpValueCode());
		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(getRequestTypeValueCode(request, itemDAO));
		List<Long> validItemStatus = getValidItemStatus(request, itemDAO);
		boolean anyItemRequest = false;
		
		// If the request type is disconnect a circuit: the circuit can be disconnected in all item state and therefore the map will return null.
		// null means request valid for all item status
		if (null == requestDesiredStatus) return true;
		
		for (Request itemRequest: requests) {
			//if (itemRequests.contains(itemRequest.getRequestTypeLookup().getLkpValueCode()) && itemRequest.getItemId().equals(item.getItemId())) {
			if (itemRequests.contains(getRequestTypeValueCode(itemRequest, itemDAO)) && itemRequest.getItemId().equals(item.getItemId())) {
				anyItemRequest = true;
				Long itemRequestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO);//RequestLookup.itemRequestTypeAndFinalStatusMap.get(itemRequest.getRequestTypeLookup().getLkpValueCode());
				/*if (!itemRequestDesiredStatus.equals(requestDesiredStatus)) return false;*/
				if (!validItemStatus.contains(itemRequestDesiredStatus)) return false;
			}
			
		}
		
		return anyItemRequest;
		
	}

	public static boolean itemGoingToUndesiredState(Item item, Request request, List<Request> requests, Long currentItemStatus, ItemDAO itemDAO) {
		
		Long requestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(request.getRequestTypeLookup().getLkpValueCode());
		// List<Long> validItemStatus = RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(getRequestTypeValueCode(request, itemDAO));
		List<Long> validItemStatus = getValidItemStatus(request, itemDAO);
		
		// If the request type is disconnect a circuit: the circuit can be disconnected in all item state and therefore the map will return null.
		// null means request valid for all item status
		if (null == requestDesiredStatus) return true;
		
		for (Request itemRequest: requests) {
			//if (itemRequests.contains(itemRequest.getRequestTypeLookup().getLkpValueCode()) && itemRequest.getItemId().equals(item.getItemId())) {
			if (itemRequests.contains(getRequestTypeValueCode(itemRequest, itemDAO)) && itemRequest.getItemId().equals(item.getItemId())) {
				Long itemRequestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(itemRequest.getRequestTypeLookup().getLkpValueCode());
				/*if (!itemRequestDesiredStatus.equals(requestDesiredStatus)) return true;*/
				if (!validItemStatus.contains(itemRequestDesiredStatus)) return true;
			}
			
		}
		
		return false;
		
	}

	
	public static boolean inCorrectStateOrRequest(Request request, List<Long> childrenItemIds, Map<Long, Long> statusList, Map<Long, List<Request>> childrenPendingRequests, List<Request> reqToProcessList, Long currentItemStatus, ItemDAO itemDAO) {
		
		// TODO:: if 'request' is a move request, then return true
		
		Long requestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		boolean retValue = true;

		
		//TODO: For future, when only approving a request,  we might need the list childrenPendingRequests and not the reqToProcessList
		//this is not applicable when using request bypass
		for (Long itemId: childrenItemIds) {
			Long status = statusList.get(itemId);
			
			if(status.equals(requestDesiredStatus)) continue;
			
			retValue = false;
			
			//Check if item is in list of request to be processed
			for(Request r:reqToProcessList){
				if(itemId.equals(r.getItemId())){
					Long futureStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); //RequestLookup.itemRequestTypeAndFinalStatusMap.get(r.getRequestTypeLookup().getLkpValueCode());
					
					if(futureStatus != null && futureStatus.equals(requestDesiredStatus)){
						retValue = true;
						break;
					}
				}
			}
			
			if(!retValue) break;
		}
		
		return retValue;
		
	}
	
	public static boolean statusMatchRequestState(List<Long> statusList, Request request, Long currentItemStatus, ItemDAO itemDAO) {
		
		Long requestDesiredStatus = getRequestDesiredStatus(request, currentItemStatus, itemDAO); // RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		
		for (Long status: statusList) {
			if (!status.equals(requestDesiredStatus)) return false;
		}
		
		return true;
		
	}
	
	public static String getDesiredStatus(Request request, ItemDAO itemDAO) {

		// return RequestLookup.itemStatusToErrorString.get(RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode()));
		return RequestLookup.itemStatusToErrorString.get(RequestLookup.itemRequestTypeAndFinalStatusMap.get(getRequestTypeValueCode(request, itemDAO) ).get(0) );

	}
	
	public static String getDesiredSourceStatus(Request request, ItemDAO itemDAO) {

		// return RequestLookup.itemStatusToErrorString.get(RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode()));
		String errorString = RequestLookup.itemStatusToErrorString.get(RequestLookup.itemRequestTypeAndFinalStatusMap.get(getRequestTypeValueCode(request, itemDAO) ).get(0) );
		
		if (request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
			errorString = "Moved(" + errorString + ")";
		}
		
		return errorString;
	}
	
	public static String getItemClassSubclass(Item item) {
		String itemClass = item.getClassLookup().getLkpValue();
		
		String itemSubClass = (null != item.getSubclassLookup()) ? item.getSubclassLookup().getLkpValue() : null;
		
		return itemClass + ((null != itemSubClass) ? " / " + itemSubClass : "");
	}

	public static String getItemStatus(Item item) {
		return (null != item.getStatusLookup()) ? item.getStatusLookup().getLkpValue() : "";
	}

	private static Long getRequestDesiredStatus(Request request, Long currentItemStatus, ItemDAO itemDAO) {
		
		// TODO:: if move request then fake the request type based on the current item status for 'getRequestTypeLookup().getLkpValueCode()'
		// List<Long> requestDesiredStatus = RequestLookup.itemRequestTypeAndFinalStatusMap.get(request.getRequestTypeLookup().getLkpValueCode());
		List<Long> requestDesiredStatus = RequestLookup.itemRequestTypeAndFinalStatusMap.get(getRequestTypeValueCode(request, itemDAO));
		
		if (null == requestDesiredStatus) return null;
			
		if (requestDesiredStatus.size() == 1) return requestDesiredStatus.get(0);

		return currentItemStatus;
		
	}
	
	private static Long getRequestTypeValueCode(Request request, ItemDAO itemDAO) {
		
		Map<Long, Long> currentItemStatusMap = itemDAO.getItemsStatus(Arrays.asList(request.getItemId()));
		Long currentItemStatus = currentItemStatusMap.get(request.getItemId());
		
		Long requestTypeLkpVC = request.getRequestTypeLookup().getLkpValueCode();
		
		// for the move request there desired status can be Installed or Power-Off, we need to get the corresponding status change request type
		if (requestTypeLkpVC.equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
			
			Long fakeMoveRequestType = itemMoveRequestToFakeRequestType.get(currentItemStatus);
			if (null != fakeMoveRequestType) 
				return fakeMoveRequestType;
		}
		
		return requestTypeLkpVC;
		
	}
	
	private static List<Long> getValidItemStatus(Request request, ItemDAO itemDAO) {

		if (request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
			Map<Long, Long> currentItemStatusMap = itemDAO.getItemsStatus(Arrays.asList(request.getItemId()));
			Long currentItemStatus = currentItemStatusMap.get(request.getItemId());
			
			if (currentItemStatus.equals(SystemLookup.ItemStatus.INSTALLED)) {
				return Arrays.asList(SystemLookup.ItemStatus.INSTALLED);
			}
			else if (currentItemStatus.equals(SystemLookup.ItemStatus.POWERED_OFF)) {
				return Arrays.asList(SystemLookup.ItemStatus.INSTALLED, SystemLookup.ItemStatus.POWERED_OFF);
			}
		}

		return RequestLookup.itemRequestTypeAndFinalStatusCheckMap.get(getRequestTypeValueCode(request, itemDAO));
		
	}

}
