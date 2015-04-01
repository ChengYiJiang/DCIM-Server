/**
 * 
 */
package com.raritan.tdz.circuit.validators;



import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.PowerConnection;

/**
 * @author prasanna
 *
 */
public class PowerConnCompatibilityValidator implements Validator {

	private List<Validator> connValidatorList;
	
	public PowerConnCompatibilityValidator(List<Validator> connValidatorList){
		this.connValidatorList = connValidatorList;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(PowerConnection.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//Validate power connection	
		for (Validator validator:connValidatorList){
			validator.validate(target, errors);
		}	
	}
}
