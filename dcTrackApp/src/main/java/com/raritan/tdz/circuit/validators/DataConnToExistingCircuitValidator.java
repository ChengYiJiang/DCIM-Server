/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author  Santo Rosario
 *
 */
public class DataConnToExistingCircuitValidator implements Validator {
	@Autowired
	DataCircuitDAO dataCircuitDAO;
	
	public DataConnToExistingCircuitValidator() {
		super();
	}
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return DataConnection.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		DataCircuit circuit = (DataCircuit)targetMap.get(DataCircuit.class.getName());	
		
		//validate for circuit consisting of two nodes only
		if(circuit == null || circuit.getCircuitConnections().size() != 2) return;
		
		DataPort port = circuit.getStartConnection().getSourceDataPort();
		
		if(port == null) return;
		
		//apply only to virtual port
		if(port.isVirtual() == false) return;
		
		List<DataCircuit> recList = null;
		
		try {
			port = circuit.getEndConnection().getSourceDataPort();
			
			if(port == null) return;
			
			recList = dataCircuitDAO.viewDataCircuitByStartPortId(port.getPortId());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(recList == null || recList.size() == 0) {
			Object errorArgs[] = new Object[] { port.getDisplayName()};
			errors.reject("Import.Circuit.InvalidPartialDataCircuit", errorArgs, "Invalid partial circuit");
		}
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide an item target");
	}
	
}
