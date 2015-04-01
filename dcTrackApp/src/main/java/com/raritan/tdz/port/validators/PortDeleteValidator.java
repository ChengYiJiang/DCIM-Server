package com.raritan.tdz.port.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.port.home.IPortObject;
import com.raritan.tdz.port.home.PortObjectFactory;

public class PortDeleteValidator<T> implements Validator {

	private Dao<?> portDAO;

	@Autowired
	PortObjectFactory portObjectFactory;

	public PortDeleteValidator(){
	}


	PortDeleteValidator(Dao<?> portDAO ){
		this.portDAO= portDAO; 
	}
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(IPortInfo.class);
	}

	/* This Valdiator assumes that item has passed validation, so take care of order */
	
	@Override
	public void validate(Object target, Errors errors) {
		Map<String, Object>targetMap = (Map<String, Object>)target;
			
		if( !targetMap.containsKey("portId") || targetMap.get("portId") == null ){
			throw new IllegalArgumentException("you have to provide non null portId");
		}
		Long portId = (Long)targetMap.get("portId");
		IPortInfo port = (IPortInfo) portDAO.read(portId);
		
		IPortObject portObject = portObjectFactory.getPortObject(port, errors);
		if (portObject != null){
			portObject.validateDelete(port, errors);
		}
	}

}
