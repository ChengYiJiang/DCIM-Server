package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;

public class FPDUItemArchiveValidator implements Validator {
	
	@Autowired
	FPDUItemConnectionValidator fpduItemConnectionValidator;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return MeItem.class.equals(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object target, Errors errors) {
		
		if (target instanceof Map) {
			Map<String,Object> targetMap = (Map<String,Object>)target;
			Item item = (Item)targetMap.get(MeItem.class.getName());
			if (item == null) throw new IllegalArgumentException();
		
			if (item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.ARCHIVED) {
				fpduItemConnectionValidator.validate(targetMap, errors);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
}
