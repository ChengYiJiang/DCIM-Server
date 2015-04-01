package com.raritan.tdz.powerchain.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.validator.ValidateObject;

public class FloorPduDeleteConnectionActionHandler implements
		PowerChainActionHandler, Validator {

	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PortFactory floorPduPortFactory;

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	
	public void process(Item floorPduItem, Errors errors) {
		
		ValidateObject floorPduValidateObj = new ValidateObject(floorPduItem, "PowerChain.notAFloorPDU", supportsFloorPduItem(floorPduItem));
		validate(floorPduValidateObj, errors);
		if (errors.hasErrors()) return;
		
		// if FPDU input breaker is currently connected to the same UPS Bank, return
		PowerPort floorPduInputBreakerPort = getFloorPduInputBreaker(floorPduItem, errors);
		if (errors.hasErrors() || null == floorPduInputBreakerPort) return;

		// delete the connection from the floor pdu input breaker to the ups bank breaker
		deleteFpduConnection(floorPduItem, floorPduInputBreakerPort, errors);
		if (errors.hasErrors()) return;
		
	}

	
	@Override
	public void process(long itemId, String prevUpsItemIdStr, String data2, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		Item floorPduItem = itemDAO.getItemWithPortConnections(itemId);
		

		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			process(floorPduItem, errors);
			if (errors.hasErrors()) return;
			
			
			itemDAO.update(floorPduItem);
			
		} finally {
			
			powerChainErrors.addAllErrors(errors);
			
		}
		

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

	private boolean supportsFloorPduItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU);
	}
	

	private PowerPort getFloorPduInputBreaker(Item floorPduItem, Errors errors) {
		PowerPort floorPduInputBreakerPort = (PowerPort) floorPduPortFactory.get(floorPduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);
		return floorPduInputBreakerPort;
	}


	private void deleteFpduConnection(Item floorPduItem, PowerPort floorPduInputBreakerPort, Errors errors) {
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER, 
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);

		powerChainActionHandlerHelper.deleteConnections(portConnection, floorPduItem, floorPduInputBreakerPort, errors, "PowerChain.cannotDeleteConnectionWithCircuits");
		
	}
	
	
}
