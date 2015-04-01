/**
 * 
 */
package com.raritan.tdz.item.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 *
 */
public class ItemRequestImpl implements ItemRequest {

	private ItemRequestDAO requestDAO;
	private Errors errors;

	@Autowired
	private ItemDAO itemDao;
	
	@Autowired(required=true)
	private LksCache lksCache;
	
	@Autowired(required=true)
	private CircuitRequest circuitRequest;

	@Autowired
	private ResourceBundleMessageSource messageSource;
	
	// @Autowired
	private Validator validatePermission;
	
	private static ArrayList<Long> invalidFSContainerStatus = new ArrayList<Long>(){{
		add(SystemLookup.ItemStatus.POWERED_OFF);
		add(SystemLookup.ItemStatus.OFF_SITE);
	}};
	
	ItemRequestImpl(ItemRequestDAO requestDAO, Validator validatePermission){
		this.requestDAO = requestDAO;
		this.validatePermission = validatePermission;
		clearErrors();
	}
	
	/* convertToVMRequest
	 * This request create two request: "Place In Storage" and "New Item" requests 
	 */
	@Override
	public Map<Long, Long> convertToVMRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.decomissionToStorage, "Decommission to Storage", errors, true, SystemLookup.ItemStatus.IN_STORAGE, false);
			
			
			List<Long> tempList = new ArrayList<Long>();
			Long vmItemId;
			
			for(Long itemId:itemIds){	
				if(requestIdMap.containsKey(itemId) == false) continue;

				Request requestDS = requestDAO.loadRequest(requestIdMap.get(itemId));

				vmItemId = requestDAO.createVmItem(itemId);	
				
				tempList.add(vmItemId);
				
				Map<Long,Long> vmRequest = requestDAO.insertRequests(tempList, ItemRequestType.installItem, "Install Item", errors, false, SystemLookup.ItemStatus.INSTALLED, false);
				
				if(vmRequest.containsKey(vmItemId) == false) continue;
								
				requestIdMap.putAll(vmRequest);
				
				//Append " Converted to VM" to request description for VM Item
				Request request = requestDAO.loadRequest(vmRequest.get(vmItemId));
				request.setDescription(request.getDescription() + " Converted to VM");
				request.setRequestTypeLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.RequestTypeLkp.CONVERT_TO_VM));
				
				//Update Request Number to Match Decommission to Storage				
				request.setRequestNo(requestDS.getRequestNo() + "-00");
				requestDAO.insertOrUpdateRequest(request, errors);
				
				tempList.clear();
			}
			
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#takeItemOffsiteRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> takeItemOffsiteRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "Take Off-site", errors, false, SystemLookup.ItemStatus.OFF_SITE, false);
			                                    
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#bringItemOnsiteRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> bringItemOnsiteRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "Bring On-site", errors, false, SystemLookup.ItemStatus.INSTALLED, false);
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#powerOffItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> powerOffItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "Power-off", errors, false, SystemLookup.ItemStatus.POWERED_OFF, false);
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#powerOnItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> powerOnItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "Power-on", errors, false, SystemLookup.ItemStatus.INSTALLED, false);
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#decommisionItemToStorageRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> decommisionItemToStorageRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.decomissionToStorage, "Decommission to Storage", errors, true, SystemLookup.ItemStatus.IN_STORAGE, true);
			
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#decommisionItemToArchiveRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> decommisionItemToArchiveRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.decomissionToArchive, "Decommission to Archive", errors, true, SystemLookup.ItemStatus.ARCHIVED, true);
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#installItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> installItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		try {
			requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "Install Item", errors, false, SystemLookup.ItemStatus.INSTALLED, false);
			
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#resubmitRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> resubmitRequest(List<Long> requestIds, UserInfo user) throws BusinessValidationException, DataAccessException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		
		//For each item let us process the request
		for (Long requestId : requestIds){
			requestDAO.reSubmitRequest(requestId);
			
			requestIdMap.put(requestId, SystemLookup.RequestStage.REQUEST_UPDATED);
			
		}
		
		return requestIdMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getRequests(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, List<Request>> getRequests(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub
		Map<Long, List<Request>> requestItemMap = new HashMap<Long, List<Request>>();
		requestItemMap = requestDAO.getRequest(itemIds, errors);
		return requestItemMap;
	}

	@Override
	public Map<Long, List<Request>> getRequests(List<Long> itemIds,
			List<Long> requestStages, UserInfo user)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub
		Map<Long, List<Request>> requestItemMap = new HashMap<Long, List<Request>>();
		requestItemMap = requestDAO.getRequest(itemIds, requestStages, errors);
		return requestItemMap;
	}

	@Override
	public List<Request> getRequests(Long itemId, List<Long> requestStages) throws BusinessValidationException, DataAccessException {
		List<Request> recList = requestDAO.getRequest(itemId, requestStages, errors);
		return recList;
	}
	
	@Override
	public List<Long> getItemRequestStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException {
		
		return requestDAO.getItemRequestStages(itemId, requestStageValueCodes);
		
	}
	
	@Override
	public boolean itemRequestExistInStages(long itemId, List<Long> requestStageValueCodes) throws DataAccessException {
		
		return requestDAO.itemRequestExistInStages(itemId, requestStageValueCodes);
		
	}
	
	@Override
	public LksData getLatestRequestStage(Long itemId)
			throws DataAccessException {
		
		LksData requestStageLookup = null;
		RequestHistory requestHistory = requestDAO.getCurrentHistory(itemId);
		if (null != requestHistory) {
			requestStageLookup = requestHistory.getStageIdLookup();
		}
		
		return requestStageLookup;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getErrors()
	 */
	@Override
	public Errors getErrors() {
		// TODO Auto-generated method stub
		return errors;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#clearErrors(org.springframework.validation.Errors)
	 */
	@Override
	public void clearErrors() {
		Map<String, String> errorMap = new HashMap<String, String>();
		errors = new MapBindingResult(errorMap, ItemRequest.class.getName());
	}

	@Override
	public Map<Long, Long> moveItemRequest(Map<Long, Long> itemIds, UserInfo user, boolean disconnect) throws BusinessValidationException {
		Map<Long,Long> requestIdMap = new HashMap<Long, Long>();
		
		try {
			List<Long> itemToMoveIds = new ArrayList<Long>(itemIds.keySet());
			requestIdMap = requestDAO.insertRequests(itemToMoveIds, ItemRequestType.moveItem, "Move", errors, disconnect, null, false);
			
		} catch (DataAccessException e) {
			//We dont really need to process this exception since errors will be filled up.
		}
		
		return requestIdMap;	
	}

	@Override
	public Boolean getIsMoveRequestAllowed(List<Integer> itemIds, UserInfo userInfo) throws BusinessValidationException, DataAccessException {
		Boolean isAllowed = true;
		List<Long> allowedIds = new ArrayList<Long>();

		//Find pending request for this item
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		
		if (itemIds == null) return isAllowed;
		for (Integer id : itemIds) {
			if (id != null && id.longValue() > 0) {
				String itemName = itemDao.getItemName(id);
				List<Request> reqList = getRequests(id.longValue(), requestStages);
				
				// check if item has pending request
				if(reqList != null & reqList.size() > 0) { //resubmit request
					Request request = reqList.get(0);
					createPendingRequestError (itemName, request, errors);
				}
				// check if circuit has pending request
				circuitRequest.hasConnectionRequests(new Long(id), errors);
				
				if (errors.hasErrors() == false){
					allowedIds.add(id.longValue());
				}
			}
		}
		
		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			for (ObjectError error: objectErrors) {
				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg );
			}
			clearErrors();
			throw e;
		}

		List<Integer> permTestedList = new ArrayList<Integer>();
		permTestedList.addAll(itemIds);
		
		// first check permission of the cabinet, if not permitted throw the permission error only for the cabinet
		// 													if permitted, check for all children
		for (Integer itemId: itemIds) {

			Errors permErrors = getErrorObject(errors);
			
			Map<String, Object> target = new HashMap<String, Object>();
			target.put(Item.class.getName(), itemId.longValue());
			target.put(UserInfo.class.getName(), userInfo);
			validatePermission.validate(target, permErrors);

			if (permErrors.hasErrors()) {
				errors.addAllErrors(permErrors);
				// Do not test all the children of the cabinet if the parent do not have the permission
				if (itemDao.getItemClass(itemId).equals(SystemLookup.Class.CABINET)) {
					permTestedList.removeAll(Arrays.asList(itemId));
				}
			}
		}
		
		if (permTestedList.size() > 0) {
			// if itemId is parent itemId, then get all child items for deletion
			List itemIdList = itemDao.getItemIdsToDelete(permTestedList);
	
			// add power panels to the list if the parent item id is not set for the panel
			addPanelsUsingConnections(permTestedList, itemIdList);
	
			//add current items to list
			itemIdList.removeAll(itemIds);
			// itemIdList.addAll(itemIds);
	
			for (Object id : itemIdList) {
				if (null == id) continue;
				Long itemId = null;
				if (id instanceof Long) itemId = (Long) id;
				else if (id instanceof Integer) itemId = ((Integer) id).longValue();
	
				Map<String, Object> target = new HashMap<String, Object>();
				target.put(Item.class.getName(), itemId);
				target.put(UserInfo.class.getName(), userInfo);
				validatePermission.validate(target, errors);
		
			}
		}
		
		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			for (ObjectError error: objectErrors) {
				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg );
			}
			clearErrors();
			throw e;
		}
		
		return isAllowed;
	}
	
	private void addPanelsUsingConnections(List itemIds, List primaryItemIdList) {
		
		List<Long> panelItemIds = itemDao.getPanelItemIdsToDelete(itemIds);
		
		for (Long panelItemId: panelItemIds) {
			if (!primaryItemIdList.contains(panelItemId)) {
				primaryItemIdList.add(0, panelItemId);
			}
		}
		
	}
	
	private void createPendingRequestError(String itemName, Request request, Errors errors) throws BusinessValidationException {
		String requestNumber = request.getRequestNo();
		String requestDescription = request.getDescription();
		Long itemSubclass = itemDao.getItemSubClass(request.getItemId());
		Long itemClass = itemDao.getItemClass(request.getItemId());
		String requestDesc = RequestTypesLookup.RequestType.PREPARE_MOVE_ITEM_REQUEST + "/" + RequestTypesLookup.RequestType.QUICK_MOVE_ITEM_REQUEST;
		if (null != itemSubclass && (itemSubclass.equals(SystemLookup.SubClass.BLADE) || 
				itemSubclass.equals(SystemLookup.SubClass.BLADE_SERVER) ) ) {
			requestDesc = RequestTypesLookup.RequestType.QUICK_MOVE_ITEM_REQUEST;
		}
		if (null != itemClass && itemClass.equals(SystemLookup.Class.CABINET)) {
			requestDesc = RequestTypesLookup.RequestType.QUICK_MOVE_ITEM_REQUEST;
		}
		Object errorArgs[] = {itemName, requestNumber, requestDescription, itemName, requestDesc};
		String errorCode = "itemRequest.pendingMoveRequest.sameRequestType";
		if (null != request.getRequestTypeLookup() && !request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
			errorCode = "itemRequest.pendingMoveRequest.differentRequestType";
		}
		errors.reject(errorCode, errorArgs, "Could not submit request for item");	
	}

	@Override
	public void setItemStatus(Long itemId, Long statusValueCode) throws Throwable{
		LksData statusLks = lksCache.getLksDataUsingLkpCode(statusValueCode);		
		Item item = itemDao.read(itemId);		
		
		//Data Panel and Floor Outlet are not put in storage, status set to Planned
		if(statusValueCode.equals(SystemLookup.ItemStatus.IN_STORAGE)){
			if(item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.DATA_PANEL) ||
			   item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_OUTLET)){
				statusLks = lksCache.getLksDataUsingLkpCode(SystemLookup.ItemStatus.PLANNED);
			}
		}
		
		item.setStatusLookup(statusLks);		
		itemDao.saveItem(item);
		
		//Handle FreeStanding Item
		if(syncFreeStandingStatus(item)){
			return;
		}
		
		//Handle Cabinet/Passive 
		syncCabinetStatus(item);
		
		//Handle FPDU/Power Panel 
		syncFloorPduStatus(item);
	}	
	
	private boolean syncFreeStandingStatus(Item item){
		Long itemId = item.getItemId();
		Long fsItemId = itemDao.getFreeStandingItemIdForItem(itemId);
		
		if(fsItemId != null && fsItemId > 0){
			if(fsItemId.equals(itemId)){//update cabinet/container
				if(item.getParentItem() != null){
					Long currentStatusLkpValueCode = item != null && item.getStatusLookup() != null ?
							item.getStatusLookup().getLkpValueCode() : null;
					if (currentStatusLkpValueCode != null &&
							!invalidFSContainerStatus.contains(currentStatusLkpValueCode)) {
						item.getParentItem().setStatusLookup(item.getStatusLookup());		
						itemDao.saveItem(item.getParentItem());
					}
				}
			}
			else{ //request was on container
				Item item2 = itemDao.read(fsItemId);		
				item2.setStatusLookup(item.getStatusLookup());		
				itemDao.saveItem(item2);
			}
			return true;
		}
		
		return false;
	}
	
	private boolean syncCabinetStatus(Item item) throws Throwable{
		//Handle Cabinet/Passive 
		if(item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)){
			for(Item item2:itemDao.getChildrenItems(item)){
				if(item2.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.PASSIVE)){
					item2.setStatusLookup(item.getStatusLookup());		
					itemDao.saveItem(item2);					
				}
			}
			return true;
		}
		
		return false;
	}
	
	private boolean syncFloorPduStatus(Item item) throws Throwable{
		if(item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)){
			for(Item item2:itemDao.getChildrenItems(item)){
				if(item2.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)){
					item2.setStatusLookup(item.getStatusLookup());		
					itemDao.saveItem(item2);					
				}
			}
			return false;
		}		
		
		return false;
	}
	
	private MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, (null != refErrors) ? refErrors.getObjectName() : Request.class.getName() );
		return errors;
		
	}


}
