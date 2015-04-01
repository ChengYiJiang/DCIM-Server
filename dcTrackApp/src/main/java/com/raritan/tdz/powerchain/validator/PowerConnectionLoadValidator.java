package com.raritan.tdz.powerchain.validator;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorFloorPDU;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

public class PowerConnectionLoadValidator implements Validator {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired(required=true)
	private PowerCircuitDAO powerCircuitDAO;

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private EnoughPowerValidatorFloorPDU enoughPowerValidatorFloorPDU;

	
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
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		// Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		validatePowerConnectionLoadLimit(powerConn, errors);
		
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

	private void validatePowerConnectionLoadLimit(PowerConnection powerConn, Errors errors) {
		// get the present load on this port from the DB
		double srcLoad = 0.0; 
		double dstLoad = 0.0;
		
		//Get the ports that are getting changed
		PowerPort srcPort = powerConn.getSourcePowerPort();
		PowerPort dstPort = powerConn.getDestPowerPort();

		// get the current load at the port
		srcLoad = getLoad(srcPort);
		
		// Skip the load/limit validation if the floor pdu is not loaded 
		if (0 == srcLoad) return;
		
		// get the load on the destination port
		dstLoad = getLoad(dstPort);  

		// get the current amps used at the port
		double ampsAtSrc = (null == srcPort.getPortId()) ? 0.0 : enoughPowerValidatorFloorPDU.getCurrentAmpsAtNode(srcPort.getPortId()); 
		
		// get the load limit
		double ulRating = getULRating(srcPort);
		double srcLimit = getSrcLimit(srcPort, ulRating);
		double dstLimit = getDstLimit(dstPort, ulRating);
		
		if (srcLoad > srcLimit) {

			Object[] errorArgs = new Object[] {
					// srcPort.getPortSubClassLookup().getLkpValue(),
					srcPort.getDisplayName(),
					srcLoad - srcLimit
					};
			errors.reject("powerProc.insufficientPowerAtPort", errorArgs, "Source Ports Load is more than the limit");

		}
		
		if (dstLoad > dstLimit) {
			
			Object[] errorArgs = new Object[] {
					// dstPort.getPortSubClassLookup().getLkpValue(),
					dstPort.getDisplayName(),
					dstLoad - dstLimit
					};
			errors.reject("powerProc.insufficientPowerAtPort", errorArgs, "Source Ports Load is more than the limit");
			
		}
		
		double srcAmpsLimit = getSrcAmps(srcPort, ulRating);
		
		if (ampsAtSrc > srcAmpsLimit) {
			Object[] errorArgs = new Object[] {
					// srcPort.getPortSubClassLookup().getLkpValue(),
					srcPort.getDisplayName(),
					ampsAtSrc - srcAmpsLimit
					};
			errors.reject("powerProc.insufficientAmpsAtPort", errorArgs, "Source Ports Amps is more than the limit");

			
		}
		
	
		// for the ups bank, skip the amps validation
		if (null != dstPort.getPortSubClassLookup() && null != dstPort.getPortSubClassLookup().getLkpValueCode() && 
				dstPort.getPortSubClassLookup().getLkpValueCode().longValue() != SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER) {

			double dstAmpsLimit = getSrcAmps(dstPort, ulRating);
			double ampsAtDst = (null == dstPort.getPortId()) ? 0.0 : enoughPowerValidatorFloorPDU.getCurrentAmpsAtNode(dstPort.getPortId());

			if (ampsAtDst > dstAmpsLimit) {
				Object[] errorArgs = new Object[] {
						// srcPort.getPortSubClassLookup().getLkpValue(),
						dstPort.getDisplayName(),
						ampsAtDst - dstAmpsLimit
						};
				errors.reject("powerProc.insufficientAmpsAtPort", errorArgs, "Source Ports Amps is more than the limit");
				
			}
		}
		
		
	}
	
	private double getSrcLimit(PowerPort port, double ulRating) {

		if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER) {
			PowerBankInfo bankInfo =  powerCircuitDAO.getPowerBankInfo(port.getItem().getItemId());
			
			if(bankInfo.getUnits() != null && bankInfo.getRedundancy() != null && bankInfo.getRating_kw() != null) {
				double maxkW=0;
				if(bankInfo.getRedundancy().equals("N")){
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits();
				}
				else{
					int redun = Integer.valueOf(bankInfo.getRedundancy().substring(2));
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits() - bankInfo.getUnits() * redun;
				}
				return (maxkW * 1000);
			}
			return 0;
		}
		else {
			double amps=0, watts=0;
			
			amps = getSrcAmps(port, ulRating);
			
			watts = computeWatts(port, amps);
			
			return watts;
		}
		
	}
	
	private double getDstLimit(PowerPort port, double ulRating) {
		if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER) {
			PowerBankInfo bankInfo =  powerCircuitDAO.getPowerBankInfo(port.getItem().getItemId());
			
			if(bankInfo.getUnits() != null && bankInfo.getRedundancy() != null && bankInfo.getRating_kw() != null) {
				double maxkW=0;
				if(bankInfo.getRedundancy().equals("N")){
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits();
				}
				else{
					int redun = Integer.valueOf(bankInfo.getRedundancy().substring(2));
					maxkW = bankInfo.getRating_kw() * bankInfo.getUnits() - bankInfo.getUnits() * redun;
				}
				return (maxkW * 1000);
			}
			return 0;
		}
		else {
			double amps=0, watts=0;
			
			amps = getDstAmps(port, ulRating);
			
			watts = computeWatts(port, amps);
			
			return watts;
		}
		
	}
	
	private long getLoad(PowerPort port) {
		if (null == port.getPortId()) {
			return 0;
		}
		return powerCircuitDAO.getPowerWattUsedTotal(port.getPortId(), null);
	}


	private double getSrcAmps(PowerPort srcPort, double ulRating) { 
		return srcPort.isPowerSupply() ? srcPort.getAmpsBudget() : srcPort.getAmpsNameplate() * ulRating;
	}
	
	private double getDstAmps(PowerPort dstPort, double ulRating) {
		return dstPort.isPowerSupply() ? 0.0 : dstPort.getAmpsNameplate()  * ulRating;
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
	
	private double getULRating(PowerPort powerPort) {
		Item item = powerPort.getItem();
		if (item instanceof MeItem) {
		}
		else {
			Item itemUnProxy = itemDAO.initializeAndUnproxy(item);
			if (itemUnProxy instanceof MeItem) {
				item = itemUnProxy;
			}
			else {
				log.debug("Cannot get the data center information for the item");
				item = null;
			}
		}
		
		if (item == null) return 1.0;
		
		//Get the location of the PDU
		//Based on the location, we multiply the rating amps with 0.8
		String country = 
				item.getDataCenterLocation() != null && item.getDataCenterLocation().getDcLocaleDetails() != null
						? item.getDataCenterLocation().getDcLocaleDetails().getCountry():"";
		return country.equalsIgnoreCase("United States") ? 0.8 : 1.0;
	}

	
}
