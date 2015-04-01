package com.raritan.tdz.powerchain.home;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

/**
 * power connection adaptor that gets ot creates a power connection domain
 * @author bunty
 *
 */
public interface PowerConnectionAdaptor {

	/**
	 * using the source and destination port creates a connection
	 * @param srcPort
	 * @param destPort
	 * @return
	 */
	PowerConnection convert(PowerPort srcPort, PowerPort destPort);

	/**
	 * updates the connection
	 * @param powerConnection
	 * @return
	 */
	PowerConnection update(PowerConnection powerConnection, PowerPort destPort);
	
}
