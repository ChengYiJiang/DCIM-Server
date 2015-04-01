package com.raritan.tdz.circuit.dao;

import com.raritan.tdz.domain.PowerCircuit;

public class PowerCircuitDAOExtImpl extends CircuitDAOImpl<PowerCircuit> {

	public PowerCircuitDAOExtImpl(Class<PowerCircuit> type, 
			String isCircuitInCabinetQuery, 
			String getAssociatedDataCircuitsForCabinet, 
			String getCabinetPlannedCircuitIdsNotMovingQuery, 
			String getParentMoveRequestQuery, 
			String getPendingCircuitRequestForItems,
			String getReconnectCircuitReqInfo, 
			String getDisAndMoveCircuitReqInfo) {
		
		super(type, isCircuitInCabinetQuery, 
				getAssociatedDataCircuitsForCabinet, 
				getCabinetPlannedCircuitIdsNotMovingQuery, 
				getParentMoveRequestQuery, 
				getPendingCircuitRequestForItems,
				getReconnectCircuitReqInfo, 
				getDisAndMoveCircuitReqInfo);
		
	}

}
