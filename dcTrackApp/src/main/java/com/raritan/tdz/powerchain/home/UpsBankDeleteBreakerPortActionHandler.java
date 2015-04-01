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

public class UpsBankDeleteBreakerPortActionHandler implements
		PowerChainActionHandler, Validator {

	@Autowired(required=true)
	ItemDAO itemDAO;

	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	

	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {
		
		Item upsBankItem = itemDAO.getItemWithPortConnections(itemId);
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			// validate the item
			ValidateObject upsBankValidateObj = new ValidateObject(upsBankItem, "PowerChain.notAUPSBank", supportsUpsBankItem(upsBankItem));
			validate(upsBankValidateObj, errors);
			if (errors.hasErrors()) return;

			// delete UPS Bank connections and power ports
			upsBankItem.getPowerPorts().clear();
			
			// delete UPS Bank connections
			// deleteUpsBankConnections(upsBankItem);
			
			// delete ups bank ports
			// deleteUpsBankPorts(upsBankItem);
			
			// delete ups bank
			// itemDAO.delete(upsBankItem);
			
			// update the item
			itemDAO.update(upsBankItem);
		
		} finally{

			powerChainErrors.addAllErrors(errors);
			
		}
	}

	@SuppressWarnings("unused")
	private void deleteUpsBankConnections(Item upsBankItem) {
		upsBankItem.getPowerPorts().clear();
	}
	
	private boolean supportsUpsBankItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.UPS_BANK);
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
