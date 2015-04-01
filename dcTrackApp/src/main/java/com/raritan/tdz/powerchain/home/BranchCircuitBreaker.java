package com.raritan.tdz.powerchain.home;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

public interface BranchCircuitBreaker {

	/**
	 * create branch circuit breaker port
	 * @param item
	 * @param phaseLkpValueCode
	 * @param startPoleNumber
	 * @param maxCurrent
	 * @return TODO
	 */
	public IPortInfo create(Item item, Long phaseLkpValueCode, Long startPoleNumber, Long maxCurrent); 
	
}
