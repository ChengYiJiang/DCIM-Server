package com.raritan.tdz.port.home;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * power outlet port object
 * @author bunty
 *
 */
public class PowerOutletBreakerPortObject extends PowerPortObject {

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.WHIP_OUTLET  );
		codes.add( SystemLookup.PortSubClass.BUSWAY_OUTLET  );
		
		return Collections.unmodifiableSet( codes );
	}

	@SuppressWarnings("unused")
	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(-1L); // supports all
		return Collections.unmodifiableSet( supportingItemClassMountingFormFactor );
	}

	protected void validateInvalidFields(Errors errors) {

		// validate power factor range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "powerFactor", "Power Factor", "PortValidator.powerIncorrectFieldValue", 0L, 1L);

		// validate sort order
		// powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "sortOrder", "Index", "PortValidator.powerIncorrectFieldValue", 0L, null);

		// validate port name range
		long MAX_PORT_LENGTH = 64;
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "portName", "Port Name", "PortValidator.powerIncorrectFieldValue", 0L, MAX_PORT_LENGTH);

	}

	@Override
	protected void validateRequiredFields(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		// FIXME:: commented because the requirement is not clear if these should be the required fields for breaker ports
		//fields.put("phaseLookup", "Phase");
		//fields.put("voltsLookup", "Volts");
		fields.put("portName", "Port Name");
		fields.put("portSubClassLookup", "Port Type");
		// fields.put("phaseLegsLookup", "Phase Legs");
		
		powerPortObjectHelper.validateRequiredFields(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.powerPortFieldRequired");

	}

	/**
	 * validates the port. target is the item domain to be saved
	 */
	@Override
	public void validateSave(Object target, Errors errors) {
		super.validateSave(target, errors);

		// Validates the invalid value in the fields
		validateInvalidFields(errors);
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
		codes.add( -1L ); // support all classes
		return Collections.unmodifiableSet( codes );
	}
	
	@Override
	public void applyCommonAttributes(IPortInfo refPort, Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		/*fields.put("powerFactor", "Power Factor");
		fields.put("wattsNameplate", "Watts Nameplate");
		fields.put("wattsBudget", "Watts Budget");*/
		
		powerPortObjectHelper.applyCommonAttributes(getPortInfo(), refPort, errors, fields, "PortValidator.commonAttributeVoilations");
		
	}

	@Override
	public void validateItemClassSubclass(Object target, Errors errors) {
		// TODO Auto-generated method stub
		
	}
	
	

	
}
