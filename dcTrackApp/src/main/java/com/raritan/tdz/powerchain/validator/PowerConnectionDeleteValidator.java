package com.raritan.tdz.powerchain.validator;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.exception.DataAccessException;

public class PowerConnectionDeleteValidator implements Validator {

	@Autowired
	PowerCircuitDAO powerCircuitDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		PowerConnection powerConn = (PowerConnection)targetMap.get(PowerConnection.class.getName());
		String errorCode = (String)  targetMap.get("errorcode");

		try {
			List<PowerCircuit> circuitList = powerCircuitDAO.viewPowerCircuitByConnId(powerConn.getConnectionId());
			if (null != circuitList && circuitList.size() > 0) {
				// PowerChain.cannotDeleteConnectionWithCircuits
				Object[] errorArgs = new Object[] { };
				errors.reject(errorCode, errorArgs, "This connection has circuits, cannot delete.");
			}
		} catch (DataAccessException e) {
			Boolean isException = true;
			e.printStackTrace();
			Assert.isTrue(isException, "error while getting circuit list for a connection");
		}
	}

}
