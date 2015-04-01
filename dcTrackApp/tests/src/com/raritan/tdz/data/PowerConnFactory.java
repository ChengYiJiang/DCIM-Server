package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

public interface PowerConnFactory {

	public abstract PowerConnection createConnExplicit(
			PowerPort sourcePort, PowerPort destPort, Long statusValueCode);

	public abstract PowerConnection createConnImplicit(
			PowerPort sourcePort, PowerPort destPort, Long statusValueCode);

	public abstract PowerConnection createConnection(PowerPort sourcePort,
			PowerPort destPort, Long typeValueCode, Long statusValueCode);

	public abstract List<PowerConnection> createConnectionUsingPortList(
			List<PowerPort> portList, Long statusValueCode);

}