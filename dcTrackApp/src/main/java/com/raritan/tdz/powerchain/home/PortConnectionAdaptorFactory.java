package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * returns the port connection handler for a given source and destination port subclass
 * @author bunty
 *
 */
public interface PortConnectionAdaptorFactory {

	/**
	 * Registers an PortAdaptor for a given item unique value and the port subclass.
	 * @param portAdaptorBeans
	 */
	public void setPortConnectionAdaptors( Map<String, PowerConnectionAdaptor> portConnectionAdaptorBeans );
	
	
	/**
	 * gets the PortConnection Object for a given source port subclass and destination port subclass 
	 * @param srcPortSubClass
	 * @param destPortSubClass
	 * @return
	 */
	PowerConnectionAdaptor get(Long srcPortSubClass, Long destPortSubClass);
	
	
}
