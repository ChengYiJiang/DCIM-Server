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
public class FloorOutletPowerConnPortValidator implements Validator {
	
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
		
		//Check Amps
		if(powerPortDB.getAmpsNameplate() <= 0){
			errors.reject("powerProc.misingAmpsRating");
		}
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a power port target");
	}

}
