package com.raritan.tdz.powerchain.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
//import org.apache.solr.common.util.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.validators.EnoughPowerCircuitValidator;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.powerchain.home.PowerCircuitInfo;

public class PowerChainLoadValidator implements Validator {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired(required=true)
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	private PowerConnDAO powerConnDAO;

	@Autowired(required=true)
	private EnoughPowerCircuitValidator powerCircuitValidator;

	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Transactional(readOnly=true,propagation=Propagation.REQUIRES_NEW)
	@Override
	public void validate(Object target, Errors errors) {

		validateArgs(target);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		// Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		PowerCircuitInfo newCircuitTrace = (PowerCircuitInfo)targetMap.get(PowerConnectionValidator.NEW_CIRCUIT_TRACE);
		PowerCircuitInfo oldCircuitTrace = (PowerCircuitInfo)targetMap.get(PowerConnectionValidator.OLD_CIRCUIT_TRACE);
		
		validatePortLoadLimit(powerConn, errors, oldCircuitTrace, newCircuitTrace);
		
	}
	
	
	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		
		if (powerConn == null) throw new IllegalArgumentException("You must provide a power connection target");
		
		Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		if (nodeNumber == null) throw new IllegalArgumentException("You must provide a node number for this target");
		
		PowerCircuitInfo newCircuitInfo = (PowerCircuitInfo)targetMap.get(PowerConnectionValidator.NEW_CIRCUIT_TRACE);
		
		if (newCircuitInfo == null) throw new IllegalArgumentException("You must provide the new circuitInfo");
	}

	private long getLoad(PowerPort port) {
		if (null == port.getPortId()) {
			return 0;
		}
		return powerCircuitDAO.getPowerWattUsedTotal(port.getPortId(), null);
	}

	private void validatePortLoadLimit(PowerConnection powerConn, Errors errors,
			PowerCircuitInfo oldCircuitTrace, PowerCircuitInfo newCircuitTrace) {
		
		PowerPort srcPort = powerConn.getSourcePowerPort();
		// get the current load at the port
		double srcLoad = getLoad(srcPort);
		
		// Skip the load/limit validation if the floor pdu is not loaded 
		if (0 == srcLoad) return;
		
		//Dont perform validation of the circuit trace if there is no difference between the old and new trace
		if (compareCircuitTrace(oldCircuitTrace, newCircuitTrace)) return;
		
		//If there is already an insufficient power on the port being connected, then there is no need to go further
		//up the chain.
		if (errors.hasErrors()){
			for (ObjectError error : errors.getAllErrors()){
				if (error.getCode().equals("powerProc.insufficientPowerAtPort") || error.getCode().equals("powerProc.insufficientAmpsAtPort")) return;
			}
		}
			
		Map<String,Object> cirValidationMap = new HashMap<String, Object>();
		cirValidationMap.put(PowerCircuit.class.getName(), getValidationCircuit(oldCircuitTrace, newCircuitTrace, powerConn));
		cirValidationMap.put(Boolean.class.getName(),true);
		powerCircuitValidator.validate(cirValidationMap, errors);
	}
	
	private PowerCircuit getValidationCircuit(PowerCircuitInfo oldCircuitTrace, PowerCircuitInfo newCircuitTrace, PowerConnection powerConn){
		
		PowerCircuit circuit = null;
		String circuitTrace = newCircuitTrace.getCircuitTrace();
		
		
		if (oldCircuitTrace != null){
			//First Compare the old circuit trace and new circuit trace
			if (!compareCircuitTrace(oldCircuitTrace, newCircuitTrace)){
				// String firstNode = getFirstNode(newCircuitTrace.getCircuitTrace()); // This is not used remove.
				String oldTraceWithoutFirstNode = getTraceWithoutFirstNode(oldCircuitTrace.getCircuitTrace());
				String newTraceWithoutFirstNode = getTraceWithoutFirstNode(newCircuitTrace.getCircuitTrace());
				circuitTrace = strDiffChop(oldTraceWithoutFirstNode, newTraceWithoutFirstNode);
				if (circuitTrace == null) 
					circuitTrace = "," + newTraceWithoutFirstNode;
			} else {
				circuitTrace = "," +  getTraceWithoutFirstNode(newCircuitTrace.getCircuitTrace());
			}
		}
		
		circuit = new PowerCircuit();
		circuit.setCircuitTrace(circuitTrace);
		List<Long> connectionIds = circuit.getConnListFromTrace();
		if (connectionIds != null && connectionIds.size() > 0){
			List<PowerConnection> connectionList = new ArrayList<PowerConnection>();
			connectionList.add(powerConn);
			for (Long connectionId:connectionIds){
				connectionList.add(powerConnDAO.read(connectionId));
			}
			circuit.setCircuitConnections(connectionList);
		}
		
		//Then try to create the circuit trace based on the difference.
		return circuit;
	}
	
	public String getTraceWithoutFirstNode(String trace){
		String traceWithoutFirstNode = null;
		
		traceWithoutFirstNode = trace.substring(trace.indexOf(",", 1) + 1);
		
		return traceWithoutFirstNode;
	}
	
	public String getFirstNode(String trace){
		String firstNode = null;
		
		firstNode = trace.substring(0,trace.indexOf(",", 1) + 1);
		
		return firstNode;
	}
	
	public String strDiffChop(String s1, String s2) {
		if (s1.contains(s2)){
			return s1.substring(0,s1.indexOf(s2) - 1);
		} else if (s2.contains(s1)){
			return s2.substring(0,s2.indexOf(s1) - 1);
		}
		
		return null;
	}
	
	private boolean compareCircuitTrace(PowerCircuitInfo oldCircuitTrace, PowerCircuitInfo newCircuitTrace){
		
		String oldTrace = (null != oldCircuitTrace) ? oldCircuitTrace.getCircuitTrace() : "";
		String newTrace = newCircuitTrace.getCircuitTrace();
		
		return oldTrace.equals(newTrace);
	}
}
