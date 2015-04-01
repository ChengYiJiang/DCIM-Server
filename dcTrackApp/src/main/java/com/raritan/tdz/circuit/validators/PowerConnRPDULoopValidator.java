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
public class PowerConnRPDULoopValidator implements Validator {
	
	@Autowired
	PowerPortDAO powerPortDAO;

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
		if (powerConn.getSourcePowerPort() != null 
				&& powerConn.getDestPowerPort() != null){
			PowerPort srcPortClient = powerConn.getSourcePowerPort();
			PowerPort dstPortClient = powerConn.getDestPowerPort();
			PowerPort srcPort = powerPortDAO.read(powerConn.getSourcePowerPort().getPortId());
			PowerPort dstPort = powerPortDAO.read(powerConn.getDestPowerPort().getPortId());
			
			if(srcPort.getItem() != null && dstPort.getItem() != null
					&& srcPort.getItem().getItemId() == dstPort.getItem().getItemId()){
				if(srcPort.getPortSubClassLookup().getLkpValueCode() == com.raritan.tdz.lookup.SystemLookup.PortSubClass.INPUT_CORD &&
						dstPort.getPortSubClassLookup().getLkpValueCode() == com.raritan.tdz.lookup.SystemLookup.PortSubClass.RACK_PDU_OUTPUT){
					errors.reject("powerProc.RackPDUConnectionLoop");
				}
			}
			
		}

	}
	
	private void validateSrcDestPort(PowerConnection powerConn, Errors errors) {
		if (powerConn.getSourcePort() == null){
			errors.reject("powerProc.missingSourcePort");
		}
		
		if (powerConn.getDestPowerPort() == null){
			errors.reject("powerProc.missingDestPort");
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
