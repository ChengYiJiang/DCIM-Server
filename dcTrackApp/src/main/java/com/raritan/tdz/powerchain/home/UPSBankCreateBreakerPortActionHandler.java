package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.validator.ValidateObject;

public class UPSBankCreateBreakerPortActionHandler implements Validator,
		PowerChainActionHandler {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PortFactory upsBankPortFactory;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;

	
	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {

			ValidateObject floorPduValidateObj = new ValidateObject(item, "PowerChain.notAUPSBank", supportsItem(item));
			validate(floorPduValidateObj, errors);
			if (errors.hasErrors()) return;
	
			// Create a panel breaker port
			IPortInfo bankBreakerPort = createBankBreaker(item, errors);
			if (errors.hasErrors() || null == bankBreakerPort) return;
			
			createTerminatingUpsBankConnection(item, bankBreakerPort, null, errors);
			if (errors.hasErrors() || null == bankBreakerPort) return;
			
			itemDAO.update(item);
		}
		finally {
			
			if (null != powerChainErrors) {
				powerChainErrors.addAllErrors(errors);
			}
			else {
				powerChainErrors = errors;
			}
		}
	}
	
	private IPortInfo createBankBreaker(Item item, Errors errors) {
		
		IPortInfo upsBankBreakerPort = powerChainActionHandlerHelper.createPort(upsBankPortFactory, item, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, errors);
		
		return upsBankBreakerPort;
	}
	
	private boolean supportsItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
					item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.UPS_BANK);
	}
	
	private void createTerminatingUpsBankConnection(Item upsBankItem, IPortInfo upsBankOutputBreakerPort, IPortInfo terminatePort, Errors errors) {
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 
				null);

		if (! portConnection.connectionExist(upsBankOutputBreakerPort, errors)) {
			portConnection.create(upsBankOutputBreakerPort, null, errors);
		}
		
	}



	@Override
	public boolean supports(Class<?> clazz) {
		
		return MeItem.class.equals(clazz);
		
	}

	
	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}



}
