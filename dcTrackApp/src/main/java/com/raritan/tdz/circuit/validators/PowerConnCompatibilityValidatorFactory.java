/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

/**
 * This factory gives the correct PowerConnCompatibilityValidator for a given 
 * connection
 * @author prasanna
 *
 */
public interface PowerConnCompatibilityValidatorFactory {
	/**
	 * Provides a compatibility validator based on the power connection
	 * @param powerConn
	 * @return
	 */
	public PowerConnCompatibilityValidator getCompatibilityValidator(PowerConnection powerConn);
	
	/**
	 * Provides a compatibility validator based on the destination port in a connection.
	 * @param destPowerPort
	 * @return
	 */
	public PowerConnCompatibilityValidator getCompatibilityValidator(PowerPort destPowerPort);
}
