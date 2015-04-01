package com.raritan.tdz.request.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestLookup;

/**
 * 
 * @author bunty
 *
 */
public class CheckChildConflict implements ValidateConflictState {

	@Autowired(required=true)
	private ItemDAO itemDAO;

	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	private Validator permissionValidator;

	

	public CheckChildConflict(Validator permissionValidator) {
		
		super();
		
		this.permissionValidator = permissionValidator;
	}



	@Override
	public void validate(Item item, Request request, List<Request> requests,
			Errors errors, String itemName, UserInfo userInfo) throws DataAccessException {

		Long currentItemStatus = item.getStatusLookup().getLkpValueCode();
		List<Item> children = itemDAO.getChildrenItems(item);
		Collection<ItItem> blades = itemDAO.getAllBladesForChassis(item.getItemId());
		if (null != blades) {
			children.addAll(blades);
		}
		
		if (null == children || children.size() == 0) return;
		
		// List<Long> childrenStatus = itemDAO.getChildrenItemsStatus(item); 
		
		List<Long> childrenItemIds = itemDAO.getChildItemIds(item.getItemId());
		
		List<Long> passiveChildren = itemDAO.getPassiveChildItemIds(item.getItemId());
		
		childrenItemIds.removeAll(passiveChildren);
		
		if (childrenItemIds.size() == 0) return;
		
		// If any child is a -when-moved item then include the original item against the -when-moved item and then remove -when-moved from the list 
		List<Long> movingItemIds = powerPortMoveDAO.getMovingItemId(childrenItemIds);
		if (null != movingItemIds) {
			childrenItemIds.removeAll(movingItemIds);
			childrenItemIds.addAll(movingItemIds);
		}
		List<Long> whenMovedItems = powerPortMoveDAO.getWhenMovedItemId((List<Long>)null);
		childrenItemIds.removeAll(whenMovedItems);
		
		//Find pending request for this parent item
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		
		Map<Long, List<Request>> childrenPendingRequests = itemRequestDAO.getRequest(childrenItemIds, requestStages, errors);

		Map<Long, Long> childrenStatus = itemDAO.getItemsStatus(childrenItemIds);
		
		// Child that is in correct state and no request - good
		// Child that is in incorrect state and request - good
		
		/*if (!RequestLookup.statusMatchRequestState(childrenStatus, request) && 
				(null == childrenPendingRequests || childrenPendingRequests.size() == 0 || !RequestLookup.allChildrenIncluded(new Long(children.size()), childrenPendingRequests) || 
				!RequestLookup.allRequestIncluded(requests, childrenPendingRequests))) {
			String desiredStatus = RequestLookup.getDesiredStatus(request);
			Object[] errorArgs = { itemName, desiredStatus, RequestLookup.getItemClassSubclass(children.get(0)), desiredStatus, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ChildrenStatusConflict", errorArgs, "Item request cannot be completed because its children has conflicting pending request.");
			return;

		}*/
		
		// TODO:: Add or condition for move request on cabinet
		if (!RequestLookup.inCorrectStateOrRequest(request, childrenItemIds, childrenStatus, childrenPendingRequests, requests, currentItemStatus, itemDAO)) {

			String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
			String desiredSrcstatus = RequestLookup.getDesiredSourceStatus(request, itemDAO);
			Object[] errorArgs = { itemName, desiredSrcstatus, RequestLookup.getItemClassSubclass(children.get(0)), desiredStatus, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ChildrenStatusConflict", errorArgs, "Item request cannot be completed because its children has conflicting pending request.");
			return;
		}
		
		String conflictingRequestNumber = RequestLookup.getConflictingRequestNumber(request, childrenPendingRequests, itemDAO);
		if (null != conflictingRequestNumber) {
			String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
			Object[] errorArgs = { itemName, desiredStatus, RequestLookup.getItemClassSubclass(children.get(0)), conflictingRequestNumber, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ChildrenRequestConflict", errorArgs, "Item request cannot be completed because its children has conflicting pending request.");
			return;
		}

		String conflictingChildName = checkChildrenPermission(childrenItemIds, requests, userInfo, errors);
		if (null != conflictingChildName) {
			String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
			Object[] errorArgs = { itemName, desiredStatus, conflictingChildName, null, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ChildrenPermissionConflict", errorArgs, "Item request cannot be completed because its child has conflicting pending permission.");
			return;
		}

		
		
	}
	
	private String checkChildrenPermission(List<Long> childrenItemIds, List<Request> requests, UserInfo userInfo, Errors refErrors) {
		
		String itemName = null;
		
		if (null == childrenItemIds || childrenItemIds.size() == 0) return itemName;
		
		if (null == requests || requests.size() == 0) return itemName;
		
		Errors errors = getErrorObject(refErrors);
		
		for (Request request: requests) {
			
			Long requestItemId = request.getItemId();
			
			if (childrenItemIds.contains(requestItemId)) {
				
				Map<String, Object> targetMap = new HashMap<String, Object>();
				targetMap.put(Item.class.getName(), requestItemId);
				targetMap.put(UserInfo.class.getName(), userInfo);
				
				permissionValidator.validate(targetMap, errors);
				
				if (errors.hasErrors()) {
					itemName = itemDAO.getItemName(requestItemId);
					break;
				}
				
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
