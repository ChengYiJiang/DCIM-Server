package com.raritan.tdz.circuit.util;

import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.util.Power3Phase;
import com.raritan.tdz.domain.PowerPort;

public interface PowerCalc {

	/**
	 * This is the method that calculates the node sum. 
	 * @param port
	 * @param ampsToBeAdded
	 * @param nodeInfo
	 * @param portIdToExclude
	 * @param fuseLkuId
	 * @return
	 */
	public Power3Phase get3PhaseNodeSum(PowerPort port,
			double ampsToBeAdded, PowerWattUsedSummary nodeInfo, Long portIdToExclude,
			Long fuseLkuId);

	public Power3Phase get3PhaseNodeSum(PowerPort port);

	public Power3Phase get3PhaseNodeSum(PowerPort port, double ampsToBeAdded);

	public Power3Phase get3PhaseNodeSumMeasured(PowerPort port);

}