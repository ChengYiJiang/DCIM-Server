package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * factory that returns the port connection handler
 * @author bunty
 *
 */
public class PortConnectionFactoryImpl implements PortConnectionFactory {

	/* map key has the srcPortSubClassLkpValueCode:destPortSubClassLkpValueCode */
	private Map<String, PortConnection> portConnections;
	
	@Override
	public void setPortConnections(
			Map<String, PortConnection> portConnectionBeans) {
		this.portConnections = portConnectionBeans;
	}

	@Override
	public PortConnection get(Long srcPortSubClass, Long destPortSubClass) {
		String key = srcPortSubClass.toString() + ":" + ((null != destPortSubClass) ? destPortSubClass.toString() : "");
		return portConnections.get(key);
	}

}
