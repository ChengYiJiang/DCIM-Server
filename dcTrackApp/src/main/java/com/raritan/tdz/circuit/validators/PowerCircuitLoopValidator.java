/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerCircuitLoopValidator implements Validator {
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	private List<Long> invalidSrcPortSubClassList = new ArrayList<Long>() {{
		add(SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
		add(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);
	}};
	
	private List<Long> invalidDstPortSubClassList = new ArrayList<Long>() {{
		add(SystemLookup.PortSubClass.INPUT_CORD);
		add(SystemLookup.PortSubClass.PANEL_BREAKER);
	}};
	

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(PowerCircuit.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target, errors);
		
		PowerCircuit powerCircuit = (PowerCircuit)target;
		checkLooped(powerCircuit,errors);
	}
	
	private void checkLooped(PowerCircuit circuit, Errors errors){
		HashSet<Long> exists = new HashSet<Long>();
		boolean loop = false;
		
		for (PowerConnection conn:circuit.getCircuitConnections()){
			//Get the source port
			PowerPort srcPort = conn.getSourcePowerPort();
			PowerPort dstPort = conn.getDestPowerPort();
			
			if (srcPort.equals(dstPort)){
				errors.reject("powerProc.RackPDUConnectionLoop");
				return;
			}
			
			PowerPort srcPortDB = srcPort != null ? powerPortDAO.read(srcPort.getPortId()): null;
			PowerPort dstPortDB = dstPort != null ? powerPortDAO.read(dstPort.getPortId()): null;
			
			if (isCheckValid(srcPortDB,dstPortDB)){
				//From those get the items
				Item srcItem = srcPortDB.getItem();
				Item dstItem = dstPortDB.getItem();
				
				//Check the loop of item
				if (srcItem.equals(dstItem) || exists.contains(srcItem.getItemId()) || exists.contains(dstItem.getItemId())){
					loop = true;
					break;
				} else {
					exists.add(srcItem.getItemId());
				}
			}
		}
		
		if (loop == true){
			errors.reject("powerProc.RackPDUConnectionLoop");
		}
	}
	
	private boolean isCheckValid(PowerPort srcPort, PowerPort dstPort){
		if (srcPort == null || dstPort == null)
			return false;
		
		Long srcPortSubclassLkpValueCode = srcPort.getPortSubClassLookup() != null ? srcPort.getPortSubClassLookup().getLkpValueCode() : null;
		Long dstPortSubclassLkpValueCode = dstPort.getPortSubClassLookup() != null ? dstPort.getPortSubClassLookup().getLkpValueCode() : null;
		
		if (invalidSrcPortSubClassList.contains(srcPortSubclassLkpValueCode) && invalidDstPortSubClassList.contains(dstPortSubclassLkpValueCode)){
			return false;
		}
		
		return true;
	}
	
	private void validateArgs(Object target, Errors errors) {
		
		
		if (target == null || !(target instanceof PowerCircuit)){
			throw new IllegalArgumentException("You must provide a valid power circuit");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
		PowerCircuit powerCircuit = (PowerCircuit)target;
		List<PowerConnection> connections = powerCircuit.getCircuitConnections();
		
		if (connections == null || connections.size() == 0){
			errors.reject("powerProc.noConnectionsFound");
			return;
		}
	}


}
