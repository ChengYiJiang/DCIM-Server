package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;

public interface DataConnFactory {

	public abstract DataConnection createConnPlannedExplicit(
			DataPort sourcePort, DataPort destPort, Long statusValueCode);

	public abstract DataConnection createConnPlannedImplicit(
			DataPort sourcePort, DataPort destPort, Long statusValueCode);

	public abstract DataConnection createConnection(DataPort sourcePort,
			DataPort destPort, Long typeValueCode, Long statusValueCode);

	public abstract List<DataConnection> createConnectionUsingPortList(
			List<DataPort> portList, Long statusValueCode);

}