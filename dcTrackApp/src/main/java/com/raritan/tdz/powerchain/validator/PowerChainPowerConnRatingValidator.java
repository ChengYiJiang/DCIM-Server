package com.raritan.tdz.powerchain.validator;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class PowerChainPowerConnRatingValidator implements Validator {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired
	protected PowerPortDAO powerPortDAO;

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
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		// Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		validateSrcDestPort(powerConn, errors);
		
		if (powerConn.getSourcePowerPort() != null 
				&& powerConn.getDestPowerPort() != null){
			validatePowerConnRatings(powerConn,errors);
		}
	}
	
	private void validatePowerConnRatings(PowerConnection powerConn,
			Errors errors) {

		if (null == powerConn.getSourcePowerPort()) {
			return;
		}
		if (null == powerConn.getDestPowerPort()) {
			return;
		}
		
		// Get the ports from the connection
		PowerPort srcPort = powerConn.getSourcePowerPort(); // powerPortDAO.read(powerConn.getSourcePowerPort().getPortId());
		PowerPort dstPort = powerConn.getDestPowerPort(); // powerPortDAO.read(powerConn.getDestPowerPort().getPortId());
		
		//check if amps rating of the item to the left is not greater than the item to the right
		double srcAmps=0, dstAmps=0, srcWatts=0, dstWatts=0;
		
		srcAmps = getSrcAmps(srcPort);
		
		dstAmps = getDstAmps(dstPort);
		
		srcWatts = computeWatts(srcPort, srcAmps);
		
		dstWatts = computeWatts(dstPort, dstAmps);
		
		if (srcAmps > dstAmps){
			Object[] errorArgs = new Object[] {
					srcPort.getDisplayName(),
					srcAmps,
					srcWatts,
					dstPort.getDisplayName(),
					dstAmps,
					dstWatts
					};
			errors.reject("powerProc.AmpsRatingExceedsPrev", errorArgs, "Source Amps Rating Exceeds Destination Amps rating");
		}
	}

	private double getSrcAmps(PowerPort srcPort){
		return srcPort.isPowerSupply() ? srcPort.getAmpsBudget() : srcPort.getAmpsNameplate();
	}
	
	private double getDstAmps(PowerPort dstPort){
		return dstPort.isPowerSupply() ? 0.0 : dstPort.getAmpsNameplate();
	}
	
	
	
	private double computeWatts(PowerPort pPort, double thisAmps) {
		if (pPort.isPowerSupply())
			return pPort.getWattsBudget();
		else
			return thisAmps * getVolts(pPort)
					* pPort.getPowerFactor() * phaseFactor(pPort.getPhaseLookup());
	}

	private double phaseFactor(LksData phaseLookup) {
		double factor = 0;

		if (phaseLookup != null){
			Long lookupValueCode = phaseLookup.getLkpValueCode();
			if (lookupValueCode == SystemLookup.PhaseIdClass.SINGLE_2WIRE
					|| lookupValueCode == SystemLookup.PhaseIdClass.SINGLE_3WIRE){
				factor = 1.0;
			} else if (lookupValueCode == SystemLookup.PhaseIdClass.THREE_DELTA
					|| lookupValueCode == SystemLookup.PhaseIdClass.THREE_WYE){
				//Square Root of 3
				factor = Math.sqrt(3);
			}
		}
		return factor;
	}
	
	private double getVolts(PowerPort port) {
		double volts = 0;
		try {
			if (port.getVoltsLookup() != null){
				volts = Double.parseDouble(port.getVoltsLookup().getLkpValue());
			}
		} catch (NumberFormatException e){
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		return volts;
	}
	
	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
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
