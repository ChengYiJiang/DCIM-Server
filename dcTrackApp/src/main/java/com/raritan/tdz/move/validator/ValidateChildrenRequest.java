package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.move.home.ItemMoveHelper;
import com.raritan.tdz.util.BusinessExceptionHelper;

public class ValidateChildrenRequest implements Validator {

	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
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
		
		if (item.getItemId() > 0) return;
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return;
		
		if (null == item.getItemToMoveId()) return;
		
		Errors childReqErrors = itemMoveHelper.getChildrenRequestErrors(item.getItemToMoveId(), errors);
		
		String errorMsg = businessExceptionHelper.getMessage(childReqErrors);

		if (null == errorMsg || errorMsg.length() == 0) return;
		
		Object[] errorArgs = { errorMsg };
		errors.rejectValue("itemMove", "ItemMoveValidator.childrenHasPendingRequest", errorArgs, errorMsg);
		
	}
	
}
