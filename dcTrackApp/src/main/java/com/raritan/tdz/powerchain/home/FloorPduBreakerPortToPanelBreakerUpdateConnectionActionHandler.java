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
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.validator.ValidateObject;

/**
 * 1. create / get the FPDU input breaker port
 * 2. get the branch circuit breaker port
 * 3. make connection from FPDU i/p breaker port to branch circuit breaker port
 * @author bunty
 *
 */
public class FloorPduBreakerPortToPanelBreakerUpdateConnectionActionHandler  implements
				PowerChainActionHandler, Validator  {

	@Autowired(required=true)
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortConnectionFactory portConnectionFactory;

	@Autowired(required=true)
	private PortFactory floorPduPortFactory;

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;

	@Override
	public void process(long itemId, String destPPBreakerPortIdStr, String oldOutputBreakerPortIdStr, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress)
			throws BusinessValidationException {

		if (!powerChainActionHandlerHelper.isNumeric(destPPBreakerPortIdStr)) {
			return;
		}

		MeItem srcFloorPduItem = (MeItem)itemDAO.getItemWithPortConnections(itemId);
		
		Long destPPBreakerPortId = (Long.parseLong(destPPBreakerPortIdStr));
		
		process(srcFloorPduItem, destPPBreakerPortId, powerChainErrors, validateConn, migrationInProgress);
		
		itemDAO.update(srcFloorPduItem);

	}
	
	public void process(MeItem srcFloorPduItem, Long destPPBreakerPortId, Errors powerChainErrors, boolean validateConn, boolean migrationInProgress) {
		MapBindingResult errors = powerChainActionHandlerHelper.getErrorObject();
		
		try {

			// Validate the authenticity of the item
			ValidateObject validateObj = new ValidateObject(srcFloorPduItem, "PowerChain.notAFloorPDU", supportsFloorPduItem(srcFloorPduItem));
			validate(validateObj, errors);
			if (errors.hasErrors()) return;
	
			// get floor pdu input breaker port
			PowerPort floorPduBreakerPort = getFloorPduInputBreaker(srcFloorPduItem, errors);
			if (errors.hasErrors() || null == floorPduBreakerPort) return;
	
			// get Power Panel's circuit breaker port
			PowerPort circuitBreakerPort = getPPCircuitBreakerPort(destPPBreakerPortId, errors);
			if (errors.hasErrors()) return;
	
			// create/update connection from fpdu input breaker to power panel circuit breaker port
			makeFpduToPPCircuitBreakerPortConnection(srcFloorPduItem, floorPduBreakerPort, circuitBreakerPort, errors, validateConn, migrationInProgress);
			
			if (errors.hasErrors()) return;
		
		} finally {
			powerChainErrors.addAllErrors(errors);
			
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
	

	private PowerPort getFloorPduInputBreaker(Item floorPduItem, Errors errors) {
		PowerPort floorPduInputBreakerPort = (PowerPort) floorPduPortFactory.get(floorPduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, errors);
		return floorPduInputBreakerPort;
	}

	
	private void makeFpduToPPCircuitBreakerPortConnection(Item floorPduItem, PowerPort floorPduInputBreakerPort, IPortInfo circuitBreakerPort, Errors errors, boolean validateConn, boolean migrationInProgress) {
		PortConnection portConnection = portConnectionFactory.get(SystemLookup.PortSubClass.PDU_INPUT_BREAKER,
				SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER 
				);

		powerChainActionHandlerHelper.makeConnections(portConnection, floorPduItem, floorPduInputBreakerPort, (PowerPort)circuitBreakerPort, errors, validateConn, migrationInProgress);
				
	}


	private PowerPort getPPCircuitBreakerPort(Long portId, Errors errors) {
		PowerPort port;
		try {
			port = powerPortDAO.loadPort(portId);
		} catch (DataAccessException e) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", "PowerChain.circuitBreakerPortAbsent", errorArgs, "Panel Circuit Breaker Port is not available to make connection to the floor pdu input port.");
			return null;
		}
		
		return port;
	}


	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void validate(Object target, Errors errors) {

		powerChainActionHandlerHelper.validateItem((ValidateObject)target, errors);
		
	}
	


}
