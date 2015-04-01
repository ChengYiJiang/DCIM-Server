package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * Adapts the information in the items and other tables and creates a port
 * @author bunty
 *
 */
public interface PortAdaptorFactory {

	/**
	 * Registers an PortAdaptor for a given item unique value and the port subclass.
	 * @param portAdaptorBeans
	 */
	public void setPortAdaptors( Map<String, PortAdaptor> portAdaptorBeans );
	
	/**
	 * gets the port adaptor for the given item's unique value and port subclass
	 * @param itemUniqueValueId
	 * @param portSubClass
	 * @return
	 */
	PortAdaptor get(Long portSubClass);

}
