/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * This factory produces EnoughPowerValidator list based on destination port's Item uniqueValue.
 * @author prasanna
 *
 */
public class EnoughPowerValidatorFactoryImpl implements
		EnoughPowerValidatorFactory {
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	Map<Long, List<EnoughPowerValidator>> enoughPowerValidatorMap;
	
	public EnoughPowerValidatorFactoryImpl(Map<Long, List<EnoughPowerValidator>> enoughPowerValidatorMap) {
		this.enoughPowerValidatorMap = enoughPowerValidatorMap;
	}

	public Map<Long, List<EnoughPowerValidator>> getEnoughPowerValidatorMap() {
		return enoughPowerValidatorMap;
	}

	public void setEnoughPowerValidatorMap(
			Map<Long, List<EnoughPowerValidator>> enoughPowerValidatorMap) {
		this.enoughPowerValidatorMap = enoughPowerValidatorMap;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidatorFactory#getValidators(com.raritan.tdz.domain.PowerConnection)
	 */
	@Override
	public List<EnoughPowerValidator> getValidators(PowerConnection powerConn) {
		if (powerConn == null){
			return new ArrayList<EnoughPowerValidator>();
		}
		
		PowerPort destPort = powerConn.getDestPowerPort();
		if (destPort != null && destPort.getPortId() != null)
			destPort = powerPortDAO.read(destPort.getPortId());
		
		return getValidators(destPort);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.EnoughPowerValidatorFactory#getValidators(com.raritan.tdz.domain.PowerPort)
	 */
	@Override
	public List<EnoughPowerValidator> getValidators(PowerPort destPort) {
		List<EnoughPowerValidator> validators = new ArrayList<EnoughPowerValidator>();
		
		if (destPort != null){
			Long portSubClass = destPort.getPortSubClassLookup() != null ? destPort.getPortSubClassLookup().getLkpValueCode() : 0L;
			
			if (enoughPowerValidatorMap.get(portSubClass) != null){
				validators = enoughPowerValidatorMap.get(portSubClass);
			}
		}
		
		return validators;
	}

}
