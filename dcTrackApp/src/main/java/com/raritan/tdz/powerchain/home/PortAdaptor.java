package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

/**
 * This creates a port based on the item and returns the port
 * @author bunty
 *
 */
public interface PortAdaptor {

	/**
	 * adapts the value from the item and creates/updates the port
	 * @param item
	 * @param errors
	 * @param additionalParameters 
	 * @return
	 */
	IPortInfo convert(Item item, Errors errors, Object... additionalParameters);
	
	/**
	 * update the used flag depending on the number of connection to the port
	 * if the oldSrcPort is the current sourcePort, then this connection is not counted when calculating the used flag
	 * @param port
	 * @param oldSrcPort
	 * @param errors
	 * @return
	 */
	IPortInfo updateUsed(IPortInfo port, IPortInfo oldSrcPort, Errors errors);
	
	/**
	 * update used flag with the value provided
	 * @param port
	 * @param value
	 * @param errors
	 * @return
	 */
	IPortInfo updateUsed(IPortInfo port, boolean value, Errors errors);

	/**
	 * update the voltage
	 * @param item
	 * @param port
	 * @param errors
	 * @param additionalParameters 
	 * @return
	 */
	IPortInfo updateVolt(Item item, IPortInfo port, Errors errors, Object... additionalParameters);

	/**
	 * update the phase
	 * @param item
	 * @param port
	 * @param errors
	 * @param additionalParameters
	 * @return
	 */
	IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters);
	
	/**
	 * update the amps
	 * @param item
	 * @param port
	 * @param errors
	 * @return
	 */
	IPortInfo updateAmps(Item item, IPortInfo port, Errors errors);
	
}
