package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

public interface ValidateFieldLength {

	/**
	 * Validate the length of the field and add to the errors if the length is not valid
	 * @param target
	 * @param errors
	 * @param field
	 * @param fieldLength
	 * @param errorCode
	 */
	public void validate(Object target, Errors errors, String field, Long fieldLength, String userField, String errorCode);
}
