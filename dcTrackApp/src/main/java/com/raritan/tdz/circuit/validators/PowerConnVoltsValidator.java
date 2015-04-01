/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerConnVoltsValidator implements Validator {
	
	@Autowired
	private PowerPortDAO powerPortDAO;

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
		
		if (powerConn.getSourcePowerPort() != null 
				&& powerConn.getDestPowerPort() != null){
			validateVoltsCompatibility(powerConn,errors);
		}
	}
	
	private void validateVoltsCompatibility(PowerConnection powerConn,
			Errors errors) {
		PowerPort srcPort = powerPortDAO.read(powerConn.getSourcePowerPort().getPortId());
		PowerPort dstPort = powerPortDAO.read(powerConn.getDestPowerPort().getPortId());
		
		if (srcPort != null && dstPort != null 
				&& srcPort.getVoltsLookup() != null && dstPort.getVoltsLookup() != null
				&& !checkVoltsCompatibility(srcPort.getVoltsLookup(), dstPort.getVoltsLookup())){

			String sItemClass = srcPort.getItem() != null && srcPort.getItem().getClassLookup() != null 
					? srcPort.getItem().getClassLookup().getLkpValue(): "<Unknown>"	;

			String dItemClass = dstPort.getItem() != null && dstPort.getItem().getClassLookup() != null 
					? dstPort.getItem().getClassLookup().getLkpValue(): "<Unknown>"	;
			
			Object[] errorArgs = new Object[] {
					sItemClass,
					srcPort.getVoltsLookup().getLkpValue(),
					dItemClass,
					dstPort.getVoltsLookup().getLkpValue()
					};
			
			errors.reject("powerProc.voltageMismatch",errorArgs,"Connection Voltage mismatch");
		}
	}

	private boolean checkVoltsCompatibility(LksData vLeft, LksData vRight) {
		boolean retCode = false;
		
		if(vLeft.getLksId().equals(vRight.getLksId())){
			retCode = true;
		}
		else{
			String lv = vLeft.getLkpValue();
			Long rv = Long.valueOf(vRight.getLkpValue());
			
			if (lv.equals("120~240") && (rv >= 100 && rv <= 250)){
				retCode = true;
			}
			else if (lv.equals("120") && (rv >= 100 && rv <= 150)){
				retCode = true;
			}
			else if (lv.equals("240") && (rv <= 250 && rv >= 150)){
				retCode = true;
			}
			else if (lv.equals("230") && (rv >= 200 && rv <= 250)){
				retCode = true;
			}
			else if (lv.equals("220") && (rv >= 200 && rv <= 250)){
				retCode = true;
			}
			
		}
		
		return retCode;
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

}
