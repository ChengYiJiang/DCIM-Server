package com.raritan.tdz.powerchain.home;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.home.IPortObject;
import com.raritan.tdz.port.home.IPortObjectFactory;
import com.raritan.tdz.powerchain.validator.PowerConnectionDeleteValidator;
import com.raritan.tdz.powerchain.validator.PowerConnectionValidator;
import com.raritan.tdz.powerchain.validator.ValidateObject;
import com.raritan.tdz.util.GlobalUtils;

/**
 * power chain action handler helper
 * @author bunty
 *
 */
public class PowerChainActionHandlerHelperImpl implements
		PowerChainActionHandlerHelper {

	@Autowired(required=true)
	private IPortObjectFactory portObjectFactory;
	
	@Autowired(required=true)
	private PortAdaptorFactory portAdaptorFactory;
	
	@Autowired(required=true)
	private PowerCircuitHelper powerChainPowerCircuitHelper;
	
	@Autowired
	private PowerConnectionValidator powerChainPowerConnectionValidator;
	
	@Autowired
	private PowerConnectionDeleteValidator powerConnectionDeleteValidator;
	
	@Override
	public MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, MeItem.class.getName() );
		return errors;
		
	}

	@Override
	public void validateSupportedItemClass(boolean supports, String errorCode,
			Errors errors) {
		
		if (!supports) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", errorCode, errorArgs, "Do not support the item class, subclass");
		}

	}
	
	@Override
	public void validateSupportedPortSubClass(boolean supports, String errorCode, Errors errors) {

		if (!supports) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", errorCode, errorArgs, "Do not support the port subclass");
		}

		
	}
	
	@Override
	public void validateItem(Item item, String errorCode, Errors errors) {
		
		if (null == item) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", errorCode, errorArgs, "Item with provided item id does not exist");
		}
	}

	
	@Override
	public boolean isNumeric(String str)
	{
		
		return GlobalUtils.isNumeric(str);
	}
	

	@Override
	public void makeConnections(PortConnection portConnection, Item item, PowerPort srcPort, PowerPort destPort, Errors errors, boolean validateConnectionRatings, boolean migrationInProgress ) {
		IPortInfo oldDestPort = null;
		ICircuitConnection powerConn = null;
		PowerCircuitInfo oldTraceInfo = null;
		PowerCircuitInfo newTraceInfo = null;
		
		if (portConnection.connectionExist(srcPort, errors)) { 
				if(!portConnection.connectionExist(srcPort, destPort, errors)) {
					oldDestPort = portConnection.getDestPort(item, srcPort);
					
					if (!migrationInProgress) {
						oldTraceInfo = powerChainPowerCircuitHelper.getCircuitTraceInfo(srcPort, errors);
					}
					powerConn = portConnection.update(item, srcPort, destPort, errors);
					if (!migrationInProgress) {
						newTraceInfo = powerChainPowerCircuitHelper.getCircuitTraceInfo(srcPort, errors);
						powerChainPowerCircuitHelper.updateCircuitTrace(oldTraceInfo, newTraceInfo);
					}
				}
				else {
					if (!migrationInProgress) {
						powerConn = portConnection.getConnection(srcPort, destPort, errors);
						oldTraceInfo = newTraceInfo = powerChainPowerCircuitHelper.getCircuitTraceInfo(srcPort, errors);
					}
				}
		}
		else {
			powerConn = portConnection.create(srcPort, destPort, errors);
			newTraceInfo = powerChainPowerCircuitHelper.getCircuitTraceInfo(srcPort, errors);
		}

		{
			PortAdaptor portAdaptor = portAdaptorFactory.get(srcPort.getPortSubClassLookup().getLkpValueCode());
			portAdaptor.updateVolt(item, srcPort, errors);
			portAdaptor.updatePhase(item, srcPort, errors);
			portAdaptor.updateAmps(item, srcPort, errors);
		}
		
		if (null != destPort && null != destPort.getPortId()) {
			PortAdaptor portAdaptor = portAdaptorFactory.get(destPort.getPortSubClassLookup().getLkpValueCode());
			portAdaptor.updateUsed(destPort, true, errors);
		}
		
		if (null != oldDestPort && null != oldDestPort.getPortId()) {
			PortAdaptor portAdaptor = portAdaptorFactory.get(oldDestPort.getPortSubClassLookup().getLkpValueCode());
			portAdaptor.updateUsed(oldDestPort, srcPort, errors);
		}
		
		if (!migrationInProgress) {
			// Validate the connection
			validate((PowerConnection) powerConn, oldTraceInfo, newTraceInfo, errors, validateConnectionRatings);
		}
		
	}
	
	private void validate(PowerConnection powerConn, PowerCircuitInfo oldCircuitInfo, PowerCircuitInfo newCircuitInfo, Errors errors, boolean validateConnectionRatings) {
		
		if (null == powerConn) {
			return;
		}

		MapBindingResult powerConnErrors = getErrorObject();

		if (validateConnectionRatings) {
			
			Map<String, Object> targetMap = new HashMap<String, Object>();
			
			targetMap.put(PowerConnection.class.getName(), powerConn);
			targetMap.put(Integer.class.getName(), 0);
			targetMap.put(PowerConnectionValidator.OLD_CIRCUIT_TRACE, oldCircuitInfo);
			targetMap.put(PowerConnectionValidator.NEW_CIRCUIT_TRACE, newCircuitInfo);

			powerChainPowerConnectionValidator.validate(targetMap, powerConnErrors);

			// add all the errors
			errors.addAllErrors(powerConnErrors);
		}
		
	}
	
	
	private void validateConnDelete(PowerConnection powerConn, Errors errors, String errorCode) {
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		targetMap.put(PowerConnection.class.getName(), powerConn);
		
		targetMap.put("errorcode", errorCode);

		powerConnectionDeleteValidator.validate(targetMap, errors);
		
	}
	
	@Override
	public void deleteConnections( PortConnection portConnection, Item item, PowerPort srcPort, Errors errors, String errorCode ) {

		Set<PowerConnection> powerConns = portConnection.getPowerConnection(srcPort);
		if (powerConns != null) {
			for (PowerConnection pc: powerConns) {
				// check if the given connection is part of any circuit(s)
				validateConnDelete(pc, errors, errorCode);
				PowerPort oldDestPort = (PowerPort) pc.getDestPort();  
				PortAdaptor portAdaptor = portAdaptorFactory.get(oldDestPort.getPortSubClassLookup().getLkpValueCode());
				portAdaptor.updateUsed(oldDestPort, srcPort, errors);
			}
		}
		
		portConnection.deleteSource(item, srcPort, errors);
		
	}
	
	@Override
	public IPortInfo createPort(PortFactory portFactory, Item item, Long portSubClass, Errors errors) {
		
		IPortInfo breakerPort = portFactory.get(item, portSubClass, errors);
		if (null == breakerPort) return null;
		
		IPortObject breakerPortObject = portObjectFactory.getPortObject(breakerPort, errors);
		
		breakerPortObject.validateSave(item, errors);
		
		return breakerPort;
		
	}
	

	@Override
	public void validateItem(ValidateObject validateObj, Errors errors) {

		// item should not be null
		validateItem(validateObj.item, "PowerChain.invalidItemId", errors);
		if (errors.hasErrors()) return;

		// item should be a supported
		validateSupportedItemClass(validateObj.supports, validateObj.errorCode, errors);
		if (errors.hasErrors()) return;

		
	}

}
