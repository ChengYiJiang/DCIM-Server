/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortFinderDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.port.dao.SensorPortFinderDAO;

/**
 * @author prasanna
 *
 */
public class BladeWithConnectionsLocationValidator implements Validator {
	
	@Autowired
	DataPortFinderDAO dataPortDAO;
	
	@Autowired
	PowerPortFinderDAO powerPortDAO;
	
	@Autowired
	SensorPortFinderDAO sensorPortDAO;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return ItItem.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		ItItem itemDomainObject = (ItItem) targetMap.get(errors.getObjectName());
		
		//Check to see if there are power or data connections associated with any ports for the given item
		List<Long> usedDataPorts = dataPortDAO.findUsedPorts(itemDomainObject.getItemId());
		List<Long> usedPowerPorts = powerPortDAO.findUsedPorts(itemDomainObject.getItemId());
		
		
		Item parentItem = itemDomainObject.getParentItem();
		Item bladeChassis = itemDomainObject.getBladeChassis();
		long slotPosition = itemDomainObject.getSlotPosition();
		
		boolean error = false;
		StringBuffer fields = new StringBuffer();
		//If so, check to see if the cabient, uPosistion, Chassis and slot position is blank/null. 
		if (usedDataPorts.size() > 0 || usedPowerPorts.size() > 0) {
			if (parentItem == null){
				fields.append("Cabinet\n\t");
				error = true;
			}
			if (bladeChassis == null) {
				fields.append("Chassis\n\t");
				error = true;
			}
			if (slotPosition <= 0){
				fields.append("Slot Position\n\t");
				error = true;
			}
		}
		
		if (error == true){
			String itemName = itemDomainObject.getItemName();
			
			Object[] errorArgs = {  itemName == null ? "<Unknown>":itemName, fields.deleteCharAt(fields.lastIndexOf("\n\t"))};
			errors.reject("ItemValidator.connectionExists.cannotClearFields", errorArgs,"Cannot clear fields");
		}
		
	}

}
