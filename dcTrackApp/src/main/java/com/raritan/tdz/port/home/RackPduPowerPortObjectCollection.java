package com.raritan.tdz.port.home;

import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.util.GlobalUtils;

public class RackPduPowerPortObjectCollection extends PowerPortObjectCollection {

	public RackPduPowerPortObjectCollection(IPortObjectFactory portObjectFactory) {
		super(portObjectFactory);
	}
	
	@Override
	public void deleteInvalidPorts(Errors errors) {
		// Delete all the power ports of Rack PDU if the voltage is not valid for any port
		if (!isVoltageValid()) {
			validateInvalidPortDelete(errors);
			item.getPowerPorts().clear();
		}
	}
	
	private void validateInvalidPortDelete(Errors errors) {
		Set<PowerPort> powerPorts = getItem().getPowerPorts();
		if (null == powerPorts) {
			return;
		}
		for (PowerPort powerPort: powerPorts) {
			IPortObject port = portObjectFactory.getPortObject(powerPort, errors);
			port.validateDelete(item, errors);
		}
	}
	
	private boolean isVoltageValid() {
		boolean invalidOutputVolt = false;
		Set<PowerPort> powerPorts = getItem().getPowerPorts();
		if (null == powerPorts) {
			return true;
		}
		for (PowerPort powerPort: powerPorts) {
			if (null != powerPort.getVoltsLookup() && null != powerPort.getVoltsLookup().getLkpValue() && !GlobalUtils.isNumeric(powerPort.getVoltsLookup().getLkpValue().trim())) {
				invalidOutputVolt = true;
				break;
			}
		}
		return !invalidOutputVolt;
	}
	
	@Override
	public void validateSortOrder(Errors errors) {
		// Do not validate sort order for rack pdu power ports
	}
	
	@Override
	public void postSave(UserInfo userInfo, Errors errors) {
		updateSortOrder(errors);
	}
	
}
