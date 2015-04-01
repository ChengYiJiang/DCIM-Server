package com.raritan.tdz.circuit.dao;

import com.raritan.tdz.domain.DataCircuit;

public class DataCircuitDAOExtImpl extends CircuitDAOImpl<DataCircuit> {

	public DataCircuitDAOExtImpl(Class<DataCircuit> type, 
			String isCircuitInCabinetQuery, 
			String getAssociatedCircuitsForCabinet, 
			String getCabinetPlannedCircuitIdsNotMovingQuery, 
			String getParentMoveRequestQuery, 
			String getPendingCircuitRequestForItems,
			String getReconnectCircuitReqInfo, 
			String getDisAndMoveCircuitReqInfo) {
		
		super(type, isCircuitInCabinetQuery, 
				getAssociatedCircuitsForCabinet, 
				getCabinetPlannedCircuitIdsNotMovingQuery, 
				getParentMoveRequestQuery, 
				getPendingCircuitRequestForItems, 
				getReconnectCircuitReqInfo, 
				getDisAndMoveCircuitReqInfo);
	}

}
