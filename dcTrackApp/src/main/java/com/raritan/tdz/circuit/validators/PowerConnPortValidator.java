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
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerConnPortValidator implements Validator {
	
	@Autowired
	PowerPortDAO powerPortDAO;
	
	Map<Long, Validator> itemSpecificPortValidator;
	
	public PowerConnPortValidator(Map<Long, Validator> itemSpecificPortValidator) {
		this.itemSpecificPortValidator = itemSpecificPortValidator;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(PowerConnection.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		validateSrcDestPort(powerConn, errors);
		
		PowerPort srcPortClient = powerConn.getSourcePowerPort();
		
		if (srcPortClient != null){
			PowerPort srcPort = powerPortDAO.read(srcPortClient.getPortId());
			
			//validateUsedPort(srcPortClient, srcPort, errors);
			
			validateConnectorLookup(srcPort, errors);
			
			validatePhaseLookup(srcPort,errors);
			
			validateVoltsLookup(srcPort,errors);
			
			Long srcPortSubClassValueCode = srcPort != null && srcPort.getPortSubClassLookup() != null ?
					srcPort.getPortSubClassLookup().getLkpValueCode() : null;
			if (srcPortSubClassValueCode != null && itemSpecificPortValidator != null && itemSpecificPortValidator.get(srcPortSubClassValueCode) != null){
				itemSpecificPortValidator.get(srcPortSubClassValueCode).validate(srcPort, errors);
			}
		}
	}





	//---------------- Private methods -----------------------------
	private void validateVoltsLookup(PowerPort srcPort, Errors errors) {
		if (srcPort.getVoltsLookup() == null){
			Object errorArgs[] = new Object[] { srcPort.getPortName() };
			errors.reject("powerProc.missingVolts", errorArgs,"Missing Volts lookup");
		}
	}

	private void validatePhaseLookup(PowerPort srcPort, Errors errors) {
		if (srcPort.getPhaseLookup() == null){
			Object errorArgs[] = new Object[] { srcPort.getPortName() };
			errors.reject("powerProc.missingPhase", errorArgs,"Missing Phase lookup");
		}
	}

	
	private void validateConnectorLookup(PowerPort srcPort, Errors errors) {
		if (srcPort.getConnectorLookup() == null){
			Object errorArgs[] = new Object[] { srcPort.getPortName() };
			errors.reject("powerProc.missingConnector", errorArgs,"Missing connector lookup");
		}
	}

	/*
	private void validateUsedPort(PowerPort srcPortClient, PowerPort srcPort,
			Errors errors) {
		if (srcPortClient.getUsed() == false && srcPort.getUsed() == true){
			Object errorArgs[] = new Object[] { srcPort.getPortName() };
			errors.reject("powerProc.srcPortUsed", errorArgs, "Port in use");
		}
	}
*/
	
	private void validateSrcDestPort(PowerConnection powerConn, Errors errors) {
		if (powerConn.getSourcePort() == null){
			errors.reject("powerProc.missingSourcePort");
		}
	}

	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		
		if (powerConn == null) throw new IllegalArgumentException("You must provide a power connection target");
		
		Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		if (nodeNumber == null) throw new IllegalArgumentException("You must provide a node number for this target");
	}

}
