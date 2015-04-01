/**
 * 
 */
package com.raritan.tdz.port.validators;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.home.PortCollectionHelper;

/**
 * This validator validates if the given portId is associated with
 * itemId. If not an error.
 * <p>This expects a map that contains the following<p>
 *  <ul>
 *  <li>"itemId", Long value</li>
 *  <li>"portId", Long value</li>
 *  </ul>
 * @author prasanna
 *
 */
public class ItemPortAssociationValidator<T extends IPortInfo> implements Validator {

	@Autowired
	private ItemDAO itemDAO;
	
	private Dao<Serializable> portDAO;
	
	
	private PortCollectionHelper<T> portCollectionHelper;
	
	Logger log = Logger.getLogger(getClass());

	
	public ItemPortAssociationValidator(PortCollectionHelper<T> portCollectionHelper,Dao<Serializable> portDAO ){
		this.portCollectionHelper = portCollectionHelper;
		this.portDAO = portDAO;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(IPortInfo.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		// Check args. This expects a map that contains the following
		// - itemId, <Long>
		// - portId. <Long>
		validateArgs(target,errors);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		//Now get the args
		Long itemId = (Long) targetMap.get("itemId");
		Long portId = (Long) targetMap.get("portId");
		
		Item item = itemDAO.getItem(itemId);
		String itemName = item != null ? item.getItemName() : "<UNKNOWN>";
		
		//Check to see if the port belongs to this item. If not error!
		boolean portBelongToItem = isPortBelongToItem(item,portId);
		if (!portBelongToItem){
			Object[] errorArgs = {itemName};
			errors.reject("PortValidator.portNotFound",errorArgs,"Port not found in item");
		}
	}
	
	private boolean isPortBelongToItem(Item item, Long portId) {
		IPortInfo portInfo = (IPortInfo) portDAO.read(portId);
		return portInfo != null && portInfo.getItem() != null && portInfo.getItem().equals(item);
	}

	private void validateArgs(Object target, Errors errors) {
		if (target == null || !(target instanceof Map)) 
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		if (targetMap.size() < 2)
			throw new IllegalArgumentException("You must provide a Map of String and object for this validator." +
												" At least two entries must be present");
		
		if (!targetMap.containsKey("itemId")){
			throw new IllegalArgumentException("You must provide itemId");
		}
		
		if (!targetMap.containsKey("portId")){
			throw new IllegalArgumentException("You must provide portId");
		}
		
		if (errors == null)
		{
			throw new IllegalArgumentException("You must provide a valid errors object");
		}
	}

}
