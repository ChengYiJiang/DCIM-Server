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

/**
 * create FPDU input breaker port
 * @author bunty
 *
 */
public class FloorPduCreateBreakerPortActionHandler implements
		PowerChainActionHandler, Validator {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortFactory floorPduPortFactory;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;

	public void process(Item item, Errors powerChainErrors, boolean validateConn) {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {

			ValidateObject floorPduValidateObj = new ValidateObject(item, "PowerChain.notAFloorPDU", supportsItem(item));
			validate(floorPduValidateObj, errors);
			if (errors.hasErrors()) return;
	
			// Create a panel breaker port
			IPortInfo fpduInputBreakerPort = createFloorPDUInputBreaker(item, errors);
			if (errors.hasErrors() || null == fpduInputBreakerPort) return;
			
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
	
	@Override
	public void process(long itemId, String data1, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		Item item = itemDAO.getItemWithPortConnections(itemId);
		
		process(item, powerChainErrors, validateConn);
		
	}
	
	private IPortInfo createFloorPDUInputBreaker(Item item, Errors errors) {
		
		IPortInfo fpduInputBreakerPort = powerChainActionHandlerHelper.createPort(floorPduPortFactory, item, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);
				
		return fpduInputBreakerPort;
	}
	
	private boolean supportsItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
					item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU);
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
