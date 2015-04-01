package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;

public interface PowerCircuitHelper {

	void updateCircuitTrace(PowerCircuitInfo oldPowerCirInfo,
			PowerCircuitInfo newPowerCirInfo);

	PowerCircuitInfo getCircuitTraceInfo(PowerPort port, Errors errors);
	
}
