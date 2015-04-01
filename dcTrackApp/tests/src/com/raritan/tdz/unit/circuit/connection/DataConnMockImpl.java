package com.raritan.tdz.unit.circuit.connection;

import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class DataConnMockImpl implements DataConnMock {
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;
	
	@Autowired
	protected SystemLookupInitUnitTest systemLookupInitTest;

	@Autowired
	DataConnExpectations expectations;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnMock#createConnPlannedExplicit(org.jmock.Mockery, com.raritan.tdz.domain.DataPort, com.raritan.tdz.domain.DataPort)
	 */
	public DataConnection createConnPlannedExplicit(Mockery jmockContext, DataPort sourcePort, DataPort destPort){
		DataConnection conn = createConnection(jmockContext, sourcePort, destPort,SystemLookup.LinkType.EXPLICIT, SystemLookup.ItemStatus.PLANNED);
		
		return conn;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnMock#createConnPlannedImplicit(org.jmock.Mockery, com.raritan.tdz.domain.DataPort, com.raritan.tdz.domain.DataPort)
	 */
	public DataConnection createConnPlannedImplicit(Mockery jmockContext, DataPort sourcePort, DataPort destPort){
		DataConnection conn = createConnection(jmockContext, sourcePort, destPort,SystemLookup.LinkType.IMPLICIT, SystemLookup.ItemStatus.PLANNED);
		
		return conn;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnMock#createConnInstalledExplicit(org.jmock.Mockery, com.raritan.tdz.domain.DataPort, com.raritan.tdz.domain.DataPort)
	 */
	public DataConnection createConnInstalledExplicit(Mockery jmockContext, DataPort sourcePort, DataPort destPort){
		DataConnection conn = createConnection(jmockContext, sourcePort, destPort,SystemLookup.LinkType.EXPLICIT, SystemLookup.ItemStatus.INSTALLED);
		
		return conn;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnMock#createConnInstalledImplicit(org.jmock.Mockery, com.raritan.tdz.domain.DataPort, com.raritan.tdz.domain.DataPort)
	 */
	public DataConnection createConnInstalledImplicit(Mockery jmockContext, DataPort sourcePort, DataPort destPort){
		DataConnection conn = createConnection(jmockContext, sourcePort, destPort,SystemLookup.LinkType.IMPLICIT, SystemLookup.ItemStatus.INSTALLED);
		
		return conn;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.circuit.connection.DataConnMock#createConnection(org.jmock.Mockery, com.raritan.tdz.domain.DataPort, com.raritan.tdz.domain.DataPort, java.lang.Long, java.lang.Long)
	 */
	public DataConnection createConnection(Mockery jmockContext, DataPort sourcePort, DataPort destPort, Long typeValueCode, Long statusValueCode){
		DataConnection conn = new DataConnection();
		LksData statusLookup = systemLookupInitTest.getLks(statusValueCode);
		LksData connectionType = systemLookupInitTest.getLks(typeValueCode);
		
		conn.setConnectionId(unitTestIdGenerator.nextId());
		conn.setConnectionType(connectionType);
		conn.setSourceDataPort(sourcePort);
		conn.setDestDataPort(destPort);		
		conn.setStatusLookup(statusLookup);
		
		Long connectionId = conn.getConnectionId();
		
		expectations.createGetConn(jmockContext, connectionId, conn);
		
		if(destPort != null){
			expectations.createIsDestinationPort(jmockContext, destPort.getPortId(), true);
		}
		
		expectations.createIsSourcePort(jmockContext, sourcePort.getPortId(), true);
		expectations.createLoadConn(jmockContext, connectionId, conn);
		expectations.createLoadConn(jmockContext, connectionId, false, conn);
		expectations.createLoadConn(jmockContext, connectionId, true, conn);
		expectations.createOneOfRead(jmockContext, connectionId, conn);
		expectations.createRead(jmockContext, connectionId, conn);
		
		return conn;
	}	
}
