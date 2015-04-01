/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author prasanna
 *
 */
public class DeleteItemCommonValidator implements Validator {
	
	private List<Validator> validators; // refer: validators.xml 
 
	public List<Validator> getValidators() {
		return validators;
	}

	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target instanceof Map) {
			for (Validator v: validators) {
				v.validate(target, errors);
			}
		} else {
			throw new IllegalArgumentException("You must provide a Map of item and userInfo for Item delete validation");
		}
	}
}