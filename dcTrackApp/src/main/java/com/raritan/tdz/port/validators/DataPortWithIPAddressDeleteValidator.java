package com.raritan.tdz.port.validators;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.port.dao.DataPortDAO;

public class DataPortWithIPAddressDeleteValidator<T> implements Validator {

	@Autowired
	private DataPortDAO portDAO;
	

	private Map<Long,Validator> itemSpecificValidatorMap = new HashMap<Long, Validator>();
	
	private Logger log = Logger.getLogger(getClass());

	public DataPortWithIPAddressDeleteValidator(Map<Long,Validator> itemSpecificValidatorMap){
		this.itemSpecificValidatorMap = itemSpecificValidatorMap;
	}


	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(DataPort.class);
	}

	/* This Valdiator assumes that item has passed validation, so take care of order */
	
	@Override
	public void validate(Object target, Errors errors) {
		Map<String, Object>targetMap = (Map<String, Object>)target;
			
		if( !targetMap.containsKey("portId") || targetMap.get("portId") == null ){
			throw new IllegalArgumentException("you have to provide non null portId");
		}
		
		if (!targetMap.containsKey("skipValidation") || targetMap.get("skipValidation") == null){
			throw new IllegalArgumentException("you have to provide non null skipValidation");
		}
		
		Long portId = (Long)targetMap.get("portId");
		DataPort port = portDAO.read(portId);
		
		Item item = port.getItem();
		Integer piqId = item.getPiqId();
		
		Boolean skipValidation = (Boolean) targetMap.get("skipValidation");
		
		if (piqId != null && piqId > 0 && !skipValidation){
	
			long uniqueValue = item.getClassMountingFormFactorValue();
			
			Validator validator = itemSpecificValidatorMap.get(uniqueValue);
			
			if (validator != null)
				validator.validate(port, errors);
		}
	
	}

}
