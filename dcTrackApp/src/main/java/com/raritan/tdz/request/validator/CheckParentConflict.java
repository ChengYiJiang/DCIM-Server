package com.raritan.tdz.request.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.home.RequestLookup;

/**
 * 
 * @author bunty
 *
 */
public class CheckParentConflict implements ValidateConflictState {

	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;

	private Validator permissionValidator;

	
	
	public CheckParentConflict(Validator permissionValidator) {
	
		super();
		
		this.permissionValidator = permissionValidator;
		
	}

	@Override
	public void validate(Item item, Request request, List<Request> requests, Errors errors, String itemName, UserInfo userInfo) throws DataAccessException {
		
		if(isFreeStanding(item)) return;  //don't check
		
		Long currentItemStatus = item.getStatusLookup().getLkpValueCode();
		
		Item parentOfItem = null;
		if (null != item.getSubclassLookup() && (item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
			item = itemDAO.initializeAndUnproxy(item);
			parentOfItem = ((ItItem)item).getBladeChassis();
		}
		else {
			parentOfItem = item.getParentItem();
		}

		while (null != parentOfItem) {
			//Find pending request for this parent item
			List<Long> requestStages = new ArrayList<Long>();
			requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
			requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
			requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
			requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
			requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			
			List<Request> parentPendingRequests = itemRequestDAO.getRequest(parentOfItem.getItemId(), requestStages, errors);
	
			if (!RequestLookup.itemInDesiredState(parentOfItem, request, currentItemStatus, itemDAO) && 
					(null == parentPendingRequests || parentPendingRequests.size() == 0 || !RequestLookup.allRequestIncluded(requests, parentPendingRequests))) {
				String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
				String desiredSrcstatus = RequestLookup.getDesiredSourceStatus(request, itemDAO);
				Object[] errorArgs = { itemName, desiredSrcstatus, RequestLookup.getItemClassSubclass(parentOfItem), desiredStatus, request.getRequestNo(), request.getDescription() };
				errors.rejectValue("item", "Request.ParentStatusConflict", errorArgs, "Item request cannot be completed because its parent has conflicting pending request.");
				return;
			}
			
			String conflictingRequestNumber = RequestLookup.getConflictingRequestNumber(request, parentPendingRequests, itemDAO);
			if (null != conflictingRequestNumber) {
				String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
				Object[] errorArgs = { itemName, desiredStatus, RequestLookup.getItemClassSubclass(parentOfItem), conflictingRequestNumber, request.getRequestNo(), request.getDescription() };
				errors.rejectValue("item", "Request.ParentRequestConflict", errorArgs, "Item request cannot be completed because its parent has conflicting pending request.");
				return;
			}
			
			String conflictingParentName = checkParentPermission(parentOfItem.getItemId(), requests, userInfo, errors);
			if (null != conflictingParentName) {
				String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
				Object[] errorArgs = { itemName, desiredStatus, conflictingParentName, null, request.getRequestNo(), request.getDescription() };
				errors.rejectValue("item", "Request.ParentPermissionConflict", errorArgs, "Item request cannot be completed because its parent has conflicting permission.");
				return;

			}
			
			parentOfItem = parentOfItem.getParentItem();
		}
		
		return;

	}
	
	private boolean isFreeStanding(Item item){
		Item cabinet = item.getParentItem();
		
		if(cabinet == null) return false;
		
		Long subClass = cabinet.getSubclassLookup() == null ? -1L : cabinet.getSubclassLookup().getLkpValueCode();
		
		if(subClass.equals(SystemLookup.SubClass.CONTAINER)) return true;
		
		return false;
	}
	
	
	private String checkParentPermission(Long parentItemId, List<Request> requests, UserInfo userInfo, Errors refErrors) {
		
		String itemName = null;
		
		if (null == parentItemId) return itemName;
		
		if (null == requests || requests.size() == 0) return itemName;
		
		Errors errors = getErrorObject(refErrors);
		
		for (Request request: requests) {
			
			Long requestItemId = request.getItemId();
			
			if (parentItemId.equals(requestItemId)) {
				
				Map<String, Object> targetMap = new HashMap<String, Object>();
				targetMap.put(Item.class.getName(), requestItemId);
				targetMap.put(UserInfo.class.getName(), userInfo);
				
				permissionValidator.validate(targetMap, errors);
				
				if (errors.hasErrors()) {
					itemName = itemDAO.getItemName(requestItemId);
				}
				
				break;
				
			}
			
		}
		
		return itemName;
		
	}
	
	private MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, refErrors.getObjectName() );
		return errors;
		
	}


}


