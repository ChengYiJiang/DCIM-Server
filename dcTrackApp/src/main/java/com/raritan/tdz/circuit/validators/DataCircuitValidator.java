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

import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * @author Santo Rosario
 *
 */
public class DataCircuitValidator implements Validator {
	
	@Autowired
	DataPortDAO dataPortDAO;
	
	@Autowired
	DataConnCompatibilityValidatorFactory compValidatorFactory;
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	DataConnPortValidator dataConnPortValidator;
	
	@Autowired
	CircuitItemsValidator circuitItemsValidator;
	
	@Autowired
	DataConnToExistingCircuitValidator dataConnToExistingCircuitValidator;
	
	private boolean targetIsMap = false;

	public DataCircuitValidator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(DataCircuit.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//Validate args
		validateArgs(target,errors);
		
		DataCircuit dataCircuit;
		
		if (targetIsMap){
			Map<String,Object> targetMap = (Map<String,Object>) target;
			dataCircuit = (DataCircuit)targetMap.get(DataCircuit.class.getName());
		} else {
			dataCircuit = (DataCircuit)target;
		}
		
		List<DataConnection> connections = dataCircuit.getCircuitConnections();
		
		if (connections == null || connections.size() == 0){
			errors.reject("dataProc.noConnectionsFound");
			return;
		}

		validateDuplicatePort(dataCircuit, errors);
		
		DataConnection priorConn = null;
		DataPort startPort = null;
		DataPort endPort = null;
		
		
		for (DataConnection conn:dataCircuit.getCircuitConnections()){
			DataPort srcPort = dataPortDAO.read(conn.getSourcePortId());
			
			circuitItemsValidator.validate(srcPort.getItem(), errors);
			
			validateUsedPort(startPort, conn.getSourceDataPort(), srcPort, errors);
			
			dataConnPortValidator.validate(getTargetMap(conn.getSourceDataPort()), errors);
			
			conn.setSourceDataPort(srcPort);
			
			if(priorConn != null) {
				priorConn.setDestDataPort(srcPort);
				validateMiddlePort(srcPort, priorConn.getSourceDataPort(), conn.getDestDataPort(), errors);
			}
			
			if(startPort == null) {
				startPort = srcPort;
			}
			
			priorConn = conn;
			endPort = srcPort;
			
		}
			
		dataConnToExistingCircuitValidator.validate(getTargetMap(dataCircuit), errors);
		
		validateStartPort(startPort, errors);
		validateEndPort(endPort, errors);
		
		if(errors.hasErrors()) {
			return;
		}
		
		for (DataConnection conn:dataCircuit.getCircuitConnections()){
			validateCompatibility(conn, errors);
		}
	}
	
	private void validateCompatibility(DataConnection conn, Errors errors) {
		DataConnCompatibilityValidator compValidator = compValidatorFactory.getCompatibilityValidator(conn);
		Map<String, Object> targetMap = getTargetMap(conn);
		
		if (compValidator != null)
			compValidator.validate(targetMap, errors);
	}

	private Map<String, Object> getTargetMap(DataConnection conn) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(DataConnection.class.getName(), conn);
		targetMap.put(Integer.class.getName(), 0);
		return targetMap;
	}

	private Map<String, Object> getTargetMap(DataPort port) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(DataPort.class.getName(), port);
		targetMap.put(Integer.class.getName(), 0);
		return targetMap;
	}

	private Map<String, Object> getTargetMap(DataCircuit obj) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(DataCircuit.class.getName(), obj);
		targetMap.put(Integer.class.getName(), 0);
		return targetMap;
	}

	private void validateArgs(Object target, Errors errors) {
		
		targetIsMap = false;
		if (target != null && target instanceof Map){
			targetIsMap = true;
			
			Map<String,Object> targetMap = (Map<String,Object>)target;
			if (targetMap.size() < 2){
				throw new IllegalArgumentException("You must provide a valid map that contains a data circuit and a bool to indicate the source port connection should be included in calculation");
			}
			
			if (targetMap.get(DataCircuit.class.getName()) == null){
				throw new IllegalArgumentException("You must provide the data circuit to validate");
			}
			
			if (targetMap.get(Boolean.class.getName()) == null){
				throw new IllegalArgumentException("You must provide the boolean to indicate if you need to include first port sum in the calculation. It cannot be null");
			}
		}
		
		if (target == null || ( !targetIsMap  && !(target instanceof DataCircuit))){
			throw new IllegalArgumentException("You must provide a valid data circuit");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
	}
	
	private void validateUsedPort(DataPort startPort, DataPort srcPortClient, DataPort srcPort, Errors errors) {
		if(startPort != null && (startPort.isLogical() || startPort.isVirtual())) {
			return;
		}
		
		if (srcPortClient.getUsed() == false && srcPort.getUsed() == true){
			Object errorArgs[] = new Object[] { srcPort.getDisplayName() };
			errors.reject("dataProc.srcPortUsed", errorArgs, "Port in use");
		}
	}
	
	private void validateStartPort(DataPort port, Errors errors) {	
		Map<Long, Boolean> mapper = new HashMap<Long, Boolean>();
		mapper.put(SystemLookup.Class.DEVICE, true);
		mapper.put(SystemLookup.Class.NETWORK, true);
		mapper.put(SystemLookup.Class.DATA_PANEL, true);
		mapper.put(SystemLookup.Class.CRAC, true);
		mapper.put(SystemLookup.Class.FLOOR_PDU, true);
		mapper.put(SystemLookup.Class.UPS, true);
		mapper.put(SystemLookup.Class.PROBE, true);
		mapper.put(SystemLookup.Class.RACK_PDU, true);
		
		Long key = port.getItem().getClassLookup().getLkpValueCode();
		
		if(!mapper.containsKey(key)){
			Object errorArgs[] = new Object[] { port.getItem().getClassLookup().getLkpValue() };
			errors.reject("dataProc.badCircuitStart", errorArgs, "Data circuit cannot start with an item of this class.");
		}
	}

	private void validateEndPort(DataPort port, Errors errors) {	
		Map<Long, Boolean> mapper = new HashMap<Long, Boolean>();
		mapper.put(SystemLookup.Class.DEVICE, true);
		mapper.put(SystemLookup.Class.NETWORK, true);
		
		Long key = port.getItem().getClassLookup().getLkpValueCode();
		
		if(!mapper.containsKey(key)){
			Object errorArgs[] = new Object[] { port.getItem().getClassLookup().getLkpValue() };
			errors.reject("dataProc.badCircuitEnd", errorArgs, "Data circuit cannot end with an item of this class.");
		}
	}	

	private void validateMiddlePort(DataPort port, DataPort priorPort, DataPort nextPort, Errors errors) {	
		if(nextPort == null || priorPort.isLogical() || priorPort.isVirtual()) return;
		
		Long key = port.getItem().getClassLookup().getLkpValueCode();
		
		if(!key.equals(SystemLookup.Class.DATA_PANEL)){
			Object errorArgs[] = new Object[] { port.getDisplayName() };
			errors.reject("dataProc.badCircuitMiddleNode", errorArgs, "Data circuit cans only have data panels between start and end nodes.");
		}
	}	

	private void validateDuplicatePort(DataCircuit circuit, Errors errors) {
		int count;
	
		for (Long p1:circuit.getPortIdList()){
			count = 0;
			
			for (Long p2:circuit.getPortIdList()){
				if(p1.equals(p2)) count++;
			}
			
			if(count > 1){
				DataPort port = dataPortDAO.read(p1);
				Object errorArgs[] = new Object[] { port.getDisplayName() };
				errors.reject("dataProc.badCircuitDupPorts", errorArgs, "Invalid data circuit. Port use more than one time in the same circuit.");
			}
		}
	}		
}
