/**
 * 
 */
package com.raritan.tdz.port.validators;


import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * when the data port is connected, the data port cannot be deleted.
 * 
 * @author KC
 *
 */
public class DataPortConnectedValidator implements Validator {
	
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	private DataPortDAO portDAO;
	
	@Autowired
	private ItemHome itemHome;
	
	@Autowired
	private LksCache lksCache;
	
	Logger log = Logger.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		//validate the args
		validateArgs(target, errors);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;
				 		
		//Get the args
		Item item = (Item)itemDAO.read((Long) targetMap.get("itemId"));									
		
		try {						
			if(validateDataPortConnected(item, targetMap) == false){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("Import.DataPort.DataPortConnected",errorArgs,"The data port has been connected!");
			}							
			
		} catch (Exception e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	/** 
	 * when the data port is being deleted, the location, item name and port name must be provided to uniquely identify the data port.  
	 */
	private boolean validateDataPortConnected(Item item, Map<String,Object> targetMap){
		String portName = (String)targetMap.get("portName");
		//String location = (String)targetMap.get("location");
		boolean validBln = false;
		
		//Port name & location name
		if( null != portName && portName.trim().length()>0 ){
			
			
			Long portId = (Long)targetMap.get("portId");
			
			DataPort dataPort;
			try {
				dataPort = portDAO.loadPort(portId);
				
				//DataPort dataPort = portDAO.getPort(item.getItemId(), portSubClass, portName);	
				
				//ConnectorLkuData lkuDat = dataPort.getConnectorLookup(); 
				
				String connDir = dataPort.getConnectedDir();
				
				//if(lkuDat!=null){
				if(connDir==null){// doesn't connect
					validBln = true;
				}
				
			} catch (DataAccessException e) {				
				e.printStackTrace();
			}
			
			
			
		}							
		
		return validBln;		
	}

	private void validateArgs(Object target, Errors errorsObj) {
		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;				
		
		
		if (errorsObj == null){
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
	}						

}
