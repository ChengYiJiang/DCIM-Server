/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerConnCompatibilityValidatorFactoryImpl implements
		PowerConnCompatibilityValidatorFactory {
	
	@Autowired
	PowerPortDAO powerPortDAO;
	
	Map<Long, PowerConnCompatibilityValidator> validatorMap;
	
	public PowerConnCompatibilityValidatorFactoryImpl(Map<Long, PowerConnCompatibilityValidator> validatorMap){
		this.validatorMap = validatorMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.PowerConnCompatibilityValidatorFactory#getCompatibilityValidator(com.raritan.tdz.domain.PowerConnection)
	 */
	@Override
	public PowerConnCompatibilityValidator getCompatibilityValidator(
			PowerConnection powerConn) {
		
		PowerConnCompatibilityValidator validator = null;
		
		//validate arguments
		if (powerConn == null || powerConn.getDestPowerPort() == null){
			return null;
		}
		
		PowerPort dstPort = powerConn.getDestPowerPort();
		
		return getCompatibilityValidator(dstPort);
		
	}

	@Override
	public PowerConnCompatibilityValidator getCompatibilityValidator(
			PowerPort destPowerPort) {
		
		PowerConnCompatibilityValidator validator = null;
		
		if (destPowerPort == null)
			return null;
		
		if (destPowerPort.getPortSubClassLookup() == null && destPowerPort.getPortId() != null)
			destPowerPort = powerPortDAO.read(destPowerPort.getPortId());
		
		if (destPowerPort.getPortSubClassLookup() != null){
			validator = validatorMap.get(destPowerPort.getPortSubClassLookup().getLkpValueCode());
		}
		
		return validator;
	}

}
