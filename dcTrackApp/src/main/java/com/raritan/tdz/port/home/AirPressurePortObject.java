package com.raritan.tdz.port.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.lookup.SystemLookup;

public class AirPressurePortObject extends SensorPortObject {
	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.PRESSUERE );
		return Collections.unmodifiableSet( codes );
	}


/*	@Override
	public void validateSave(Object target, Errors errors) {
		super.validateSave(target, errors);		
	}*/
	
	@Override
	public boolean isConnectorValid() {
		SensorPort sensorDAOPort = getSensorPort();
		return (sensorDAOPort.getConnectorLookup() != null);
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( -1L );
		return Collections.unmodifiableSet( codes );
	}

}
