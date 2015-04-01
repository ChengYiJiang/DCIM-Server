package com.raritan.tdz.port.validators;

import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.validators.EnoughPowerValidatorFactory;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;

public class PowerPortLoadValidator implements Validator {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired(required=true)
	private EnoughPowerValidatorFactory enoughPowerValidatorFactory;

	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Transactional(readOnly=true,propagation=Propagation.REQUIRES_NEW)
	@Override
	public void validate(Object target, Errors errors) {

		validateArgs(target);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		PowerPort powerPort = (PowerPort)targetMap.get(PowerPort.class.getName());
		
		validateEnoughPowerLoaded(powerPort, errors);
		
	}
	
	
	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		PowerPort powerPort = (PowerPort)targetMap.get(PowerPort.class.getName());
		
		if (powerPort == null) throw new IllegalArgumentException("You must provide a power port target");
		
	}

	private void validateEnoughPowerLoaded(PowerPort port, Errors errors) {
		List<EnoughPowerValidator> validators = enoughPowerValidatorFactory.getValidators(port);
		
		for (EnoughPowerValidator validator: validators) {
			// checkEnoughPower(double  ampsBeingConnected, long wattsBeingConnected, double powerFactor, Long srcNodeId, Long destNodeId, Long psPortIdToExclude, String psPortItemName, int positionInCircuit, Object firstNodeSum, Errors errors)
			validator.checkEnoughPower(0.0, 0L, 1.0, null, port.getPortId(), null, null, -1, null, errors, null);
		}
	}
	

}
