/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerSupplyPowerConnPortValidator implements Validator {
	
	@Autowired
	PowerPortDAO powerPortDAO;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return PowerPort.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		PowerPort powerPort = (PowerPort)target;
		
		
		PowerPort powerPortDB = powerPortDAO.read(powerPort.getPortId());
		
		//Check Watts
		if(powerPortDB.getWattsBudget() <= 0){ //For Device and Network items, check for Budget Watts
			errors.reject("powerProc.missingBudgetWatts");
		}
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a power port target");
	}

}
