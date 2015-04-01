package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.validator.ValidateObject;

public class UPSBankBreakerPortUpdateValueActionHandler implements
		PowerChainActionHandler, Validator {
	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortAdaptorFactory portAdaptorFactory;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;


	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {
		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			// Validate the floor pdu item
			ValidateObject upsBankValidateObject = new ValidateObject(item, "PowerChain.notAUPSBank", supportsItem(item));
			validate(upsBankValidateObject, errors);
			if (errors.hasErrors()) return;

			PortAdaptor portAdaptor = portAdaptorFactory.get(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);
			portAdaptor.convert(item, errors);
			
			// return if error exist
			if (errors.hasErrors()) return;
			
			itemDAO.update(item);

		} finally {
		
			powerChainErrors.addAllErrors(errors);
		}
		
	}
	
	private boolean supportsItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.UPS_BANK);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}


}
