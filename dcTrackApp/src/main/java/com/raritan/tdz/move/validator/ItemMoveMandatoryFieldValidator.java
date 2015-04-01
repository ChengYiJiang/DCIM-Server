package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.itemState.MandatoryFieldStateValidator;

public class ItemMoveMandatoryFieldValidator implements Validator {

	private Map<String, MandatoryFieldStateValidator> mandatoryFieldValidators;
	
	public ItemMoveMandatoryFieldValidator(
			Map<String, MandatoryFieldStateValidator> mandatoryFieldValidators) {

		this.mandatoryFieldValidators = mandatoryFieldValidators;
	}

	@Override
	public boolean supports(Class<?> clazz) {

		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {

		if (null == mandatoryFieldValidators || mandatoryFieldValidators.size() == 0) return;
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (null == item.getItemToMoveId()) return;
		
		String mounting = (null != item.getModel()) ? item.getModel().getMounting() : "";
		
		MandatoryFieldStateValidator mandatoryFieldValidator = mandatoryFieldValidators.get(mounting);

		if (null == mandatoryFieldValidator) {
			
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("tiClass", "ItemMoveValidator.CannotValidateMandatoryFields", errorArgs, "Cannot validate the mandatory fields of when moved item.");
			return;
		}

		try {
			
			mandatoryFieldValidator.validateMandatoryFields(item,  item.getStatusLookup().getLkpValueCode(), errors, "movingItem", null);
			
		} catch (DataAccessException | ClassNotFoundException e) {
			
			Object[] errorArgs = { item.getItemName() };
			errors.rejectValue("tiClass", "ItemMoveValidator.CannotValidateMandatoryFields", errorArgs, "Cannot validate the mandatory fields of when moved item.");
			
		}
		
	}


}
