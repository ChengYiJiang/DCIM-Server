package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * adapts the values in dct_items_me and item class and creates ups bank output breaker port
 * @author bunty
 *
 */
public class UpsBankOutputBreakerPortAdaptor implements PortAdaptor {

	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;

	@Autowired(required=true)
	PortConnectionFactory portConnectionFactory;


	/**
	 * public functions
	 */
	
	public IPortInfo convertIndividualOutputBreaker(Item item, Errors errors) {
		
		return portAdaptorHelper.convertUniquePortForMeItem(item, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, "PowerChain.notAUPSBank", errors);
		
	}

	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		PowerPort port = portAdaptorHelper.convertSingletonPortForMeItem(item, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, "PowerChain.notAUPSBank", errors);

		return update(item, port, errors);
		
	}


	private IPortInfo update(Item item, PowerPort port, Errors errors) {
		port = (PowerPort) updateVolt(item, port, errors);
		
		port = (PowerPort) updatePhase(item, port, errors);
		
		port = (PowerPort) updateAmps(item, port, errors);
		
		port = portAdaptorHelper.updatePortName(port, "Bank Breaker");
		
		return port;
	}

	@Override
	public IPortInfo updateUsed(IPortInfo port, IPortInfo oldSrcPort, Errors errors) {
		
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, (PowerPort) oldSrcPort);
		
		return port;
	}
	

	@Override
	public IPortInfo updateUsed(IPortInfo port, boolean value, Errors errors) {
		
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, value);
		
		return port;
	}

	@Override
	public IPortInfo updateVolt(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		port = portAdaptorHelper.updateVoltUsingRatingVolt(item, (PowerPort) port, "PowerChain.notAUPSBank", errors);

		return port;
		
	}

	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		port = portAdaptorHelper.updatePhaseLookup(item, (PowerPort) port, "PowerChain.notAUPSBank", errors);
		
		return port;
		
	}

	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		
		port = portAdaptorHelper.updateAmpsRatedUsingKVA(item, (PowerPort) port, "PowerChain.notAUPSBank", errors);
		
		return port;
		
	}
	

}
