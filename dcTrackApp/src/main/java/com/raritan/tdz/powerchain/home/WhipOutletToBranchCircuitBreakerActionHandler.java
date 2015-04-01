package com.raritan.tdz.powerchain.home;

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
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 * create port and/or connections between power outlet port and the panel's branch circuit breaker
 * @author bunty
 *
 */
public class WhipOutletToBranchCircuitBreakerActionHandler implements PowerChainActionHandler, Validator	{

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortFactory whipOutletPortFactory;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		
		return MeItem.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);

	}

	/**
	 * itemId - power outlet item id
	 * data1 - branch circuit breaker port id of the power panel
	 */
	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, 	boolean validateConn, boolean migrationInProgress) throws BusinessValidationException {

		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		Long branchCircuitBreakerId = new Long(data1);
		IPortInfo branchCircuitBreaker = powerPortDAO.getPortWithConnections(branchCircuitBreakerId);
		
		process(item, branchCircuitBreaker, powerChainErrors, validateConn, migrationInProgress);
		
		itemDAO.update(item);
	}
	
	/**
	 * create a new port in the power outlet
	 * make connection to the branch circuit breaker port
	 * update the power outlet port with the phase and voltage of the branch circuit breaker port
	 * @param item
	 * @param branchCircuitBreaker
	 * @param powerChainErrors
	 * @param validateConn
	 * @param migrationInProgress
	 * @return new power outlet port
	 */
	public PowerPort process(Item item, IPortInfo branchCircuitBreaker, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			// validate the item
			ValidateObject powerPanelValidateObj = new ValidateObject(item, "PowerChain.notAPowerOutlet", supportsItem(item));
			validate(powerPanelValidateObj, errors);
			if (errors.hasErrors()) return null;
			
			if (null == branchCircuitBreaker) return null;
			
			// Create a power outlet breaker port
			IPortInfo whipOutletBreakerPort = createPowerOutletBreaker(item, errors);
			if (errors.hasErrors() || null == whipOutletBreakerPort) return null;
			
			// make connections from the power outlet breaker port to branch circuit breaker
			createWhipOutletBreakerToBranchCircuitBreakerConnections(item, whipOutletBreakerPort, branchCircuitBreaker, errors, validateConn, migrationInProgress);
			// return if error exist
			if (errors.hasErrors()) return (PowerPort) whipOutletBreakerPort;
			
			// set the breaker port id for the power outlet port
			((PowerPort)whipOutletBreakerPort).setBreakerPort((PowerPort)branchCircuitBreaker);
			
			// set the address of the power outlet port
			String address = "Circuit " + branchCircuitBreaker.getPortName() + " on " + ((MeItem) item).getPduPanelItem().getParentItem().getItemName() + "/" + ((MeItem) item).getPduPanelItem().getItemName(); 
			((PowerPort)whipOutletBreakerPort).setAddress(address);
			
			// itemDAO.update(item);
			// itemDAO.merge(item);
			
			return (PowerPort) whipOutletBreakerPort;
			
		} finally {
			
			if (null != powerChainErrors) {
				powerChainErrors.addAllErrors(errors);
			}
			else {
				powerChainErrors = errors;
			}
			
		}

	}
	
	private boolean supportsItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is Power Outlet */
		return (item.getClassLookup() != null && 
					item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_OUTLET) );
	}


	public IPortInfo createPowerOutletBreaker(Item item, Errors errors) {
		
		IPortInfo whipBreakerPort = powerChainActionHandlerHelper.createPort(whipOutletPortFactory, item, SystemLookup.PortSubClass.WHIP_OUTLET, errors);
		if (null == whipBreakerPort) {
			return null;
		}
		
		if (whipBreakerPort.getPortId() != null) {
			itemDAO.update(item);
		}
		
		return whipBreakerPort;
	}
	
	private void createWhipOutletBreakerToBranchCircuitBreakerConnections(Item item, IPortInfo whipBreaker, IPortInfo branchCircuitBreaker, Errors errors, boolean validateConn, boolean migrationInProgress) {
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.WHIP_OUTLET, 
				SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);

		if (errors.hasErrors() || null == whipBreaker || null == branchCircuitBreaker) return;
			
		if (whipBreaker.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.WHIP_OUTLET && 
				branchCircuitBreaker.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) {
				
				powerChainActionHandlerHelper.makeConnections(portConnection, item, (PowerPort) whipBreaker, (PowerPort) branchCircuitBreaker, errors, validateConn, migrationInProgress);
				
		}
			
		if (errors.hasErrors()) return;
			
	}


}
