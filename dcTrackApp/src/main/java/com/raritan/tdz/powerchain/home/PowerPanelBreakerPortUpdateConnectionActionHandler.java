package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 * make connections from new branch circuit breaker to panel breaker 
 * for the new poles added to the power panel
 * @author bunty
 *
 */
public class PowerPanelBreakerPortUpdateConnectionActionHandler implements
		PowerChainActionHandler, Validator {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PortFactory powerPanelPortFactory;

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;


	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) 
			throws BusinessValidationException {
		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {

			// validate the item
			ValidateObject powerPanelValidateObj = new ValidateObject(item, "PowerChain.notAPowerPanel", supportsItem(item));
			validate(powerPanelValidateObj, errors);
			if (errors.hasErrors()) return;
			

			// make connections from the branch circuit breaker to the panel breaker port
			createBranchCircuitToPanelBreakerConnections(item, errors, validateConn, migrationInProgress);
			if (errors.hasErrors()) return; // return if error exist
			
			itemDAO.update(item);
		
		} finally {
			
			powerChainErrors.addAllErrors(errors);
			
		}
	}
	
	private void createBranchCircuitToPanelBreakerConnections(Item item, Errors errors, boolean validateConn, boolean migrationInProgress) {
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, 
				SystemLookup.PortSubClass.PANEL_BREAKER);

		Set<PowerPort> powerPorts = item.getPowerPorts();
			
		IPortInfo panelBreakerPort = powerPanelPortFactory.get(item, SystemLookup.PortSubClass.PANEL_BREAKER, errors);
		
		if (errors.hasErrors() || null == panelBreakerPort) return;
			
		for (PowerPort powerPort: powerPorts) {
			if (powerPort.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
				
				powerChainActionHandlerHelper.makeConnections(portConnection, item, powerPort, (PowerPort) panelBreakerPort, errors, validateConn, migrationInProgress);
				
			}
		}
			
		if (errors.hasErrors()) return;
			
	}

	private boolean supportsItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is either 'Local', 'Remote' or 'Busway' */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() != null) && 
					item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) &&
						(item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.LOCAL) || 
								item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.REMOTE) || 
									item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BUSWAY));
	}

	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}
	


}
