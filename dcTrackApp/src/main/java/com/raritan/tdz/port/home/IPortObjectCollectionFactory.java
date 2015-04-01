package com.raritan.tdz.port.home;

import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

public interface IPortObjectCollectionFactory {
	/**
	 * Registers a PortObjects class for each port ui id.
	 * @param portObjectsBeans
	 */
	public void setPortsIds( Map<String, IPortObjectCollection> portObjectsBeans);
	
	
	/**
	 * Creates an PortObjects business wrapper for the specified port uiId ("tabPowerPorts" "tabDataPorts" "tabSensorPorts")
	 * 
	 * @param item
	 * @param ports
	 * @param errors
	 * @return
	 */
	public IPortObjectCollection getPortObjects(String uiPortId, Set<IPortInfo> ports, Errors errors);
	
	/**
	 * Creates an PortObjects business wrapper for the specified port uiId ("tabPowerPorts" "tabDataPorts" "tabSensorPorts")
	 * 
	 * @param item
	 * @param ports
	 * @param errors
	 * @return
	 */
	public IPortObjectCollection getPortObjects(Long classMountingFormFactorValue, String uiPortId, Item item, Errors errors);
	
	/**
	 * Creates an PortObjects business wrapper for the specified port uiId ("tabPowerPorts" "tabDataPorts" "tabSensorPorts")
	 * 
	 * @param item
	 * @param ports
	 * @return
	 */
	public IPortObjectCollection getPortObjects(String uiPortId, Set<IPortInfo> ports);
	
	/**
	 * Creates an PortObjects business wrapper for the specified port uiId ("tabPowerPorts" "tabDataPorts" "tabSensorPorts")
	 * 
	 * @param item
	 * @param ports
	 * @return
	 */
	public IPortObjectCollection getPortObjects(Long classMountingFormFactorValue, String uiPortId, Item item);
	

}
