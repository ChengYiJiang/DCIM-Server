package com.raritan.tdz.port.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.home.PortObjectFactory;

public class DataPortPortLoadingValidator<T> implements Validator {

	@Autowired
	private ItemDAO itemDAO;

	private Dao<?> portDAO;

	@Autowired
	PortObjectFactory portObjectFactory;

	public DataPortPortLoadingValidator(){
	}


	DataPortPortLoadingValidator(Dao<?> portDAO ){
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
		if( port == null ){
			String itemName = "";
			Item item = null;
			Long itemId = (Long) targetMap.get("itemId");
			if( itemId > 0 && (item = itemDAO.read(itemId)) != null ){
				itemName = item.getItemName();
			}
			Object[] args = { itemName };
			errors.reject("PortValidator.portNotFound", args, null );
			return;
		}
	}

}
