/**
 * 
 */
package com.raritan.tdz.circuit.validators;



import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataConnection;

/**
 * @author Santo Rosario
 *
 */
public class DataConnCompatibilityValidator implements Validator {

	private List<Validator> connValidatorList;
	
	public DataConnCompatibilityValidator(List<Validator> connValidatorList){
		this.connValidatorList = connValidatorList;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(DataConnection.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//Validate data connection	
		for (Validator validator:connValidatorList){
			validator.validate(target, errors);
		}	
	}
}
