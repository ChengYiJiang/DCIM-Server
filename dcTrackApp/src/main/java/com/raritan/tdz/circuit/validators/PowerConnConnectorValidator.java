/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.home.CircuitProc;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerConnConnectorValidator implements Validator {
	
	@Autowired
	PowerConnDAO powerConnDAO;
	
	@Autowired
	PowerPortDAO powerPortDAO;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return PowerConnection.class.equals(clazz);
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
		
		if (powerConn.isLinkTypeExplicit() && powerConn.getSourcePowerPort() != null 
				&& powerConn.getDestPowerPort() != null){
			PowerPort srcPort = powerPortDAO.read(powerConn.getSourcePowerPort().getPortId()); 
			PowerPort dstPort = powerPortDAO.read(powerConn.getDestPowerPort().getPortId());
			validateConnectors(srcPort, dstPort, errors);
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
	
	private void validateSrcDestPort(PowerConnection powerConn, Errors errors) {
		if (powerConn.getSourcePort() == null){
			errors.reject("powerProc.missingSourcePort");
		}
		
		if (powerConn.getDestPowerPort() == null){
			errors.reject("powerProc.missingDestPort");
		}
	}
	
	private void validateConnectors(PowerPort sPort, PowerPort dPort, Errors errors) {

		if (sPort != null && dPort != null){
			if (dPort.isBreaker() == false){ 
				//checks connectors on both ends. if not the same and are not compatible per tlkpConnectorCompat then disallow
				if(sPort.getConnectorLookup() != null && dPort.getConnectorLookup() != null
						&& powerConnDAO.areConnectorsCompatible(sPort.getConnectorLookup(), dPort.getConnectorLookup()) == false){
					
					String sPortItemName = sPort.getItem() != null ? sPort.getItem().getItemName() : "<Unknown>";
					String dPortItemName = dPort.getItem() != null ? dPort.getItem().getItemName() : "<Unknown>";
					
					Object[] errorArgs = new Object[] {
							sPortItemName,
							sPort.getPortName(),
							dPortItemName,
							dPort.getPortName()
							};
					
					errors.reject("powerProc.incompatibleConnector", errorArgs,"Connectors are incompatibility");
				}
			}
		}
	}

}
