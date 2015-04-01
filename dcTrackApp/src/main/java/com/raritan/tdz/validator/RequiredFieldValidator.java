/**
 * 
 */
package com.raritan.tdz.validator;

import org.springframework.validation.Errors;

import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 * Purpose of this interface is to validate required fields for a specific
 * screen on the webclient conforming to the Rules Processor definition.
 */
public interface RequiredFieldValidator {
	/**
	 * Validates the required fields that are defined in the rules processor
	 * for a specific screen.
	 * @param target - Target to validate against
	 * @param errors - Errors object to fill with errors if any
	 * @param errorCode - Error code to be used.
	 * @throws DataAccessException TODO
	 * @throws ClassNotFoundException TODO
	 */
	public void validateRequiredField(Object target, Errors errors, String errorCode) throws DataAccessException, ClassNotFoundException;
}
