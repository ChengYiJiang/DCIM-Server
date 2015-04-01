package com.raritan.tdz.move.validator;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.item.dao.ItemDAO;

public class ValidateCircuitWithinCabinet implements Validator {

	@Autowired
	private ItemDAO itemDAO;
	
	private Map<Long, CircuitDAO<Serializable>> circuitDaoMap;
	
	private static Logger log = Logger.getLogger("ValidateCircuitWithinCabinet");
	
	public ValidateCircuitWithinCabinet(Map<Long, CircuitDAO<Serializable>> circuitDaoMap) {

		this.circuitDaoMap = circuitDaoMap;
		
	}

	@Override
	public boolean supports(Class<?> clazz) {

		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Long cabinetId = (Long) targetMap.get("cabinetId");
		Long portClass = (Long) targetMap.get("portClass");
		Long circuitId = (Long) targetMap.get("circuitId");
		
		if (null == cabinetId || null == portClass || null == circuitId) {
			
			if (log.isDebugEnabled()) log.debug("Provide cabinetId, portClass, circuitId to perform validation");
			return; 
		}
		
		boolean circuitWithinCabinet = isCircuitWithinCabinet(cabinetId, circuitId, portClass);
		
		if (!circuitWithinCabinet) {
			
			String cabinetName = itemDAO.getItemName(cabinetId);
			Object[] errorArgs = { cabinetName };
			errors.rejectValue("tiClass", "ItemMoveValidator.NotAllCircuitItemsWithSameCabinet", errorArgs, "Not all the items in the circuits are within the same cabinet " + cabinetName + ".");
			
		}
		
	}
	
	private boolean isCircuitWithinCabinet(Long cabinetId, Long circuitId, Long portClass) {
		
		return circuitDaoMap.get(portClass).isCircuitInCabinet(circuitId, cabinetId);
		
	}


}
