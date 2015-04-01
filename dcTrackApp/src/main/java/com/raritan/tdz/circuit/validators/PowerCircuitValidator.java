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
import com.raritan.tdz.circuit.util.Power3Phase;
import com.raritan.tdz.circuit.util.PowerCalc;
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
public class PowerCircuitValidator implements Validator {
	
	@Autowired
	PowerPortDAO powerPortDAO;
	
	@Autowired
	PowerConnCompatibilityValidatorFactory compValidatorFactory;
	
	@Autowired
	EnoughPowerValidatorFactory enoughValidatorFactory;
	
	@Autowired
	PowerCircuitLoopValidator powerCircuitLoopValidator;

	@Autowired
	ItemDAO itemDAO;
	
	@Autowired(required=true)
	PowerCalc powerCalc;
	
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
		PowerPort startPort = getStartPort(connections);
		Long psPortId = startPort != null ? startPort.getPortId() : null;
		PowerPort srcPort = null;
		
		for (PowerConnection conn:powerCircuit.getCircuitConnections()){
			srcPort = powerPortDAO.read(conn.getSourcePortId());
			
			validateUsedPort(conn.getSourcePowerPort(), srcPort, errors);
			validateCompatibility(conn, errors);
		}
		
		validateStartPort(startPort, errors);
		validateEndPort(srcPort, errors);

		if (!errors.hasErrors()){
			powerCircuitLoopValidator.validate(powerCircuit, errors);
			EnoughPowerResult prevResult = null;
			
			PowerWattUsedSummary nodeInfo = getNodeInfo(powerCircuit);
			if (powerCircuit.getStartConnection() == null) { 
				throw new IllegalArgumentException("The circuit connection is not available"); 
			}
			PowerPort p = powerCircuit.getStartConnection().getSourcePowerPort();
			if (p == null) { 
				throw new IllegalArgumentException("Start port of the circuit is not available"); 
			}
			// The circuit.StartConnection() above returns trainsient object, and the port read from 
			// the transient object is not loaded. Therefor using powerport id and load the port again from db.
			p = powerPortDAO.getPortWithSourceConnections(p.getPortId());
			
			// when user is creates/edits partial circuit starting at rpdu input chord, the validation 
			// code needs the 3phase sum at the input chord node.
			if (p != null && p.isInputCord() ) {
				double ampsOnNode = 0.0;
				double wattsOnNode = 0.0;

				Power3Phase objElec = powerCalc.get3PhaseNodeSum(p);
			    if (objElec != null){
			    	ampsOnNode = max(objElec.currentRatedA, objElec.currentRatedB, objElec.currentRatedC);
			    	wattsOnNode = max(objElec.wRatedA, objElec.wRatedB, objElec.wRatedC);			    
			    }

				for (PowerConnection conn:powerCircuit.getCircuitConnections()){
					prevResult = validateEnoughPowerStartingInputChord(psPortId, conn, errors, includeFirstNodeSum, prevResult, nodeInfo, ampsOnNode, wattsOnNode);
					if (errors.hasErrors())
						break;
				}
			} 
			else {
				for (PowerConnection conn:powerCircuit.getCircuitConnections()){
					prevResult = validateEnoughPower(psPortId, conn, errors, includeFirstNodeSum, prevResult, nodeInfo);
					if (errors.hasErrors())
						break;
				}
			}
		}
	}
	
	//------------- private methods -------------------

	private double max(double x, double y, double z){
		return Math.max(Math.max(x, y), Math.max(x, z));
	}
	
	private EnoughPowerResult validateEnoughPowerStartingInputChord(Long psPortId,
			PowerConnection conn, Errors errors, Boolean includeFirstNodeSum, EnoughPowerResult prevResult, 
			PowerWattUsedSummary nodeInfo, Double ampsOnNode, Double wattsOnNode) {
		List<EnoughPowerValidator> enoughPowerValidatorList = enoughValidatorFactory.getValidators(conn);
		Object firstNodeSum = prevResult != null ? prevResult.getParamForNextChain() : null;
		EnoughPowerResult result = prevResult;
		for (EnoughPowerValidator enoughPowerValidator:enoughPowerValidatorList){
			result = enoughPowerValidator.checkEnoughPower(ampsOnNode, wattsOnNode.longValue(), 1.0, conn.getSourcePortId(), conn.getDestPortId(), null, null, -1, firstNodeSum, errors, nodeInfo);
			firstNodeSum = result != null ? result.getParamForNextChain() : null;
		}
		return result;
	}

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
	
	private void validateCompatibility(PowerConnection conn, Errors errors) {
		PowerConnCompatibilityValidator compValidator = compValidatorFactory.getCompatibilityValidator(conn);
		Map<String, Object> targetMap = getTargetMap(conn);
		if (compValidator != null)
			compValidator.validate(targetMap, errors);
	}

	private Map<String, Object> getTargetMap(PowerConnection conn) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), conn);
		targetMap.put(Integer.class.getName(), 0);
		return targetMap;
	}

	private PowerPort getStartPort(List<PowerConnection> connections) {
		PowerPort psPort = null;
		
		PowerConnection firstConnection = connections.get(0);
		if (firstConnection != null){
			PowerPort srcPort = firstConnection.getSourcePowerPort();
			if (srcPort != null){
				srcPort = powerPortDAO.read(srcPort.getPortId());
				psPort = srcPort != null && (srcPort.isPowerSupply() || srcPort.isInputCord()) ? srcPort : null;
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
	
	private Long getPortToExclude(PowerCircuit pc) {
		PowerConnection pwrConn = pc.getStartConnection();
		if ( pwrConn != null) {
			PowerPort startPort = pwrConn.getSourcePowerPort();
			// The circuit.StartConnection() above returns trainsient object, and the port read from 
			// the transient object is not loaded. Therefor using powerport id and load the port again from db.
			startPort = powerPortDAO.getPortWithSourceConnections(startPort.getPortId());

			if (startPort != null && startPort.isInputCord()) {
				return startPort.getPortId();
			}
		}
		return null;
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
		nodeInfo.setNodePortIdToExclude(getPortToExclude(powerCircuit));
		
		return nodeInfo;
	}

	private void validateUsedPort(PowerPort srcPortClient, PowerPort srcPort,
			Errors errors) {
		if (srcPortClient.getUsed() == false && srcPort.getUsed() == true){
			Object errorArgs[] = new Object[] { srcPort.getPortName() };
			errors.reject("powerProc.srcPortUsed", errorArgs, "Port in use");
		}
	}

	private void validateStartPort(PowerPort port, Errors errors) {	
		if(port == null){
			Object errorArgs[] = new Object[] { "N/A" };
			errors.reject("dataProc.badCircuitStart", errorArgs, "Power circuit cannot start with an item of this class.");
		}

		Map<Long, Boolean> mapper = new HashMap<Long, Boolean>();
		mapper.put(SystemLookup.Class.DEVICE, true);
		mapper.put(SystemLookup.Class.NETWORK, true);
		mapper.put(SystemLookup.Class.PROBE, true);
		mapper.put(SystemLookup.Class.RACK_PDU, true);
		
		Long key = port.getItem().getClassLookup().getLkpValueCode();
		
		if(!mapper.containsKey(key)){
			Object errorArgs[] = new Object[] { port.getItem().getClassLookup().getLkpValue() };
			errors.reject("dataProc.badCircuitStart", errorArgs, "Power circuit cannot start with an item of this class.");
		}
	}

	private void validateEndPort(PowerPort port, Errors errors) {	
		if(port == null){
			Object errorArgs[] = new Object[] { "N/A" };
			errors.reject("dataProc.badCircuitEnd", errorArgs, "Power circuit cannot end with an item of this class.");
		}
		
		Map<Long, Boolean> mapper = new HashMap<Long, Boolean>();
		mapper.put(SystemLookup.Class.UPS_BANK, true);
		
		Long key = port.getItem().getClassLookup().getLkpValueCode();
		
		if(!mapper.containsKey(key)){
			Object errorArgs[] = new Object[] { port.getItem().getClassLookup().getLkpValue() };
			errors.reject("dataProc.badCircuitEnd", errorArgs, "Power circuit cannot end with an item of this class.");
		}
	}	

		

}
