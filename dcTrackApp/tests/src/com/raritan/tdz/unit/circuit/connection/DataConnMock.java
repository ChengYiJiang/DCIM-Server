package com.raritan.tdz.unit.circuit.connection;

import org.jmock.Mockery;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;

public interface DataConnMock {

	public abstract DataConnection createConnPlannedExplicit(
			Mockery jmockContext, DataPort sourcePort, DataPort destPort);

	public abstract DataConnection createConnPlannedImplicit(
			Mockery jmockContext, DataPort sourcePort, DataPort destPort);

	public abstract DataConnection createConnInstalledExplicit(
			Mockery jmockContext, DataPort sourcePort, DataPort destPort);

	public abstract DataConnection createConnInstalledImplicit(
			Mockery jmockContext, DataPort sourcePort, DataPort destPort);

	public abstract DataConnection createConnection(Mockery jmockContext,
			DataPort sourcePort, DataPort destPort, Long typeValueCode,
			Long statusValueCode);

}