package com.raritan.tdz.request.validator;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Request;

public class ValidateRequestUnderProcess implements Validator {

	
	private void validate(Request request, Errors errors) {
		
		if (null == request) return;
		
		Long requestId  = request.getRequestId();
		
		if (RequestUnderProgress.isRequestUnderProgress(requestId)) {
			
			String requestNumber = request.getRequestNo();
			
			Object[] errorArgs = { requestNumber };
			errors.reject("Request.UnderProcess", errorArgs, "Request" + requestNumber + " is already been processed by another session.");
			
		}
			
		RequestUnderProgress.setRequest(requestId);
			

	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		
		@SuppressWarnings("unchecked")
		List<Request> requests = (List<Request>) targetMap.get(List.class.getName());

		for (Request request: requests) {
			
			validate(request, errors);
			
		}
	}

}
