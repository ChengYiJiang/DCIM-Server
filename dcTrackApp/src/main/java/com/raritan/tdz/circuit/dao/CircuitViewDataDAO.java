package com.raritan.tdz.circuit.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.CircuitViewData;

/**
 * DAO for the circuit view data
 * @author bunty
 *
 */
public interface CircuitViewDataDAO extends Dao<CircuitViewData> {
	
	/**
	 * get the circuit details for the given circuit id and circuit type
	 * @param circuitId
	 * @param circuitType
	 * @return
	 */
	public CircuitViewData getCircuitViewData(Long circuitId, Long circuitType);

}
