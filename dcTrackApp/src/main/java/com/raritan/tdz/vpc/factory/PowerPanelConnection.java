package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.powerchain.home.PowerChainActionHandlerHelper;
import com.raritan.tdz.powerchain.home.PowerPanelBreakerPortActionHandler;

/**
 * update power panel connections from branch circuit breaker to panel breaker,
 * panel breaker to floor pdu breaker
 * @author bunty
 */
public class PowerPanelConnection implements VPCConnection {

	@Autowired(required=true)
	private PowerPanelBreakerPortActionHandler powerPanelBreakerPortActionHandler;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	

	@Override
	public void create(Map<String, List<Item>> vpcItems)
			throws BusinessValidationException {

		List<Item> panels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);

		for (Item powerPanel: panels) {
			
			Errors powerChainErrors = powerChainActionHandlerHelper.getErrorObject();
		
			powerPanelBreakerPortActionHandler.process(powerPanel, powerChainErrors, false, false);
		}

	}


}
