package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.exception.BusinessValidationException;

/**
 * handles the action from the LN Event
 * @author bunty
 *
 */
public interface PowerChainActionHandler {

	/**
	 * process the event action
	 * @param itemId
	 * @param data1 - can be the port id or the item id of the connecting item
	 * @param data2 - can be the port id or the item id of the connecting item
	 * @param errors TODO
	 * @param migrationInProgress - informs the action handler that migration is in progress
	 */
	public void process(long itemId, String data1, String data2, Errors errors, boolean validateConn, boolean migrationInProgress) throws BusinessValidationException;
	
}
