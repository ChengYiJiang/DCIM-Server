package com.raritan.tdz.powerchain.validator;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.PowerConnection;

public class PowerConnectionValidator implements Validator {
	
	public static final String OLD_CIRCUIT_TRACE = "OldCircuitTrace";
	public static final String NEW_CIRCUIT_TRACE = "NewCircuitTrace";

	private List<Validator> validators;
	
	public List<Validator> getValidators() {
		return validators;
	}

	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {

		return PowerConnection.class.equals(clazz);
		
	}

	@Override
	public void validate(Object target, Errors errors) {

//		PowerConnection powerConn = (PowerConnection) target;
//		Map<String, Object> targetMap = new HashMap<String, Object>();
//		
//		targetMap.put(PowerConnection.class.getName(), powerConn);
//		targetMap.put(Integer.class.getName(), 0);
		
		if (validators != null){
			for (Validator validator:validators){
				//validator.validate(targetMap, errors);
				validator.validate(target, errors);
			}
		}

	}

}
