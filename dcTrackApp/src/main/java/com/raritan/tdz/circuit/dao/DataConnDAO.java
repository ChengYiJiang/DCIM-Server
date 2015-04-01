package com.raritan.tdz.circuit.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.exception.DataAccessException;

public interface DataConnDAO extends Dao<DataConnection>{
	/**
	 * Read an existing data connection from database
	 * @param id - Id of data connection to be loaded
	 * @return DataConnection 
	 */	
	public DataConnection getConn(Long id)  throws DataAccessException;

	/**
	 * Load an existing data connection from database using lazying loading and a new hibernate session
	 * @param id - Id of data connection to be loaded
	 * @return DataConnection 
	 */	
	public DataConnection loadConn(Long id)  throws DataAccessException;

	/**
	 * Load an existing data connection from database using lazying loading and a new hibernate session
	 * @param id - Id of data connection to be loaded
	 * @param readOnly - true for read-only, false to allow update
	 * @return DataConnection 
	 */		
	public DataConnection loadConn(Long id, boolean readOnly)  throws DataAccessException;

	/**
	 * Get the far-end/near-end connection of a port that is part of a data panel
	 * @param portId - Id of data connection to be loaded
	 * @return DataConnection 
	 */		
	public DataConnection getPanelToPanelConn(Long portId) throws DataAccessException;
	
	/**
	 * Get a list of data connection for an item
	 * @param itemId - Id of item to get data connections
	 * @return List of DataConnection objects 
	 */		
	public List<DataConnection> getConnsForItem(long itemId) throws DataAccessException;
	

	/**
	 * Test if a logical connection exists between two items
	 * @param sourceItemId - Id of item from where connection start (source port item id)
	 * @param destItemId - Id of item from where connection end (dest port item id)
	 * @return DataConnection 
	 */		
	public boolean isLogicalConnectionsExist(long sourceItemId, long destItemId)	throws DataAccessException;
	

	/**
	 * Test if port is a source port
	 * @param portId - Id of data port to check
	 * @return True if data port is the source port of a connection 
	 */		
	public boolean isSourcePort(long portId) throws DataAccessException;
	
	
	/**
	 * Test if port is a destination port
	 * @param portId - Id of data port to check
	 * @return True if data port is the destination port of a connection 
	 */		
	public boolean isDestinationPort(long portId) throws DataAccessException;
	
	
	/**
	 * Update status of the connection 
	 * @param connectionId
	 * @param connectionStatus
	 */
	public void setConnectionStatus(long connectionId, long connectionStatus);

	boolean areConnectorsCompatible(ConnectorLkuData srcConnector, ConnectorLkuData dstConnector);

	public DataConnection getPortConnection(long portId, boolean isSourcePort)	throws DataAccessException;
}
