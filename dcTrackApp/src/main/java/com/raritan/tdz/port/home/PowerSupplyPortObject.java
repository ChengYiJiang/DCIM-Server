package com.raritan.tdz.port.home;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.GlobalUtils;

public class PowerSupplyPortObject extends PowerPortObject {
	
	@Autowired(required=true)
	PowerConnDAO powerConnectionDAO;

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.POWER_SUPPLY );
		return Collections.unmodifiableSet( codes );
	}

	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(104L); // Device Blade Chassis
		supportingItemClassMountingFormFactor.add(101L); // Device Standard Rackable
		supportingItemClassMountingFormFactor.add(103L); // Device Standard FS
		supportingItemClassMountingFormFactor.add(102L); // Device Standard NR
		supportingItemClassMountingFormFactor.add(106L); // Device Standard ZU
		supportingItemClassMountingFormFactor.add(204L); // Network Chassis
		supportingItemClassMountingFormFactor.add(203L); // Network Stack FS
		supportingItemClassMountingFormFactor.add(201L); // Network Stack Rackable
		supportingItemClassMountingFormFactor.add(202L); // Network Stack NR
		supportingItemClassMountingFormFactor.add(206L); // Network Stack ZU
		supportingItemClassMountingFormFactor.add(701L); // Probe Rackable
		supportingItemClassMountingFormFactor.add(706L); // Probe ZU
		supportingItemClassMountingFormFactor.add(702L); // Probe NR
		return Collections.unmodifiableSet( supportingItemClassMountingFormFactor );
	}

	@Override
	public void validateItemClassSubclass(Object target, Errors errors) {
		Item item = null;
		if ((null != target) && (target instanceof Item)) {
			item = (Item) target;
		}
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		Set<Long> supportingItemClassMountingFormFactor = getSupportingItemClassMountingFormFactor();
		if (!supportingItemClassMountingFormFactor.contains(classMountingFormFactorValue)) {
			Object[] errorArgs = {};
			errors.rejectValue("tabDataPorts", "PortValidator.powerSupplyUnsupportedClass", errorArgs, 
					"Power Supply port can only be created for Device-Blade Chassis, Device-Standard, Network-Chassis, Network-NetworkStack and Probe");
		}
	}
	
	private void validateMoreInvalidFields(Errors errors) {

		// validate power supply port index range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "sortOrder", "Index", "PortValidator.powerSupplyPortIndexRangeIncorrect", 0L, 8L);

		// validate power factor range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "powerFactor", "Power Factor", "PortValidator.powerIncorrectFieldValue", 0L, 1L);
		
		// validate watts nameplate range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "wattsNameplate", "Watts(Nameplate)", "PortValidator.powerIncorrectFieldValue", 0L, null);
		
		// validate watts budget range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "wattsBudget", "Watts(Budget)", "PortValidator.powerIncorrectFieldValue", 0L, null);
		
	}
	
	/**
	 * validates the port. target is the item domain to be saved
	 */
	@Override
	public void validateSave(Object target, Errors errors) {
		super.validateSave(target, errors);
		
		// validate range
		validateMoreInvalidFields(errors);
		
		// Validate phase type and supported voltages
		validatePhaseAndVolts(errors);
		
		// validate supporting item class:subclass
		validateItemClassSubclass(target, errors);
		
		// valiate that watts budget is less than watts Nameplate
		validateWattsBudgetLessThanWattsNameplate(errors);
	}
	
	private void validateWattsBudgetLessThanWattsNameplate(Errors errors) {
		PowerPort powerPort = getPowerPort();
		
		if (powerPort.getWattsBudget() > powerPort.getWattsNameplate()) {
			
			Object[] errorArgs = { powerPort.getPortName(), powerPort.getWattsNameplate() };
			errors.rejectValue("tabPowerPorts", "PortValidator.wattsNameplateLessThanWattsBudget", errorArgs, "Budget Watts must be less than or equal to the Nameplate Watts");
		}
	}
	
	// Phase and supported voltages validation
	private void validatePhaseAndVolts(Errors errors) {
		PowerPort powerPort = getPowerPort();
		
		if (powerPort.getPhaseLookup() != null && null != powerPort.getPhaseLookup().getLkpValueCode() ) {
			long phaseLookup = powerPort.getPhaseLookup().getLkpValueCode().longValue();
			if (phaseLookup == SystemLookup.PhaseIdClass.SINGLE_2WIRE || 
					phaseLookup == SystemLookup.PhaseIdClass.SINGLE_3WIRE) {
				if (null == powerPort.getVoltsLookup() || null == powerPort.getVoltsLookup().getLkpValueCode() ||
						(powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_120_240 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_120 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_240 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_277)) {
					List<ObjectError> errorList = errors.getAllErrors();
					for (ObjectError error : errorList) {
					      if (error.getCode().equals("PortValidator.portInvalidSinglePhaseVolt")) {
					    	  return;
					   }
					}
					Object[] errorArgs = { };
					errors.rejectValue("tabPowerPorts", "PortValidator.portInvalidSinglePhaseVolt", errorArgs, "Invalid voltage value for single phase power supply. Supporting voltages are 120~240, 120, 240, 277");
				}
			}
			else if (phaseLookup == SystemLookup.PhaseIdClass.THREE_DELTA || 
					phaseLookup == SystemLookup.PhaseIdClass.THREE_WYE) {
				if (null == powerPort.getVoltsLookup() || null == powerPort.getVoltsLookup().getLkpValueCode() ||
						(powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_208 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_380 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_400 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_415 &&
						powerPort.getVoltsLookup().getLkpValueCode().longValue() != SystemLookup.VoltClass.V_480)) {
					List<ObjectError> errorList = errors.getAllErrors();
					for (ObjectError error : errorList) {
					      if (error.getCode().equals("PortValidator.portInvalidThreePhaseVolt")) {
					    	  return;
					   }
					}
					Object[] errorArgs = { };
					errors.rejectValue("tabPowerPorts", "PortValidator.portInvalidThreePhaseVolt", errorArgs, "Invalid voltage value for three phase power supply. Supporting voltages are 208, 380, 400, 415, 480");
				}
			} 
		}
	}
	
	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
		
	}

	@Override
	public boolean isConnectorValid() {
		PowerPort powerPort = getPowerPort();
		return (powerPort.getConnectorLookup() != null);
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.DEVICE );
		codes.add( SystemLookup.Class.NETWORK );
		codes.add( SystemLookup.Class.PROBE );
		codes.add( SystemLookup.Class.CRAC );
		codes.add( SystemLookup.Class.CRAC_GROUP );
		return Collections.unmodifiableSet( codes );
	}

	private void updateAmps(Errors errors) {
		PowerPort pp = getPowerPort();
		
		if (pp.getUsed()) 	{
			PowerPort connectedPowerPort = null;
			if (null != pp.getPortId() && pp.getPortId().longValue() > 0) {
				
				// Do not update the amps if the watts have not changed
				PowerPort savedPort = getSavedPort(pp.getPortId());
				if (null != savedPort) {
					if (pp.getWattsBudget() == savedPort.getWattsBudget() &&
							pp.getWattsNameplate() == savedPort.getWattsNameplate() && pp.getPowerFactor() == savedPort.getPowerFactor()) {
						return;
					}
				}
				
				try {
					connectedPowerPort = powerConnectionDAO.getDestinationPort(pp.getPortId());
				}
				catch (DataAccessException ex) {
					throw new InvalidPortObjectException("Cannot get destination port for port: " + pp.getPortName());
				}
			}
			String volt = null; 
			if (null != connectedPowerPort) {
				volt = connectedPowerPort.getVoltsLookup().getLkpValue();
			}
			else {
				volt = pp.getVoltsLookup().getLkpValue();
			}
			if (null != volt && GlobalUtils.isNumeric(volt)) {
				Double voltVal = null;
				if ( pp.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.SINGLE_2WIRE || 
						pp.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.SINGLE_3WIRE ) {
					voltVal = Double.parseDouble(volt);
				}
				else {
					voltVal = Integer.parseInt(volt) * Math.sqrt(3);
				}
				
				// update the amps to 2 decimal places
				pp.setAmpsBudget( roundTwoDecimals(( pp.getWattsBudget() / pp.getPowerFactor() ) / voltVal) );
				pp.setAmpsNameplate( roundTwoDecimals(( pp.getWattsNameplate() / pp.getPowerFactor() ) / voltVal) );
			}
		}
		else {

			// port is not in use: update the amps to 0
			pp.setAmpsBudget( 0 );
			pp.setAmpsNameplate( 0 );
		}
	}
	
	@Override
	public void preValidateUpdates(Errors errors) {
		updateAmps(errors);
	}
	
	private PowerPort getSavedPort(Long portId) {
		Item savedItem = SavedItemData.getCurrentItem().getSavedItem();
		
		if (null == savedItem) return null;
	
		Set<PowerPort> ports = savedItem.getPowerPorts();
		
		for (PowerPort port: ports) {
			if (port.getPortId().longValue() == portId.longValue()) {
				return port;
			}
		}
		
		return null;
	}
	
	
	private double roundTwoDecimals(double d) {
	    DecimalFormat twoDForm = new DecimalFormat("#.##");
	    return Double.valueOf(twoDForm.format(d));
	}

	@Override
	public void applyCommonAttributes(IPortInfo refPort, Errors errors) {

		Map<String, String> fields = new HashMap<String, String>();
		fields.put("connectorLookup", "Connector");
		fields.put("phaseLookup", "Phase Type");
		fields.put("voltsLookup", "Volt");
		fields.put("powerFactor", "Power Factor");
		fields.put("wattsNameplate", "Watts Nameplate");
		fields.put("wattsBudget", "Watts Budget");
		
		powerPortObjectHelper.applyCommonAttributes(getPortInfo(), refPort, errors, fields, "PortValidator.commonAttributeVoilations");
		
	}


}
