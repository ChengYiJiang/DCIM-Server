package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestLookup;

public class CheckItemConflict implements ValidateConflictState {

	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired
	private ItemDAO itemDAO;

	String errorCodePostFix;
	
	
	
	public String getErrorCodePostFix() {
		return errorCodePostFix;
	}

	public void setErrorCodePostFix(String errorCodePostFix) {
		this.errorCodePostFix = errorCodePostFix;
	}

	public CheckItemConflict(String errorCodePostFix) {
		super();
		this.errorCodePostFix = errorCodePostFix;
	}

	@Override
	public void validate(Item item, Request request, List<Request> requests,
			Errors errors, String itemName, UserInfo userInfo) throws DataAccessException {

		Long currentItemStatus = item.getStatusLookup().getLkpValueCode();
		//Find pending request for this parent item
		Long[] requestStages = new Long[] { SystemLookup.RequestStage.REQUEST_ISSUED,
											SystemLookup.RequestStage.REQUEST_REJECTED, 
											SystemLookup.RequestStage.REQUEST_UPDATED,
											SystemLookup.RequestStage.REQUEST_APPROVED,
											SystemLookup.RequestStage.WORK_ORDER_ISSUED };
		
		// List<Request> itemRequests = itemRequestDAO.getRequest(item.getItemId(), requestStages, errors);
		List<Request> allItemRequests = requestDAO.getRequestForItem(item.getItemId(), requestStages);
		
		boolean itemInDesiredState = RequestLookup.itemInDesiredState(item, request, currentItemStatus, itemDAO);
		boolean itemGoingToDesiredState = RequestLookup.itemGoingToDesiredState(item, request, requests, currentItemStatus, itemDAO);
		boolean itemGoingToUndesiredState = RequestLookup.itemGoingToUndesiredState(item, request, requests, currentItemStatus, itemDAO);

		/*if (!RequestLookup.itemInDesiredState(item, request) && !RequestLookup.itemGoingToDesiredState(item, request, requests) &&
				(null == allItemRequests || allItemRequests.size() == 0 || !RequestLookup.allRequestIncluded(requests, allItemRequests)))*/
		if ((!itemInDesiredState && !itemGoingToDesiredState) || (itemInDesiredState && itemGoingToUndesiredState)) {
			String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
			String desiredSrcstatus = RequestLookup.getDesiredSourceStatus(request, itemDAO);
			Object[] errorArgs = { itemName, desiredSrcstatus, RequestLookup.getItemClassSubclass(item), desiredStatus, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ItemStatusConflict." + errorCodePostFix, errorArgs, "Item request cannot be completed because its parent has conflicting pending request.");
			return;
		}
		
		String conflictingRequestNumber = RequestLookup.getConflictingRequestNumber(request, allItemRequests, itemDAO);
		if (null != conflictingRequestNumber) {
			String desiredStatus = RequestLookup.getDesiredStatus(request, itemDAO);
			Object[] errorArgs = { itemName, desiredStatus, RequestLookup.getItemClassSubclass(item), conflictingRequestNumber, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.ItemRequestConflict." + errorCodePostFix, errorArgs, "Item request cannot be completed because its parent has conflicting pending request.");
			return;
		}

	}

}
