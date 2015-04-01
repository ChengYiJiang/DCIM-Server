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

public class FPDUBreakerToUPSBankBreakerUpdateActionHandler implements
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


	public void process(Item floorPduItem, Item upsBankItem, Errors errors, boolean validateConn, boolean migrationInProgress) {
		
		ValidateObject floorPduValidateObj = new ValidateObject(floorPduItem, "PowerChain.notAFloorPDU", supportsFloorPduItem(floorPduItem));
		validate(floorPduValidateObj, errors);
		if (errors.hasErrors()) return;
		
		ValidateObject upsBankValidateObj = new ValidateObject(upsBankItem, "PowerChain.notAUPSBank", supportsUpsBankItem(upsBankItem));
		validate(upsBankValidateObj, errors);
		if (errors.hasErrors()) return;
		
		// if FPDU input breaker is currently connected to the same UPS Bank, return
		PowerPort floorPduInputBreakerPort = getFloorPduInputBreaker(floorPduItem, errors);
		if (null == floorPduInputBreakerPort) return;
		
		// if (isConnected(floorPduInputBreakerPort, upsBankItem, errors)) return;
		// if (errors.hasErrors()) return;

		// delete old ups bank breaker port (custom field 2)
		// PowerPort oldUpsBankOutputBreakerPort = deleteUpsBankBreakerPort(oldOutputBreakerPortId, errors);
		
		// create/get ups bank output port in ups bank (custom field 1)
		IPortInfo upsBankOutputBreakerPort = createNewUpsBankOutputPort(upsBankItem, errors);
		if (null == upsBankOutputBreakerPort) return;
		
		// update connection from fpdu input breaker to ups bank output breaker
		createFpduToUpsBankConnection(floorPduItem, floorPduInputBreakerPort, upsBankOutputBreakerPort, errors, validateConn, migrationInProgress);
		
		createTerminatingUpsBankConnection(upsBankItem, upsBankOutputBreakerPort, null, errors);		
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
		
		createBreakersAndConnect( floorPduItem, upsBankItem, powerChainErrors, validateConn, migrationInProgress);
		if( powerChainErrors.hasErrors() ) return;
		itemDAO.update(floorPduItem);
		itemDAO.update(upsBankItem);
	}
	
	public void createBreakersAndConnect( Item floorPduItem, Item upsBankItem, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		process(floorPduItem, upsBankItem, errors, validateConn, migrationInProgress);
		
		if (null != powerChainErrors) {
			powerChainErrors.addAllErrors(errors);
		}
		else {
			powerChainErrors = errors;
		}
		
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
				return true;
			}
		}
		
		return false;
	}
	
	
	private IPortInfo createNewUpsBankOutputPort(Item item, Errors errors) {
		
		IPortInfo upsBankOutputBreakerPort = powerChainActionHandlerHelper.createPort(upsBankPortFactory, item, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, errors);
		
		return upsBankOutputBreakerPort;
	}
	
	private void createFpduToUpsBankConnection(Item floorPduItem, PowerPort floorPduInputBreakerPort, IPortInfo upsBankOutputBreakerPort, Errors errors, boolean validateConn, boolean migrationInProgress) {
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER, 
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);

		// if migration in progress and connection already exist from the pdu breaker port then do not update connections. This existing connection can be from pdu to another pdu
		if (migrationInProgress && portConnection.connectionExist(floorPduInputBreakerPort, errors)) {
			return;
		}
		powerChainActionHandlerHelper.makeConnections(portConnection, floorPduItem, floorPduInputBreakerPort, (PowerPort)upsBankOutputBreakerPort, errors, validateConn, migrationInProgress);
		
	}
	
	private void createTerminatingUpsBankConnection(Item upsBankItem, IPortInfo upsBankOutputBreakerPort, IPortInfo terminatePort, Errors errors) {
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, null);

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

}
