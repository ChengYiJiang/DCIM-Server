package com.raritan.tdz.unit.circuit.connection;

import java.util.List;

import org.jmock.Mockery;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

public interface PowerConnExpectations {

	public abstract void createRead(Mockery jmockContext, Long connectionId,
			PowerConnection retValue);

	public abstract void createOneOfRead(Mockery jmockContext,
			Long connectionId, PowerConnection retValue);

	public abstract void createGetConn(Mockery jmockContext, Long connectionId,
			PowerConnection retValue);

	public abstract void createLoadConn(Mockery jmockContext,
			Long connectionId, PowerConnection retValue);

	public abstract void createLoadConn(Mockery jmockContext,
			Long connectionId, Boolean readOnly, PowerConnection retValue);

	public abstract void createGetConnsForItem(Mockery jmockContext,
			Long itemId, List<PowerConnection> retValue);

	public abstract void createIsSourcePort(Mockery jmockContext, Long portId,
			Boolean retValue);

	public abstract void createIsDestinationPort(Mockery jmockContext,
			Long portId, Boolean retValue);

	public abstract void createGetSourcePort(Mockery jmockContext, Long portId,
			PowerPort retValue);

	public abstract void createGetDestinationPort(Mockery jmockContext,
			Long portId, PowerPort retValue);

	public abstract void createAreConnectorsCompatible(Mockery jmockContext,
			ConnectorLkuData srcConnector, ConnectorLkuData dstConnector,
			boolean retValue);

	public abstract void createGetConnBetweenPortSubclass(Mockery jmockContext,
			Long srcPortSubclass, Long dstPortSubclass,
			List<PowerConnection> retValue);

	public abstract void createCompleteImportPowerCircuit(Mockery jmockContext);

	public abstract void createMigratePowerCircuit(Mockery jmockContext);

}