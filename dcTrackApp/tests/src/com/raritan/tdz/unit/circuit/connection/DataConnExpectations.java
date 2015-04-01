package com.raritan.tdz.unit.circuit.connection;

import java.util.List;

import org.jmock.Mockery;

import com.raritan.tdz.domain.DataConnection;

public interface DataConnExpectations {

	public abstract void createRead(Mockery jmockContext, Long connectionId,
			DataConnection retValue);

	public abstract void createOneOfRead(Mockery jmockContext,
			Long connectionId, DataConnection retValue);

	public abstract void createGetConn(Mockery jmockContext, Long connectionId,
			DataConnection retValue);

	public abstract void createLoadConn(Mockery jmockContext,
			Long connectionId, DataConnection retValue);

	public abstract void createLoadConn(Mockery jmockContext,
			Long connectionId, Boolean readOnly, DataConnection retValue);

	public abstract void createGetConnsForItem(Mockery jmockContext,
			Long itemId, List<DataConnection> retValue);

	public abstract void createIsSourcePort(Mockery jmockContext, Long portId,
			Boolean retValue);

	public abstract void createIsDestinationPort(Mockery jmockContext,
			Long portId, Boolean retValue);

	public abstract void createGetPanelToPanelConn(Mockery jmockContext,
			Long portId, DataConnection retValue);

	public abstract void createIsLogicalConnectionsExist(Mockery jmockContext,
			Long sourceItemId, Long destItemId, DataConnection retValue);

}