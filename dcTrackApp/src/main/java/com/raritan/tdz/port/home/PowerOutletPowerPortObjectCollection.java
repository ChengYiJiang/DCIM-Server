package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

public class PowerOutletPowerPortObjectCollection extends PowerPortObjectCollection {

	public PowerOutletPowerPortObjectCollection(
			IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}

	@Override
	public void validate(Errors errors) {
		// Do not validate sort order for power outlet power ports
	}
	

}
