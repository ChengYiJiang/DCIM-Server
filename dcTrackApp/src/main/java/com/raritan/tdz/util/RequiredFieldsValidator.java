package com.raritan.tdz.util;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class RequiredFieldsValidator implements Validator{
	private final Logger log = Logger.getLogger(this.getClass());
	
	Map<String, String> requiredFields;
	
	public Map<String, String> getRequiredFields() {
		return requiredFields;
	}

	public void setRequiredFields(Map<String, String> requiredFields) {
		this.requiredFields = requiredFields;
	}

	/**
	 * 
	 * @param target - object on which you want to check if required fields are present
	 * @param requiredFields - map:
	 * 				key - internal field name
	 * 				value - field name as it should be shown to the client
	 * @param errors
	 */
	public void validate(Object target, Errors errors) {
		if( requiredFields == null ) return;

		for( String rf : requiredFields.keySet()  ){
			Class<?> clazz = target.getClass();
			Field f = org.springframework.util.ReflectionUtils.findField(clazz, rf);
			if( f != null ){
				org.springframework.util.ReflectionUtils.makeAccessible(f);
				try {
					if( f.get(target) == null ){
						log.error("field: " + requiredFields.get(rf) + " is required");
						
						StringBuffer errorCode = new StringBuffer();
						errorCode.append("RequiredFieldsValidator.requiredParam.");
						errorCode.append(requiredFields.get(rf));
						
						StringBuffer msg = new StringBuffer();
						msg.append(requiredFields.get(rf));
						msg.append(" is missing. Please provide it.");
						errors.reject(errorCode.toString(), null, msg.toString());
					}
				}catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalArgumentException("You must provide " + f.getName());
				}
			}
		}		
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}
}
