/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.chassis.home.ChassisHomeImpl;
import com.raritan.tdz.chassis.home.ChassisHomeImpl.SlotReAssignmentInfo;
import com.raritan.tdz.domain.ItItem;

/**
 * @author prasanna
 *
 */
public class ChassisModelChangeValidator implements Validator {
	
	@Autowired
	private ChassisHome chassisHome;

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return ItItem.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		
		ItItem itItem = (ItItem) itemDomainObject;
		if (itItem.getSkipValidation() == null || !itItem.getSkipValidation()){
			try {
				SlotReAssignmentInfo slotInfo = chassisHome.validateChassisModelChange(itItem.getItemId(), itItem.getModel().getModelDetailId());
				if (slotInfo.slotEventDetailsList.size() > 0) {
					// report that some blades are falling out
					String bladeList = new String("\n");
					for (ChassisHomeImpl.SlotEventDetails evt: slotInfo.slotEventDetailsList) {
						// bladeList.concat(evt.bladeItemName + "\n\t");
						bladeList += (evt.bladeItemName + "\n");
					}
					Object[] errorArgs = { bladeList };
					errors.rejectValue("cmbModel", "ItemValidator.invalidDefinitionModelChassis", errorArgs, "The following blades will not fit with the change in model");
				}
			} catch (Throwable t) {
				
			}
		}
		//We need to validate if the chassis slots for the model is assigned.

	}

}
