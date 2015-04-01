/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * This validates if the item is editable or not
 * <p>This expects the target as a map that contains
 * <ul>
 * 	<li>"Item", item</li>
 *  <li>"UserInfo", userInfo</li>
 * </ul>
 * </p>
 * @author prasanna
 *
 */
public class ItemEditabilityValidator implements Validator {

	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	private ItemRequest itemRequest;
	
	Logger log = Logger.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//validate the args
		validateArgs(target, errors);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		//Get the args
		Item item = (Item)itemDAO.read((Long) targetMap.get("itemId"));
		UserInfo userInfo = (UserInfo) targetMap.get("UserInfo");
		
		
		try {
			//Validate permission
			//TODO: We need to revisit the order in which we do permission checking
			//TODO: Current way of implementation is that first it checks if item and port exists
			//TODO: Then it basically takes care of permission
			//TODO: However the order should really be
			//TODO: First check if item exist, check permission and then check if port exist
			if (!isEditablePermissionForAnItem(item, userInfo)){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("ItemValidator.itemEditability.noPermission",errorArgs,"No permission to perform this operation");
			}
			
			//Validate request stage
			if (!errors.hasErrors() && !doesItemStageAllowModification(item.getItemId())){
				Object[] errorArgs = {item.getItemName(), getLatestStage(item.getItemId())};
				errors.reject("ItemValidator.itemEditability.incorrectStage",errorArgs,"Incorrect stage. Cannot edit item");
			}
			
		} catch (BusinessValidationException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			
			Map<String,String> errorMap = e.getErrors();
			for (Map.Entry<String, String> entry: errorMap.entrySet()){
				errors.reject(entry.getKey());
			}
			
		} catch (DataAccessException e) {
			errors.reject("ItemValidator.itemEditability.unknownError");
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}

	private void validateArgs(Object target, Errors errors) {
		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		if (targetMap.size() < 2)
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator." +
												" At least two entries must be present");
		
		if (!targetMap.containsKey("itemId")){
			throw new IllegalArgumentException("You must provide item domain object");
		}
		
		if (!targetMap.containsKey("UserInfo")){
			throw new IllegalArgumentException("You must provide userInfo");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
	}
	
	private Boolean isEditablePermissionForAnItem(Item item, UserInfo userInfo )  throws BusinessValidationException, DataAccessException {
	    return item != null && itemModifyRoleValidator.canTransition(item, userInfo);
	}
	
	private Boolean doesItemStageAllowModification(Long itemId) throws BusinessValidationException, DataAccessException {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		Map<Long,List<Request>> requestMap = itemRequest.getRequests(itemIds, requestStages, null);
		List<Request> requests = null;
		if (null != requestMap) {
			requests = requestMap.get(itemId);
		}
		return (!(null != requests && requests.size() > 0));
	}
	
	private String getLatestStage(Long itemId) throws DataAccessException{
		String historyLkpValue = null;
		LksData historyLksData = itemRequest.getLatestRequestStage(itemId);
		if (null != historyLksData && 
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_COMPLETE &&
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_ARCHIVED &&
				historyLksData.getLkpValueCode() != SystemLookup.RequestStage.REQUEST_ABANDONED) {
			historyLkpValue = historyLksData.getLkpValue();
		}
		
		return historyLkpValue;
	}

}
