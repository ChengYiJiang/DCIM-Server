package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * adapts the values in dct_items_me and item class and creates fpdu input breaker port
 * fpdu input breaker port if exist, update the value using dct_items_me and item class  
 * @author bunty
 *
 */
public class FloorPduInputBreakerPortAdaptor implements PortAdaptor {

	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;
	
	@Autowired(required=true)
	PortConnectionFactory portConnectionFactory;
	
	/**
	 * public functions
	 */
	
	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		PowerPort port = portAdaptorHelper.convertSingletonPortForMeItem(item, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, "PowerChain.notAFloorPDU", errors);
		
		return update( item, port, errors);		
	}

	private IPortInfo update(Item item, PowerPort port, Errors errors){
		
		port = (PowerPort) updateVolt(item, port, errors);

		port = portAdaptorHelper.updatePortName(port, "PDU Breaker");
		
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
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER,
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);
		
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
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER,
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);
		
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
		
		port = portAdaptorHelper.updateAmpsRatedUsingRatingAmps( item, (PowerPort) port, "PowerChain.notAFloorPDU", errors);
		
		return port;
	}
	
	


}
