package com.raritan.tdz.port.home;

import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataPort;

public class 
NetworkBladeDataPortObjectCollection  extends DataPortObjectCollection {

	public NetworkBladeDataPortObjectCollection(IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}
	
	@Override
	public void validate(Errors errors) {
		super.validate(errors);
		
		validatePortLimit(errors);
	}
	
	private void validatePortLimit(Errors errors) {
		int logicalPortCount = 0;
		Set<DataPort> dataPorts = getItem().getDataPorts();
		if (null == dataPorts) {
			return;
		}
		for (DataPort dataPort: dataPorts) {
			if (dataPort.isLogical()) {
				logicalPortCount++;
			}
		}
		if (logicalPortCount > 1) {
			Object[] errorArgs = {"Network Blade"};
			errors.rejectValue("tabPowerPorts", "PortValidator.invalidLogicalPortCount", errorArgs, "Network Blade can have only 1 Logical Port");
		}
	}

}
