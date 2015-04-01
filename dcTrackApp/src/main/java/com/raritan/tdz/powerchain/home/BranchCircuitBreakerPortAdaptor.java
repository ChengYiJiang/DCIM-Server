/**
 * 
 */
package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;

public class BranchCircuitBreakerPortAdaptor implements PortAdaptor {

	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.powerchain.home.PortAdaptor#convert(com.raritan.tdz.domain.Item, org.springframework.validation.Errors)
	 */
	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		/* this function is not supported for BC Breaker ports */
		assert(true);
		return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.powerchain.home.PortAdaptor#updateUsed(com.raritan.tdz.domain.IPortInfo, com.raritan.tdz.domain.IPortInfo, org.springframework.validation.Errors)
	 */
	@Override
	public IPortInfo updateUsed(IPortInfo port, IPortInfo oldSrcPort, Errors errors) {
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, (PowerPort) oldSrcPort);
		return port;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.powerchain.home.PortAdaptor#updateUsed(com.raritan.tdz.domain.IPortInfo, boolean, org.springframework.validation.Errors)
	 */
	@Override
	public IPortInfo updateUsed(IPortInfo port, boolean value, Errors errors) {
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, value);
		return port;
	}

	@Override
	public IPortInfo updateVolt(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		/* this function is not supported for BC Breaker ports */
		return port;
	}

	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		/* this function is not supported for BC Breaker ports */
		return port;
	}

	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		/* this function is not supported for BC Breaker ports */
		return port;
	}

}
