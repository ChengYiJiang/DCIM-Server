/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * @author Santo Rosario
 *
 */
public class DataConnCompatibilityValidatorFactoryImpl implements
		DataConnCompatibilityValidatorFactory {
	
	@Autowired
	DataPortDAO dataPortDAO;
	
	Map<Long, DataConnCompatibilityValidator> validatorMap;
	
	public DataConnCompatibilityValidatorFactoryImpl(Map<Long, DataConnCompatibilityValidator> validatorMap){
		this.validatorMap = validatorMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.validators.DataConnCompatibilityValidatorFactory#getCompatibilityValidator(com.raritan.tdz.domain.DataConnection)
	 */
	@Override
	public DataConnCompatibilityValidator getCompatibilityValidator(
			DataConnection dataConn) {
		
		DataConnCompatibilityValidator validator = null;
		
		//validate arguments
		if (dataConn == null || dataConn.getDestDataPort() == null){
			return null;
		}
				
		return getCompatibilityValidator(dataConn.getSourceDataPort());
		
	}

	@Override
	public DataConnCompatibilityValidator getCompatibilityValidator(DataPort port) {
		
		DataConnCompatibilityValidator validator = null;
		
		if (port == null)
			return null;
		
		if (port.getPortSubClassLookup() == null && port.getPortId() != null)
			port = dataPortDAO.read(port.getPortId());
		
		if (port.getPortSubClassLookup() != null){
			validator = validatorMap.get(port.getPortSubClassLookup().getLkpValueCode());
		}
		
		return validator;
	}

}
