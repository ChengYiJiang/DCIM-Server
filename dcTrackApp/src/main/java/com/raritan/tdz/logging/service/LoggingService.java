package com.raritan.tdz.logging.service;

import com.raritan.tdz.logging.dto.ClientRuntimeErrorDTO;

/**
 * A service for logging errors that originated from the client.
 * @author Andrew Cohen
 */
public interface LoggingService {

	/**
	 * Logs a runtime error sent from the client.
	 * @param runtimeError
	 */
	public void logRuntimeError(ClientRuntimeErrorDTO runtimeError);
	
}
