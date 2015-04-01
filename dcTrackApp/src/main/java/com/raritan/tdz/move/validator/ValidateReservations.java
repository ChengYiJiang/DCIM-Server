package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.move.home.ItemMoveHelper;

/**
 * checks if any reservations are been made in the item and warns user
 * @author bunty
 *
 */
public class ValidateReservations implements Validator {

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
		
		if (item.getItemId() > 0) return;
		
		Long movingItemId = item.getItemToMoveId();
		
		if (null == movingItemId || movingItemId <= 0) return;
		
		Errors childReservationErrors = itemMoveHelper.getChildrenReservationErrors(movingItemId, errors);
		
		errors.addAllErrors(childReservationErrors);
		
	}

}
