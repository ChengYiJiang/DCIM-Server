package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * adapts the values in dct_items_me and item class and creates power panel input breaker port
 * power panel input breaker port if exist, update the value using dct_items_me and item class  
 * @author bunty
 *
 */
public class PowerPanelBreakerPortAdaptor implements PortAdaptor {

	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;

	@Autowired(required=true)
	PortConnectionFactory portConnectionFactory;

	/**
	 * override functions
	 */
	
	// @Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		PowerPort port = portAdaptorHelper.convertSingletonPortForMeItem(item, SystemLookup.PortSubClass.PANEL_BREAKER, "PowerChain.notAPowerPanel", errors);
		
		return update(item, port, errors);
		
	}

	private IPortInfo update(Item item, PowerPort port, Errors errors) {
		
		port = (PowerPort) updateVolt(item, port, errors);
		
		String portName = "Panel Breaker";
		if (null != item.getSubclassLookup() && null != item.getSubclassLookup().getLkpValueCode() &&
				item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
			portName = "Busway Breaker";
		}
			
		port = portAdaptorHelper.updatePortName(port, portName);
		
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
		
		port = portAdaptorHelper.updateVoltUsingLineVolt(item, (PowerPort) port, "PowerChain.notAPowerPanel", errors);
		
		return port;
		
	}

	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		port = portAdaptorHelper.updatePhaseLookup(item, (PowerPort) port, "PowerChain.notAPowerPanel", errors);
				
		return port;
	}
	
	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		
		port = portAdaptorHelper.updateAmpsRatedUsingRatingAmps( item, (PowerPort) port, "PowerChain.notAFloorPDU", errors);
		
		return port;
	}
	

}
