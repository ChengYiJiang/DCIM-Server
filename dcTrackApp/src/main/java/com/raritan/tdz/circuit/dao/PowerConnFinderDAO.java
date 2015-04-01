package com.raritan.tdz.circuit.dao;

import java.util.List;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

public interface PowerConnFinderDAO {
	/**
	 * Get connection details based on the id.
	 * @param id - power connection Id
	 * @return
	 */
	public List<PowerConnection> findById(Long id);
	
	/**
	 * Get source port details based on source port id.
	 * @param id - power port Id
	 * @return source port of connection
	 */
	public List<PowerPort> findSourcePort(Long id);

	/**
	 * Get destination port details based on the source port id.
	 * @param id - power port id
	 * @return destination port of connection
	 */
	public List<PowerPort> findDestinationPort(Long id);	
}
