package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

public interface ValidateRequiredFieldLku {

	/**
	 * validate if the lku field is not null and the error do not contain the error
	 * @param target
	 * @param lkuField
	 * @param errors
	 */
	public void validate(Object target, Errors errors, String lkuField, String errorCode, String userField);
	
}
