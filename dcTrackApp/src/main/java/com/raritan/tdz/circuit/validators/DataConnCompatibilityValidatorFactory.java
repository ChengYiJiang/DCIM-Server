/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;

/**
 * This factory gives the correct DataConnCompatibilityValidator for a given 
 * connection
 * @author Santo Rosario
 *
 */
public interface DataConnCompatibilityValidatorFactory {
	/**
	 * Provides a compatibility validator based on the data connection
	 * @param dataConn
	 * @return
	 */
	public DataConnCompatibilityValidator getCompatibilityValidator(DataConnection dataConn);
	
	/**
	 * Provides a compatibility validator based on the destination port in a connection.
	 * @param destDataPort
	 * @return
	 */
	public DataConnCompatibilityValidator getCompatibilityValidator(DataPort destDataPort);
}
