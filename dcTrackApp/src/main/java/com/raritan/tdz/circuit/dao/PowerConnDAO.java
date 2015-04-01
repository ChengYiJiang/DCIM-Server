package com.raritan.tdz.circuit.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;

public interface PowerConnDAO extends Dao<PowerConnection>{
	/**
	 * Read an existing power connection from database
	 * @param id - Id of power connection to be loaded
	 * @return PowerConnection 
	 */	
	public PowerConnection getConn(Long id)  throws DataAccessException;

	/**
	 * Load an existing power connection from database using lazying loading and a new hibernate session
	 * @param id - Id of power connection to be loaded
	 * @return PowerConnection 
	 */	
	public PowerConnection loadConn(Long id)  throws DataAccessException;

	/**
	 * Load an existing power connection from database using lazying loading and a new hibernate session
	 * @param id - Id of power connection to be loaded
	 * @param readOnly - true for read-only, false to allow update
	 * @return PowerConnection 
	 */		
	public PowerConnection loadConn(Long id, boolean readOnly)  throws DataAccessException;
	
	
	/**
	 * Get a list of power connection for an item
	 * @param itemId - Id of item to get power connections
	 * @return List of PowerConnection objects 
	 */		
	public List<PowerConnection> getConnsForItem(long itemId) throws DataAccessException;
	

	/**
	 * Test if port is a source port
	 * @param portId - Id of power port to check
	 * @return True if power port is the source port of a connection 
	 */		
	public boolean isSourcePort(long portId) throws DataAccessException;
	
	
	/**
	 * Test if port is a destination port
	 * @param portId - Id of power port to check
	 * @return True if power port is the destination port of a connection 
	 */		
	public boolean isDestinationPort(long portId) throws DataAccessException;

	/**
	 * Get destination port connected to a given port Id
	 * @param portId - Id of power port to find destionation port
	 * @return  power port object 
	 */		
	public PowerPort getDestinationPort(long portId) throws DataAccessException;	

	/**
	 * Get source port connected to a given port Id
	 * @param portId - Id of power port to find source port
	 * @return  power port object 
	 */		
	public PowerPort getSourcePort(long portId) throws DataAccessException;		
	
	/**
	 * Checks if the connectors are compatible.
	 * @param srcConnector TODO
	 * @param dstConnector TODO
	 * @return
	 */
	public boolean areConnectorsCompatible(ConnectorLkuData srcConnector, ConnectorLkuData dstConnector);
	
	/**
	 * migrates all the power circuits to the new ports of power panels, PDU and UPS Bank
	 */
	public void migratePowerCircuit();

	/**
	 * completes the power circuits created during the circuit import
	 */
	void completeImportPowerCircuit();

	/**
	 * get the list of power connections between given port subclass
	 * @param srcPortSubclass
	 * @param dstPortSubclass
	 * @return
	 */
	List<PowerConnection> getConnBetweenPortSubclass(Long srcPortSubclass,
			Long dstPortSubclass);

	/**
	 * Update status of the connection 
	 * @param connectionId
	 * @param connectionStatus
	 */
	public void setConnectionStatus(long connectionId, long connectionStatus);

	public List<PowerConnection> getConnectionsForSourcePort(Long portId);

	public List<PowerConnection> getConnectionForDestPort(Long portId);

}
