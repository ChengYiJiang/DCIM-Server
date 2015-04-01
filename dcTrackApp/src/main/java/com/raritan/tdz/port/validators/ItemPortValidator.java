/**
 * 
 */
package com.raritan.tdz.port.validators;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;

/**
 * @author prasanna
 *  <p>This expects a map that contains the following<p>
 *  <ul>
 *  <li>"itemId", Long value</li>
 *  <li>"portId", Long value</li>
 *  <li>"UserInfo", UserInfo</li>
 *  </ul>
 */
public class ItemPortValidator<T> implements Validator {
	
	private List<Validator> validators;
	

	public ItemPortValidator(List<Validator> validators ){
		this.validators = validators;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(IPortInfo.class);
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {	
		for( Validator validator : validators ){
			validator.validate(target, errors);
			if (errors.hasErrors()) return;
		}
	}


}
