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

/**
 * update the FPDU input breaker port value
 * @author bunty
 *
 */
public class FloorPduBreakerPortUpdateValueActionHandler implements
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


		Item floorPduItem = itemDAO.getItemWithPortConnections(itemId);
		
		process( floorPduItem, data1, data2, powerChainErrors);
		if( powerChainErrors.hasErrors() ) return;
		itemDAO.update(floorPduItem);
	}
	


	private void process(Item item, String data1, String data2, Errors powerChainErrors)
			throws BusinessValidationException {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
			
		// Validate the floor pdu item
		ValidateObject floorPduValidateObject = new ValidateObject(item, "PowerChain.notAFloorPDU", supportsItem(item));
		validate(floorPduValidateObject, errors);
		if (errors.hasErrors()) return;

		PortAdaptor portAdaptor = portAdaptorFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
		portAdaptor.convert(item, errors);		
					
	}
	

	public void update( Item fpdu, Errors powerChainErrors)
			throws BusinessValidationException {

		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		process(fpdu, null, null, errors);
		powerChainErrors.addAllErrors(errors);					
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}


}
