package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 * 1. create panel breaker port
 * 2. make connections from panel breaker to branch circuit breaker
 * 3. make connection from panel breaker to PDU Input breaker 
 * @author bunty
 *
 */
public class PowerPanelBreakerPortActionHandler implements PowerChainActionHandler, Validator {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortFactory powerPanelPortFactory;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	public void process(Item item, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			// validate the item
			ValidateObject powerPanelValidateObj = new ValidateObject(item, "PowerChain.notAPowerPanel", supportsItem(item));
			validate(powerPanelValidateObj, errors);
			if (errors.hasErrors()) return;
			
			// Create a panel breaker port
			IPortInfo panelBreakerPort = createPanelBreaker(item, errors);
			if (errors.hasErrors() || null == panelBreakerPort) return;
			
			// make connections from the branch circuit breaker to the panel breaker port
			createBranchCircuitBreakerConnection(item, panelBreakerPort, errors, validateConn, migrationInProgress);
			// return if error exist
			if (errors.hasErrors()) return;
			
			// make connection from panel breaker port to the PDU input breaker port
			createPduBreakerConnection(item, panelBreakerPort, errors, validateConn, migrationInProgress);
			// return if error exist
			if (errors.hasErrors()) return;
			
			itemDAO.update(item);
			// itemDAO.merge(item);
		} finally {
			
			if (null != powerChainErrors) {
				powerChainErrors.addAllErrors(errors);
			}
			else {
				powerChainErrors = errors;
			}
			
		}

	}
	
	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) 
			throws BusinessValidationException {
		
		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		process(item, powerChainErrors, validateConn, migrationInProgress);
		
	}

	@Override
	public boolean supports(Class<?> clazz) {
		
		return MeItem.class.equals(clazz);
	}


	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}
	
	
	private IPortInfo getPduInputBreaker(Item item) {
		if (null == item) {
			return null;
		}
		Set<PowerPort> ports = item.getPowerPorts();
		if (null == ports) {
			return null;
		}
		for (PowerPort port: ports) {
			if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.PDU_INPUT_BREAKER) {
				return port;
			}
		}
		return null;
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
	
	private IPortInfo createPanelBreaker(Item item, Errors errors) {
		
		IPortInfo panelBreakerPort = powerChainActionHandlerHelper.createPort(powerPanelPortFactory, item, SystemLookup.PortSubClass.PANEL_BREAKER, errors);
		if (null == panelBreakerPort) {
			return null;
		}
		
		if (panelBreakerPort.getPortId() != null) {
			itemDAO.update(item);
		}
		
		return panelBreakerPort;
	}
	
	private void createBranchCircuitBreakerConnection(Item item, IPortInfo panelBreakerPort, Errors errors, boolean validateConn, boolean migrationInProgress) {
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, 
				SystemLookup.PortSubClass.PANEL_BREAKER);

		Set<PowerPort> powerPorts = item.getPowerPorts();
		
		for (PowerPort powerPort: powerPorts) {
			if (powerPort.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
				
				powerChainActionHandlerHelper.makeConnections(portConnection, item, powerPort, (PowerPort) panelBreakerPort, errors, validateConn, migrationInProgress);
				
			}
		}
	}
	
	private void createPduBreakerConnection(Item item, IPortInfo panelBreakerPort, Errors errors, boolean validateConn, boolean migrationInProgress) {
		Item pduItem = item.getParentItem();
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PANEL_BREAKER, 
																				SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
		IPortInfo floorPduInputBreakerPort = getPduInputBreaker(pduItem);
		
		powerChainActionHandlerHelper.makeConnections(portConnection, item, (PowerPort)panelBreakerPort, (PowerPort)floorPduInputBreakerPort, errors, validateConn, migrationInProgress);
		
	}

}
