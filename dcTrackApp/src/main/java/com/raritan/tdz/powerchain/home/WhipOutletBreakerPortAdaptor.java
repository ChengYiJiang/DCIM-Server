package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * whip power outlet port adaptor
 * @author bunty
 *
 */
public class WhipOutletBreakerPortAdaptor implements PortAdaptor {

	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;
	
	@Autowired(required=true)
	PortConnectionFactory portConnectionFactory;
	
	/**
	 * public functions
	 */
	
	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		PowerPort port = portAdaptorHelper.convertUniquePortForMeItem(item, SystemLookup.PortSubClass.WHIP_OUTLET, "PowerChain.notAWhipOutlet", errors);
		
		return update( item, port, errors);	
	}

	private IPortInfo update(Item item, PowerPort port, Errors errors){
		
		port = (PowerPort) updateVolt(item, port, errors);

		port = portAdaptorHelper.updatePortName(port, "R");
		
		port = (PowerPort) updatePhase(item, port, errors);
		
		port = (PowerPort) updateAmps(item, port, errors); // portAdaptorHelper.updateAmpsRatedUsingRatingAmps( item, port, "PowerChain.notAFloorPDU", errors);
		
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
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.WHIP_OUTLET,
				SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);
		
		if (portConnection.connectionExist(port, errors)) {
			port = portAdaptorHelper.updateVoltUsingDestPort((PowerPort)port, errors);
		}
		else {
			port = portAdaptorHelper.updateVoltUsingLineVolt(item, (PowerPort) port, "PowerChain.notAFloorPDU", errors);
		}
		
		return port;
	}
	
	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.WHIP_OUTLET,
				SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);
		
		if (portConnection.connectionExist(port, errors)) {
			port = portAdaptorHelper.updatePhaseUsingDestPort((PowerPort)port, errors);
		}
		else {
			port = portAdaptorHelper.updatePhaseLookup(item, (PowerPort) port, "PowerChain.notAFloorPDU", errors);
		}
		
		return port;
	}

	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.WHIP_OUTLET,
				SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);
		
		if (portConnection.connectionExist(port, errors)) {
			port = portAdaptorHelper.updateAmpsRatedUsingDestPort((PowerPort) port, errors);
		}
		else {
			port = portAdaptorHelper.updateAmpsRatedUsingRatingAmps( item, (PowerPort) port, "PowerChain.notAWhipOutlet", errors);
		}
		
		return port;
	}

}
