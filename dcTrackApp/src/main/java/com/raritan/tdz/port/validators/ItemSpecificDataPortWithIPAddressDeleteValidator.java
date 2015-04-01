package com.raritan.tdz.port.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.home.PortObjectFactory;

public class ItemSpecificDataPortWithIPAddressDeleteValidator<T> implements Validator {

	@Autowired
	PortObjectFactory portObjectFactory;

	public ItemSpecificDataPortWithIPAddressDeleteValidator(){
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
		if (!(target instanceof DataPort)){
			throw new IllegalArgumentException("You must provide a valid data Port");
		}
		
		DataPort dataPort = (DataPort) target;
		if (dataPort.getIpAddresses() != null && !dataPort.getIpAddresses().isEmpty()){
			//it is an error
			String itemClass = dataPort.getItem() != null && dataPort.getItem().getClassLookup() != null 
					? dataPort.getItem().getClassLookup().getLkpValue() 
					: "<Unknown>";
			Object[] errorArgs = { itemClass  };
			errors.reject("PortValidator.cannotDeleteDataPortWithIPAddress",errorArgs,"Cannot delete data port with IPAddress");
		}
	
	}

}
