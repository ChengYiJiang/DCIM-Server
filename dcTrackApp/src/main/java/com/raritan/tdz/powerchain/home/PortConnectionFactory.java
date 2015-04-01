package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * 
 * @author bunty
 *
 */
public interface PortConnectionFactory {

	/**
	 * Registers an PortAdaptor for a given item unique value and the port subclass.
	 * @param portAdaptorBeans
	 */
	public void setPortConnections( Map<String, PortConnection> portConnectionBeans );
	
	
	/**
	 * gets the PortConnection Object for a given source port subclass and destination port subclass 
	 * @param srcPortSubClass
	 * @param destPortSubClass
	 * @return
	 */
	PortConnection get(Long srcPortSubClass, Long destPortSubClass);
	
}
