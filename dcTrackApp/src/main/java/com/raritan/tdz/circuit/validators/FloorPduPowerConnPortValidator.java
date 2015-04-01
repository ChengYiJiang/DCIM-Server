/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author Santo Rosario
 *
 */
public class FloorPduPowerConnPortValidator implements Validator {
	
	@Autowired
	ItemDAO itemDAO;
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return PowerPort.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		PowerPort powerPort = (PowerPort)target;
		
		MeItem item = (MeItem)itemDAO.loadItem(powerPort.getItem().getItemId());
				
		//Check UPS Bank
		if(item.getUpsBankItem() == null){ 
			Object[] errorsArgs = { item.getItemName() };
			errors.reject("powerProc.missingUpsBank", errorsArgs, null);
		}
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a power port target");
	}

}
