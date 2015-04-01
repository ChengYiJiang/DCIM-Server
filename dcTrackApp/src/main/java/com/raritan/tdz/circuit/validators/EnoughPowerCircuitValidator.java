/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.validators.EnoughPowerResult.PowerChainResultHandler;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class EnoughPowerCircuitValidator implements Validator {
	@Autowired
	PowerPortDAO powerPortDAO;
	
	@Autowired
	EnoughPowerValidatorFactory enoughValidatorFactory;
	
	@Autowired
	ItemDAO itemDAO;

	private boolean targetIsMap = false;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(PowerCircuit.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//Validate args
		validateArgs(target,errors);
		
		PowerCircuit powerCircuit;
		Boolean includeFirstNodeSum = false;
		
		if (targetIsMap){
			Map<String,Object> targetMap = (Map<String,Object>) target;
			powerCircuit = (PowerCircuit)targetMap.get(PowerCircuit.class.getName());
			includeFirstNodeSum = (Boolean)targetMap.get(Boolean.class.getName());
		} else {
			powerCircuit = (PowerCircuit)target;
		}
		
		
		
		List<PowerConnection> connections = powerCircuit.getCircuitConnections();
		
		if (connections == null || connections.size() == 0){
			errors.reject("powerProc.noConnectionsFound");
			return;
		}
		
	
		//Capture the power supply port
		PowerPort psPort = getPsPort(connections);
		Long psPortId = psPort != null ? psPort.getPortId():null;
		
		PowerWattUsedSummary nodeInfo = getNodeInfo(powerCircuit);
		
		EnoughPowerResult prevResult = null;
		for (PowerConnection conn:powerCircuit.getCircuitConnections()){
			prevResult = validateEnoughPower(psPortId, conn, errors, includeFirstNodeSum, prevResult, nodeInfo);
			if (errors.hasErrors())
				break;
		}
	}
	
	//------------- private methods -------------------
	private EnoughPowerResult validateEnoughPower(Long psPortId,
			PowerConnection conn, Errors errors, Boolean includeFirstNodeSum, EnoughPowerResult prevResult, PowerWattUsedSummary nodeInfo) {
		List<EnoughPowerValidator> enoughPowerValidatorList = enoughValidatorFactory.getValidators(conn);
		Object firstNodeSum = prevResult != null ? prevResult.getParamForNextChain() : null;
		EnoughPowerResult result = prevResult;
		for (EnoughPowerValidator enoughPowerValidator:enoughPowerValidatorList){
			result = enoughPowerValidator.checkEnoughPower(psPortId, psPortId, conn, includeFirstNodeSum, firstNodeSum, errors, nodeInfo);
			firstNodeSum = result != null ? result.getParamForNextChain() : null;
		}
		
		return result;
	}


	private PowerPort getPsPort(List<PowerConnection> connections) {
		PowerPort psPort = null;
		
		PowerConnection firstConnection = connections.get(0);
		if (firstConnection != null){
			PowerPort srcPort = firstConnection.getSourcePowerPort();
			if (srcPort != null && srcPort.getPortId() != null && srcPort.getPortId() > 0){
				srcPort = powerPortDAO.read(srcPort.getPortId());
				psPort = srcPort != null && srcPort.isPowerSupply() ? srcPort : null;
			} else {
				psPort = srcPort != null && srcPort.isPowerSupply() ? srcPort : null;
			}
		}
		return psPort;
	}

	private void validateArgs(Object target, Errors errors) {
		
		targetIsMap = false;
		if (target != null && target instanceof Map){
			targetIsMap = true;
			
			Map<String,Object> targetMap = (Map<String,Object>)target;
			if (targetMap.size() < 2){
				throw new IllegalArgumentException("You must provide a valid map that contains a power circuit and a bool to indicate the source port connection should be included in calculation");
			}
			
			if (targetMap.get(PowerCircuit.class.getName()) == null){
				throw new IllegalArgumentException("You must provide the power circuit to validate");
			}
			
			if (targetMap.get(Boolean.class.getName()) == null){
				throw new IllegalArgumentException("You must provide the boolean to indicate if you need to include first port sum in the calculation. It cannot be null");
			}
		}
		
		if (target == null || ( !targetIsMap  && !(target instanceof PowerCircuit))){
			throw new IllegalArgumentException("You must provide a valid power circuit");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
	}

	private PowerWattUsedSummary getNodeInfo(PowerCircuit powerCircuit){
		PowerWattUsedSummary nodeInfo = new PowerWattUsedSummary();
		
		for (PowerConnection conn:powerCircuit.getCircuitConnections()){
			PowerPort port =   powerPortDAO.read(conn.getSourcePowerPort().getPortId());
			LksData phaseLeg = port.getPhaseLegsLookup();
			
			if(phaseLeg == null){
				continue;
			}
			
			if(nodeInfo.getLegs() == null){
				nodeInfo.setLegs(phaseLeg.getAttribute());
			}
			
			long subclass = port.getPortSubClassLookup().getLkpValueCode();
			
			if(subclass == SystemLookup.PortSubClass.PANEL_BREAKER){
				MeItem item = (MeItem) itemDAO.loadItem(port.getItem().getItemId());
				nodeInfo.setPbVolts(item.getLineVolts());				
				nodeInfo.setPbPhaseVolts(item.getPhaseVolts());
				break;
			}
			
		}
		
		return nodeInfo;
	}


}
