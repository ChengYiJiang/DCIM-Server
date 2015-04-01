/**
 * 
 */
package com.raritan.tdz.circuit.dao;

import java.util.List;

import com.raritan.tdz.domain.PowerCircuit;

/**
 * @author prasanna
 *
 */
public interface PowerCircuitFinderDAO {
	public List<PowerCircuit> fetchPowerCircuitForStartPort(Long portId);
}
