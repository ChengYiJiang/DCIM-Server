/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class RPDUPowerConnPortValidator implements Validator {
	
	@Autowired
	PowerPortDAO powerPortDAO;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(PowerPort.class);
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
		validateAmps(powerPortDB,errors);
		
		//Check Input Port
        validateInputCord(powerPortDB, errors);
	}

	//--------------------- Private methods -----------------------------
	private void validateInputCord(PowerPort powerPortDB, Errors errors) {
		if(!powerPortDB.isInputCord() && powerPortDB.getInputCordPort() == null){
        	errors.reject("powerProc.missingInputCord");
        }
	}

	private void validateAmps(PowerPort powerPortDB, Errors errors) {
		if(powerPortDB.getFuseLookup() != null){ //if there is a Fuse
			if(powerPortDB.getAmpsBudget() <= 0){
				errors.reject("powerProc.missingAmpsRatingFuse");
			}
		}
		else if(powerPortDB.getAmpsNameplate() <= 0){
			errors.reject("powerProc.misingAmpsRating");
		}
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a power port target");
		if (!(target instanceof PowerPort)) throw new IllegalArgumentException("You must provide a power port target");
	}

}
