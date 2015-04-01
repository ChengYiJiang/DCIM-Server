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
 * This validator validates the location (cabinet, uPosition, etc)
 * of an ITItem when there is a data port or a power port connection
 * associated with one of the ports. We do not allow the cabinet, uPosition
 * to be cleared when there are connections.
 * @author prasanna
 *
 */
public class ItemWithConnectionsLocationValidator implements Validator {
	
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
		return clazz.equals(ItItem.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Item itemDomainObject = (Item) targetMap.get(errors.getObjectName());
		
		//Check to see if there are power or data connections associated with any ports for the given item
		List<Long> usedDataPorts = dataPortDAO.findUsedPorts(itemDomainObject.getItemId());
		List<Long> usedPowerPorts = powerPortDAO.findUsedPorts(itemDomainObject.getItemId());
		List<Long> usedSensorPorts = sensorPortDAO.findUsedPorts(itemDomainObject.getItemId());
		
		Item parentItem = itemDomainObject.getParentItem();
		long uPosition = itemDomainObject.getUPosition();
	
		
		boolean error = false;
		StringBuffer fields = new StringBuffer();
		//If so, check to see if the cabient, uPosistion, Chassis and slot position is blank/null. 
		if (usedDataPorts.size() > 0 || usedPowerPorts.size() > 0 || usedSensorPorts.size() > 0) {
			if (parentItem == null){
				fields.append("Cabinet\n\t");
				error = true;
			}
			
			if (uPosition == 0 || uPosition == SystemLookup.SpecialUPositions.NOPOS) {
				fields.append("U Position\n\t");
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
