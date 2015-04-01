package com.raritan.tdz.powerchain.home;

import java.util.Map;

/**
 * returns the port adaptor for a given port subclass
 * @author bunty
 *
 */
public class PortAdaptorFactoryImpl implements PortAdaptorFactory {

	private Map<String, PortAdaptor> portAdaptors;  
	
	@Override
	public void setPortAdaptors(Map<String, PortAdaptor> portAdaptorBeans) {
		this.portAdaptors = portAdaptorBeans;
  
	}

	@Override
	public PortAdaptor get(Long portSubClass) {
		return portAdaptors.get(portSubClass.toString());
		
	}

}
