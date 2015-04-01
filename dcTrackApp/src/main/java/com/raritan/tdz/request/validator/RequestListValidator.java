package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class RequestListValidator implements Validator {

	private List<Validator> validators;

	
	public RequestListValidator(List<Validator> validators) {
		super();
		this.validators = validators;
	}

	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		/*Map<String, Object> targetMap = (Map<String, Object>) target;
		
		List<Request> requests = (List<Request>) targetMap.get(List.class.getName());*/
		
		for (Validator validator: validators) {
			
			validator.validate(target, errors);
			
		}

	}

}
