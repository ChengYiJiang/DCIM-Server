package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.move.home.ItemMoveHelper;
import com.raritan.tdz.util.BusinessExceptionHelper;

public class ValidatePlacementInMovingChassis implements Validator {

	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
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
		
		Errors placementErrors = itemMoveHelper.getPlacementInMoveChassisError(item, errors, false);
		
		if (!placementErrors.hasErrors()) return;
		
		String errorMsg = businessExceptionHelper.getMessage(placementErrors);
		if (null == errorMsg || errorMsg.length() == 0) return;
		
		errorMsg.concat("\n\nDo you want to continue?");
		
		Object[] errorArgs = { errorMsg.toString() };
		errors.rejectValue("itemMove", "ItemMoveValidator.placeItemInMovingChassis", errorArgs, errorMsg.toString());
		
		// errors.addAllErrors(placementErrors);
		
	}

}
