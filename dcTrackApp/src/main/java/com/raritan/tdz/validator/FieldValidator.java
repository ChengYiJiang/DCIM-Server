package com.raritan.tdz.validator;

import org.springframework.validation.MapBindingResult;



public interface FieldValidator {
	/**
	 *  Validate uiId and value against schema
	 *  
	 * @param uiId
	 * @param remoteReference
	 * @param remoteType
	 * @param value
	 * 
	 * @param validationErrors - error are collected here
	 * @throws ClassNotFoundException 
	 */
	public void validate(String uiId, String remoteReference, String remoteType, Object value, MapBindingResult validationErrors) throws ClassNotFoundException;
}
