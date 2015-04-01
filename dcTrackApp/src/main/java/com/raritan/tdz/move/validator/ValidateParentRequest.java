package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.home.ItemMoveHelper;
import com.raritan.tdz.request.home.RequestLookup;
import com.raritan.tdz.util.BusinessExceptionHelper;

public class ValidateParentRequest implements Validator {

	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return;

		Boolean requestIssueValidate = (Boolean) targetMap.get(RequestLookup.RquestIssueValidate);
		
		if ( ! ( (item.getItemId() > 0 && (itemMoveHelper.isCabinetChanged(item) || itemMoveHelper.isChassisChanged(item))) || 
				(item.getItemId() <= 0 )|| 
				(null != requestIssueValidate && requestIssueValidate.equals(true)) ) ) return;
		
		// item is moving out of moving cabinet
		Long movingItemId = item.getItemToMoveId();
		Errors movingItemParentReqErrors = null;
		if (item.getItemId() <= 0 && null != movingItemId && movingItemId > 0) {
			Item movingItem = itemDAO.loadItem(movingItemId);
			movingItemParentReqErrors = itemMoveHelper.getParentRequestErrors(movingItem, errors);
		}
		
		// item is moving to moving cabinet
		Errors parentReqErrors = itemMoveHelper.getParentRequestErrors(item, errors);
		if (null != movingItemParentReqErrors && movingItemParentReqErrors.hasErrors()) {
			parentReqErrors.addAllErrors(movingItemParentReqErrors);
		}
		
		String errorMsg = businessExceptionHelper.getMessage(parentReqErrors);

		if (null == errorMsg || errorMsg.length() == 0) return;
		
		if (null == item.getSubclassLookup() ||  
				!(item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS) || item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS)) ) {
			errorMsg += "\nDo you want to continue?";
		}
		else {
			errorMsg += "\n";
		}
		Object[] errorArgs = { errorMsg };
		errors.rejectValue("itemMove", "ItemMoveValidator.parentHasPendingRequest", errorArgs, errorMsg);

	}
	
}
