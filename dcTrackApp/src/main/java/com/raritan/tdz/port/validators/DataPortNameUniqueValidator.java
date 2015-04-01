/**
 * 
 */
package com.raritan.tdz.port.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * When the data port is being deleted, the location, item name and port name must be provided to uniquely identify the data port.
 * 
 * @author KC
 *
 */
public class DataPortNameUniqueValidator implements Validator {
	
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	private DataPortDAO portDAO;
	
	@Autowired
	private ItemHome itemHome;
	
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
		String portName = (String)targetMap.get("portName");
		
		try {						
			if(validateDataPortName(item, targetMap) == false){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("Import.DataPort.DataPortNameUnique",errorArgs,"The name must be unique!");
			}		
			
			if(portName.length()>64){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("Import.DataPort.DataPortNameLength",errorArgs,"The length of name is limited to 64!");
			}
			
		} catch (Exception e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	/** 
	 * when the data port is being deleted, the location, item name and port name must be provided to uniquely identify the data port.  
	 */
	private boolean validateDataPortName(Item item, Map<String,Object> targetMap){
		String portName = (String)targetMap.get("portName");
		
		boolean validBln = false;
		
		//Port name & location name
		if( null != portName && portName.trim().length()>0 ){
			//Get all port object by item id
			List<DataPortDTO> dataPortList = itemHome.getDataPortDTOs(item.getItemId());		
			int nameCnt=0;
			if( dataPortList!=null && dataPortList.size()>0){				
				for(DataPortDTO dataPort:dataPortList){
					//dataPort = (DataPortDTO)dataPortList[i];
					if(dataPort!=null && dataPort.getPortName()!=null && dataPort.getPortName().equals(portName)){
						nameCnt++;
						if(nameCnt==1){
							validBln = true;
						}else{//unique check
							validBln = false;
						}
						//break;
					}
				}								
			}
			
			//Doesn't have any equal port name
			if(nameCnt==0){
				validBln = true;
			}
				
		}							
		
		return validBln;		
	}

	private void validateArgs(Object target, Errors errorsObj) {
		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;				
		
		if (!targetMap.containsKey("location")){
			throw new IllegalArgumentException("You must provide item domain object");
		}
		
		if (!targetMap.containsKey("portName")){
			throw new IllegalArgumentException("You must provide item domain object");
		}
		
		if (errorsObj == null){
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
	}						

}
