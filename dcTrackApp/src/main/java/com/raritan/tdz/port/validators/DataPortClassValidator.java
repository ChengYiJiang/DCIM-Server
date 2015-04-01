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

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * This validates the data port class in Device, Network, UPS, FloorPDU, Rack PDU, CRAC, and Probe.
 * 
 * @author KC
 *
 */
public class DataPortClassValidator implements Validator {

	//@Autowired
	//private ItemModifyRoleValidator itemModifyRoleValidator;
	
	@Autowired
	ItemDAO itemDAO;
			
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
			if(checkDataPortClasses(item) == false){
				Object[] errorArgs = {item.getItemName()};
				errors.reject("Import.DataPort.UnsupportedClass",errorArgs,"Item class type error!");
			}			
			
		} catch (Exception e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	private boolean checkDataPortClasses(Item item){
		boolean validBln = false;
		long classCode = item.getClassLookup().getLkpValueCode();
		
		if(classCode == SystemLookup.Class.DEVICE ||
		   classCode == SystemLookup.Class.NETWORK ||
		   classCode == SystemLookup.Class.UPS ||
		   classCode == SystemLookup.Class.FLOOR_PDU ||
		   classCode == SystemLookup.Class.RACK_PDU ||
		   classCode == SystemLookup.Class.CRAC ||
		   classCode == SystemLookup.Class.PROBE ){
			validBln = true;
		}else{
			validBln = false;
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
