/**
 * 
 */
package com.raritan.tdz.beanvalidation.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 *
 */
public interface BeanValidationHome {
	
	/**
	 * Validates the given bean
	 * <p> The target will be the actual object that has the values that needs to be validated<p>
	 * @param target
	 * @return TODO
	 * @throws BusinessValidationException TODO
	 */
	public Errors validate(Object target);

}
