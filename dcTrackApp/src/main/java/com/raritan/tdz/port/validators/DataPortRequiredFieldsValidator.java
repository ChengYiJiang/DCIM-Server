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
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * This validates the data port class in Device, Network, UPS, FloorPDU, Rack PDU, CRAC, and Probe.
 * 
 * @author KC
 *
 */
public class DataPortRequiredFieldsValidator implements Validator {
	
	
	@Autowired
	ItemDAO itemDAO;
	
	@Autowired
	private DataPortDAO portDAO;
	
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
		
		//Long portId = (Long)targetMap.get("portId");
		//DataPort port = portDAO.read(portId);				
						
		try {						
			if(validateDataPortMustFields(item, targetMap) == false){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("Import.DataPort.RequiredFields",errorArgs,"Item class type error!");
			}			
			
		} catch (Exception e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	/** When a data port is being added, the location, item name, port name, port type, connector, media, protocol and data rate must be provided for the data port. 
	 * @throws BusinessValidationException */
	private boolean validateDataPortMustFields(Item item, Map<String,Object> targetMap){
		
		String location = (String)targetMap.get("location");
		String itemName = (String)targetMap.get("itemName");
		String portName = (String)targetMap.get("portName");
		String portType = (String)targetMap.get("portType");
		String connector = (String)targetMap.get("connector");
		String media = (String)targetMap.get("media");
		String protocol = (String)targetMap.get("protocol");
		String dataRate = (String)targetMap.get("dataRate");
		
		boolean validBln = false;		
		
		
		//Location
		if( null !=item && null != location && null != item.getDataCenterLocation().getCode() && location.length()>0 ){
			//Item name
			if( null != itemName && itemName.trim().length()>0 ){
				//Port name
				if( null != portName && portName.trim().length()>0 ){
					//Port type
					if( null != portType && portType.trim().length()>0 ){
						//Connector
						if( null != connector && connector.trim().length()>0 ){
							//Media
							if( null != media && media.trim().length()>0 ){
								//Protocol
								if( null != protocol && protocol.trim().length()>0 ){
									//Data rate
									if( null != dataRate && dataRate.trim().length()>0 ){
										validBln = true;
									}	
								}	
							}		
						}	
					}	
				}
			}
		}						
		
		
		return validBln;		
	}

	private void validateArgs(Object target, Errors errorsObj) {
		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;				
		
		if (!targetMap.containsKey("itemId")){
			throw new IllegalArgumentException("You must provide item domain object");
		}
						
		if (errorsObj == null){
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
		
	}						

}
