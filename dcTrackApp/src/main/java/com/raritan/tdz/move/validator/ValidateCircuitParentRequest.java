package com.raritan.tdz.move.validator;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.request.home.RequestInfo;

public class ValidateCircuitParentRequest implements Validator {

	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	@Autowired
	private CircuitDAO<PowerCircuit> powerCircuitDAOExt;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;

		CircuitViewData circuitView = (CircuitViewData) targetMap.get(CircuitViewData.class.getName());
		
		List<Long> connIds = circuitView.getConnList();
		
		List<RequestInfo> requestInfos = null;
		if (circuitView.isDataCircuit()) {
			requestInfos = dataCircuitDAOExt.getParentMoveRequest(connIds);
		}
		else if (circuitView.isPowerCircuit()) {
			requestInfos = powerCircuitDAOExt.getParentMoveRequest(connIds);
		}

		if (null == requestInfos || requestInfos.size() == 0) return;
		
		StringBuffer errorMsg = new StringBuffer();
		
		for (RequestInfo reqInfo: requestInfos) {
			
			errorMsg.append(reqInfo.getItemName()).
						append(" Request: ").
						append(reqInfo.getRequestNumber()).
						append(" ").
						append(reqInfo.getRequestType())
						.append("\n");
		}
		errorMsg.append("\n\nDo you want to continue?");
		
		Object[] errorArgs = { errorMsg.toString() };
		errors.rejectValue("itemMove", "ItemMoveValidator.parentHasPendingRequest", errorArgs, errorMsg.toString());
		
	}

}
