package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * returns the port connection adaptor for a given source and destination port subclass 
 * @author bunty
 *
 */
public class PortConnectionAdaptorFactoryImpl implements
		PortConnectionAdaptorFactory {

	private Map<String, PowerConnectionAdaptor> portConnectionAdaptors;
	
	@Override
	public void setPortConnectionAdaptors(
			Map<String, PowerConnectionAdaptor> portConnectionAdaptorBeans) {
		this.portConnectionAdaptors = portConnectionAdaptorBeans;

	}

	@Override
	public PowerConnectionAdaptor get(Long srcPortSubClass,
			Long destPortSubClass) {
		String key = ((null != srcPortSubClass) ? srcPortSubClass.toString() : "") + ":" + ((null != destPortSubClass) ? destPortSubClass.toString(): "");
		return portConnectionAdaptors.get(key);
	}

}
