/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.List;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

/**
 * This factory produces enough power validators based on the power connection
 * or the destination port. 
 * @author prasanna
 *
 */
public interface EnoughPowerValidatorFactory {
	
	/**
	 * Given the power connection, based on the destination port gets the 
	 * List of EnoughPowerValidators. If the destination port is not supported
	 * this will return an empty EnoughPowerValidators list
	 * @param powerConn
	 * @return
	 */
	public List<EnoughPowerValidator> getValidators(PowerConnection powerConn);
	
	/**
	 * Given the destination power port of a connection gets the list of 
	 * EnoughPowerValidators. If the power port is not supported this will
	 * return an empty EnoughPowerValidators list.
	 * @param destPort
	 * @return
	 */
	public List<EnoughPowerValidator> getValidators(PowerPort destPort);

}
