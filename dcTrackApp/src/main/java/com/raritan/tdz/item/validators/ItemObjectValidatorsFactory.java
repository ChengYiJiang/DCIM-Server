package com.raritan.tdz.item.validators;


import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

public interface ItemObjectValidatorsFactory {
	
	public Validator getArchiveValidators(Item item);
	
	public Validator getDeleteValidators(Item item);
	
	public Validator getStorageValidators(Item item);

}
