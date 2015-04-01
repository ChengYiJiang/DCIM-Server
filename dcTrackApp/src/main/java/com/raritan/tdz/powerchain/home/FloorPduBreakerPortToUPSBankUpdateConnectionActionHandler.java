package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 *  1. Create/Get UPS Bank output port
 *  2. Create/Get new FPDU input breaker port
 * 	2. Create/Update connection from FPDU input breaker port to UPS Bank output breaker port
 * @author bunty
 *
 */
public class FloorPduBreakerPortToUPSBankUpdateConnectionActionHandler implements
		PowerChainActionHandler, Validator {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PortFactory floorPduPortFactory;

	@Autowired(required=true)
	private PortFactory upsBankPortFactory;
	
	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;

	public void process(Item floorPduItem, Item upsBankItem, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) {
		
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {
			
			// Validate the floor pdu item
			ValidateObject floorPduValidateObject = new ValidateObject(floorPduItem, "PowerChain.notAFloorPDU", supportsFloorPduItem(floorPduItem));
			validate(floorPduValidateObject, errors);
			if (errors.hasErrors()) return;
			
			// validate the ups item
			ValidateObject upsBankValidateObject = new ValidateObject(upsBankItem, "PowerChain.notAUPSBank", supportsUpsBankItem(upsBankItem));
			validate(upsBankValidateObject, errors);
			if (errors.hasErrors()) return;
			
			// get/create floor pdu input breaker
			PowerPort floorPduInputBreakerPort = getFloorPduInputBreaker(floorPduItem, errors);
			if (errors.hasErrors() || null == floorPduInputBreakerPort) return; // return if error exist
	
			// delete old ups bank breaker port (custom field 2)
			// PowerPort oldUpsBankOutputBreakerPort = deleteUpsBankBreakerPort(oldOutputBreakerPortId, errors);
			
			// get/create ups bank output port in ups bank
			IPortInfo upsBankOutputBreakerPort = getUpsBankOutputPort(upsBankItem, errors);
			if (null == upsBankOutputBreakerPort) return;
			
			// update connection from fpdu input breaker to ups bank output breaker
			createFpduToUpsBankConnection(floorPduItem, floorPduInputBreakerPort, upsBankOutputBreakerPort, errors, validateConn, migrationInProgress);
			if (errors.hasErrors()) return;
			
			createTerminatingUpsBankConnection(upsBankItem, upsBankOutputBreakerPort, null, errors);
			
			itemDAO.update(floorPduItem);
			itemDAO.update(upsBankItem);
			// if (null != oldUpsBankOutputBreakerPort) {
				// powerPortDAO.delete(oldUpsBankOutputBreakerPort);
			// }
			
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
	public void process(long itemId, String upsItemIdStr, String oldOutputBreakerPortIdStr, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		if (!powerChainActionHandlerHelper.isNumeric(upsItemIdStr)) {
			return;
		}
		long upsBankItemId = (Long.parseLong(upsItemIdStr));
		Item upsBankItem = itemDAO.getItemWithPortConnections(upsBankItemId);
		Item floorPduItem = itemDAO.getItemWithPortConnections(itemId);
		// Long oldOutputBreakerPortId = (null != oldOutputBreakerPortIdStr) ? Long.parseLong(oldOutputBreakerPortIdStr) : 0L;
		
		process(floorPduItem, upsBankItem, powerChainErrors, validateConn, migrationInProgress);

	}

	private boolean supportsFloorPduItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU);
	}
	
	private boolean supportsUpsBankItem(Item item) {
		if (null == item) {
			return false;
		}
		/* item class is FLoor PDU and subclass is null */
		return (item.getClassLookup() != null) &&(item.getSubclassLookup() == null) && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.UPS_BANK);
	}

	
	private PowerPort getFloorPduInputBreaker(Item floorPduItem, Errors errors) {
		PowerPort floorPduInputBreakerPort = (PowerPort) floorPduPortFactory.get(floorPduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);
		return floorPduInputBreakerPort;
	}
	
	@SuppressWarnings("unused")
	private boolean isConnected(PowerPort floorPduInputBreakerPort, Item upsBankItem, Errors errors) {
		// PowerPort floorPduInputBreakerPort = (PowerPort) floorPduPortFactory.get(floorPduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);
		
		// Check if floor pdu input breaker exist
		if (floorPduInputBreakerPort.getPortId() == null) {
			return false;
		}
		
		// check if the floor pdu is connected to the ups bank output port
		Set<PowerConnection> sourcePowerConnections = floorPduInputBreakerPort.getSourcePowerConnections();
		if (null == sourcePowerConnections) {
			return false;
		}
		for (PowerConnection pc: sourcePowerConnections) {
			PowerPort destPort = pc.getDestPowerPort();
			if ( null != destPort && null != destPort.getItem() && 
					destPort.getItem().getItemId() == upsBankItem.getItemId()) {
				Object[] errorArgs = { };
				errors.rejectValue("PowerChain", "PowerChain.floorPduToUpsBankConnectionExist", errorArgs, "Connection already exist between FPDU and UPS Bank");
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
	private PowerPort deleteUpsBankBreakerPort(Long portId, Errors errors) {
		if (portId <= 0) {
			return null;
		}
		
		try {
			PowerPort upsBankOutputBreakerPort = powerPortDAO.loadPort(portId);
			if (null == upsBankOutputBreakerPort) {
				Object[] errorArgs = { };
				errors.rejectValue("PowerChain", "PowerChain.upsBankOutputPortInvalid", errorArgs, "UPS Bank Output Breaker is invalid to delete the old connection from FPDU.");
				return null;
			}
			long portSubClass = upsBankOutputBreakerPort.getPortSubClassLookup().getLkpValueCode().longValue();
			if (portSubClass == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER) {
				return upsBankOutputBreakerPort;
			}
		}
		catch (DataAccessException dae) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", "PowerChain.upsBankOutputPortAbsent", errorArgs, "UPS Bank Output Breaker do not exist to delete the old connection from FPDU.");
		}
		return null;
	}
	
	private IPortInfo getUpsBankOutputPort(Item item, Errors errors) {
		
		IPortInfo upsBankOutputBreakerPort = powerChainActionHandlerHelper.createPort(upsBankPortFactory, item, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, errors);
		
		return upsBankOutputBreakerPort;
	}
	
	private void createFpduToUpsBankConnection(Item floorPduItem, PowerPort floorPduInputBreakerPort, IPortInfo upsBankOutputBreakerPort, Errors errors, boolean validateConn, boolean migrationInProgress) {
		
		// FIXME:: this check will wait till we finish US1897
		/*if (floorPduInputBreakerPort.getAmpsNameplate() > ((PowerPort)upsBankOutputBreakerPort).getAmpsNameplate() ) {
			Object[] errorArgs = { floorPduInputBreakerPort.getPortName(), ((PowerPort)upsBankOutputBreakerPort).getPortName()};
			errors.rejectValue("PowerChain", "PowerChain.floorPduRatingMoreThanUpsBankRating", errorArgs, "Cannot create connection between Floor PDU and UPS Bank because Floor PDU rating is more than UPS Bank rating.");
			return;
		}*/
		
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER, 
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);

		powerChainActionHandlerHelper.makeConnections(portConnection, floorPduItem, floorPduInputBreakerPort, (PowerPort) upsBankOutputBreakerPort, errors, validateConn, migrationInProgress);
		
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject) target, errors);
		
	}

	
}
