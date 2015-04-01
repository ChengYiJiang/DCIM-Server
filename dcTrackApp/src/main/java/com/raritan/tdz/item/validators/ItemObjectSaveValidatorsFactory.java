package com.raritan.tdz.item.validators;

import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

public interface ItemObjectSaveValidatorsFactory {
	
	public Validator getValidators(Item item);

}
