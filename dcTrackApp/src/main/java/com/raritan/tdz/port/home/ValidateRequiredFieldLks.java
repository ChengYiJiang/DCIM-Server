package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

public interface ValidateRequiredFieldLks {

	/**
	 * Validate if the lks field is valid
	 * @param target
	 * @param errors
	 * @param lksuField
	 * @param errorCode
	 * @param userField
	 */
	public void validate(Object target, Errors errors, String lksField, String errorCode, String userField);
	
}
