package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;

public class PanelItemArchiveValidator implements Validator {
	
	@Autowired
	PowerPanelItemConnectionValidator ppItemConnectionValidator;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Item.class.equals(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object target, Errors errors) {
		Map<String,Object> targetMap = (Map<String,Object>)target;
		Item item = (Item)targetMap.get(MeItem.class.getName());
		
		if (item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.ARCHIVED) {
			ppItemConnectionValidator.validate(targetMap, errors);
		}
	}
	
}
