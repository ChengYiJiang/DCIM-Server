package com.raritan.tdz.request.validator;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */

public class RequestValidatorImpl implements Validator, RequestValidator {

	private List<Validator> validators;
	
	private List<RequestValidator> requestValidators;
	
	public List<Validator> getValidators() {
		return validators;
	}

	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}

	public List<RequestValidator> getRequestValidators() {
		return requestValidators;
	}

	public void setRequestValidators(List<RequestValidator> requestValidators) {
		this.requestValidators = requestValidators;
	}

	@Override
	public boolean supports(Class<?> clazz) {

		boolean supported =  Request.class.equals(clazz);

		return supported;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		if (null == targetMap || null == validators) return;
		
		for (Validator validator:validators){
			
			validator.validate(targetMap, errors);
		}

	}

	@Override
	public void validate(RequestMessage requestMessage) throws DataAccessException, ClassNotFoundException {
		
		if (null == requestValidators || requestValidators.size() == 0) return;
		
		for (RequestValidator requestValidator: requestValidators) {
			
			requestValidator.validate(requestMessage);
		}
		
	}

}
