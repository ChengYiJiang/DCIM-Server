package com.raritan.tdz.item.validators;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;


public class IllegalItemNameValidator implements Validator{
	
	private static final String regex = "[\\&\\?\'\r\n\\\\]";

	private Boolean isInValid(String name){
		Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(name);
	    return matcher.find();
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if (target instanceof Map) {
			Map<String,Object> targetMap = (Map<String,Object>)target;
			
			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) throw new IllegalArgumentException();
			String name = item.getItemName();
			if ( name == null) {
				errors.rejectValue("itemName", "ItemValidator.ItemNameIsNull");
				return;
			}
			Object[] errorArgs = new Object[]{ name, "\\ & ? '" };
			Boolean rval = isInValid(name);
			if (rval == true ) {
				errors.rejectValue("itemName", "ItemValidator.invalidItemName", errorArgs, "Invalid Item name");
			}
		}
	}
}
