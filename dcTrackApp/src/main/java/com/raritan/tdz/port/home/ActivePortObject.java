package com.raritan.tdz.port.home;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

public class ActivePortObject extends DataPortObject {
	
	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
	}

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.ACTIVE );
		return Collections.unmodifiableSet( codes );
	}

	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(105L); // Device Blade Server
		supportingItemClassMountingFormFactor.add(104L); // Device Blade Chassis
		supportingItemClassMountingFormFactor.add(101L); // Device Standard Rackable
		supportingItemClassMountingFormFactor.add(103L); // Device Standard FS
		supportingItemClassMountingFormFactor.add(102L); // Device Standard NR
		supportingItemClassMountingFormFactor.add(106L); // Device Standard ZU
		
		supportingItemClassMountingFormFactor.add(205L); // Network Blade
		supportingItemClassMountingFormFactor.add(204L); // Network Chassis
		supportingItemClassMountingFormFactor.add(203L); // Network Stack FS
		supportingItemClassMountingFormFactor.add(201L); // Network Stack Rackable
		supportingItemClassMountingFormFactor.add(202L); // Network Stack NR
		supportingItemClassMountingFormFactor.add(206L); // Network Stack ZU
		
		//supportingItemClassMountingFormFactor.add(301L); // Data Panel Rackable
		//supportingItemClassMountingFormFactor.add(302L); // Data Panel NR
		//supportingItemClassMountingFormFactor.add(306L); // Data Panel ZU
		
		//supportingItemClassMountingFormFactor.add(412L); // Power Outlet - Busway Outlet
		//supportingItemClassMountingFormFactor.add(411L); // Power Outlet - Whip Outlet
		
		supportingItemClassMountingFormFactor.add(506L); // Rack PDU - ZU
		supportingItemClassMountingFormFactor.add(501L); // Rack PDU - Rackable
		supportingItemClassMountingFormFactor.add(502L); // Rack PDU - NR
		
		//supportingItemClassMountingFormFactor.add(603L); // Cabinet
		//supportingItemClassMountingFormFactor.add(613L); // Cabinet - Container
		
		supportingItemClassMountingFormFactor.add(701L); // Probe Rackable
		supportingItemClassMountingFormFactor.add(706L); // Probe ZU
		supportingItemClassMountingFormFactor.add(702L); // Probe NR
		
		supportingItemClassMountingFormFactor.add(1103L); // Floor PDU - FS Non Cabinet
		supportingItemClassMountingFormFactor.add(1114L); // Floor PDU - Panel Board (Local)
		supportingItemClassMountingFormFactor.add(1115L); // Floor PDU - FS Non Cabinet (Remote)
		supportingItemClassMountingFormFactor.add(1116L); // Floor PDU - Busway
		
		supportingItemClassMountingFormFactor.add(1203L); // UPS
		
		supportingItemClassMountingFormFactor.add(1303L); // CRAC
		
		return Collections.unmodifiableSet( supportingItemClassMountingFormFactor );
	}

	@Override
	protected void validateRequiredFields(Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("mediaId", "Media");
		fields.put("protocolID", "Protocol");
		fields.put("speedId", "Data rate");
		fields.put("portName", "Port Name");
		fields.put("portSubClassLookup", "Port Type");
		
		dataPortObjectHelper.validateRequiredFields(getPortInfo(), errors, dataPortDAO, fields, "PortValidator.dataPortFieldRequired");
	}
	

	@Override
	public void validateItemClassSubclass(Object target, Errors errors) {
		Item item = null;
		if ((null != target) && (target instanceof Item)) {
			item = (Item) target;
		}
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		Set<Long> supportingItemClassMountingFormFactor = getSupportingItemClassMountingFormFactor();
		if (!supportingItemClassMountingFormFactor.contains(classMountingFormFactorValue) && 
				!errorExist(errors, "PortValidator.dataPortUnsupportedClass", null)) {
			Object[] errorArgs = {};
			errors.rejectValue("tabDataPorts", "PortValidator.dataPortUnsupportedClass", errorArgs, 
					"Data Port cannot be created for the selected make and model.");
		}
	}

	@Override
	public void validateSave(Object target, Errors errors) {
		super.validateSave(target, errors);
		
		validateItemClassSubclass(target, errors);
		
	}

	@Override
	public boolean isConnectorValid() {
		DataPort dataPort = getDataPort();
		return (dataPort.getConnectorLookup() != null);
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( -1L );
		return Collections.unmodifiableSet( codes );
	}

}
