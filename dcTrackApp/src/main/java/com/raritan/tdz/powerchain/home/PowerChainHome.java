package com.raritan.tdz.powerchain.home;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * home layer for the power chain
 * @author bunty
 *
 */
public interface PowerChainHome {
	
	/**
	 * process the event from the database
	 * @param event
	 */
	void processLNEvent(LNEvent event) throws BusinessValidationException;
	
	/**
	 * create the power chain for all existing power panels, floor pdus and ups banks
	 * @throws BusinessValidationException
	 */
	void createPowerChainForExistingItems() throws BusinessValidationException;
	
}
