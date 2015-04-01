package com.raritan.tdz.move.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * 
 * @author bunty
 *
 */
public class ItemMoveRequestBehavior implements ItemSaveBehavior {

	private ItemRequest itemRequest;
	
	@Autowired
	private ResourceBundleMessageSource messageSource;
	
	@Autowired
	private ItemDAO itemDAO;
	
	public ItemMoveRequestBehavior(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		@SuppressWarnings("unused")
		Errors errors = (Errors) additionalArgs[0];
		
		if (null == item.getItemToMoveId()) { //do nothing
			return;
		}
				
		//Find pending request for this item
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		
		List<Request> reqList = itemRequest.getRequests(item.getItemToMoveId(), requestStages);
		
		if(reqList != null & reqList.size() > 0){ //resubmit request
			List<Long> tempList = new ArrayList<Long>();
			Request request = reqList.get(0);

			//isPendingRequest (item, request, errors); at this point, we know that is a move request -SANTO
			
			tempList.add(request.getRequestId());

			itemRequest.resubmitRequest(tempList, sessionUser);
			
			return;
		}

		requestStages.clear();
		
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		
		reqList.clear();
		reqList = itemRequest.getRequests(item.getItemToMoveId(), requestStages);
		
		if(reqList != null & reqList.size() > 0){ // do not do anything to the request that is been approved
			return;
		}
		
		//create a new request
		//map item to be move to new/current item that was saved
		Map<Long,Long> itemIds = new HashMap<Long,Long>();
		
		itemIds.put(item.getItemToMoveId(), item.getItemId());
		
		//if there is a pending request, code that create request will generate a validation error 
		itemRequest.moveItemRequest(itemIds, sessionUser, true);
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void isPendingRequest(Item item, Request request, Errors errors) throws BusinessValidationException {
		
		if (request == null) return;

		String itemName = itemDAO.getItemName(item.getItemToMoveId());
		String requestNumber = request.getRequestNo();
		String requestDescription = request.getDescription();

		if (!request.getRequestType().equals("Item Move")) {
			Object errorArgs[] = {itemName, requestNumber, requestDescription};
			errors.reject("itemRequest.pendingRequest.sameRequestType", errorArgs, "Could not submit request for item");	
			throwBusinessValidationException("itemRequest.pendingRequest.sameRequestType", errorArgs, null);
		}
	}
	
	private void throwBusinessValidationException(String code, Object[] args, Locale locale ) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		String msg = messageSource.getMessage(code, args, locale);
		e.addValidationError( msg);
		e.addValidationError(code, msg);
		throw e;
	}

}
