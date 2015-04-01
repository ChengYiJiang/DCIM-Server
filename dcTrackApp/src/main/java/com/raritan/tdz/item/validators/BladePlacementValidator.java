/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelChassis;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class BladePlacementValidator implements Validator {
	
	@Autowired
	private ChassisHome chassisHome;
	
	@Autowired
	private DataCircuitHome dataCircuitHome;

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
		
		// validate of the state of the chassis allow blade to be placed in it
		validateChassisState(itemDomainObject, errors);
		
		//We need to validate if the chassis slots for the model is assigned.
		validateChassisSlots(itemDomainObject, errors);

		/* Check the the slots are still available
		 * Model of the blade can change
		 * Some other user can take this slot */
		validatePlacementAvailibility(itemDomainObject, errors);
		
		validateCircuits(itemDomainObject, errors);

	}
	
	private void validateChassisState(Object target, Errors errors) {
		ItItem itItem = (ItItem) target;
		Item bladeChassisItem = itItem.getBladeChassis();
		if (null != bladeChassisItem && bladeChassisItem.isStatusArchived()) {
			Object[] errorArgs = { "Chassis", bladeChassisItem.getItemName() };
			errors.rejectValue("cmbChassis", "ItemValidator.invalidParentStatus", errorArgs, "Chassis status do not allow placement");
		}
	}
	
	private void validateChassisSlots(Object target, Errors errors) {
		ItItem itItem = (ItItem) target;
		if (itItem.getSkipValidation() == null || !itItem.getSkipValidation()) {
			try {
				ModelChassis modelChassisFront = null, modelChassisRear = null;
				String modelName = " ";
				String mfrName = " ";
				Item bladeChassisItem = itItem.getBladeChassis();
				if (null != bladeChassisItem) {
					if (null != bladeChassisItem.getModel()) {
						modelChassisFront = chassisHome.getBladeChassis(bladeChassisItem.getModel().getModelDetailId(), SystemLookup.ChassisFace.FRONT);
						modelChassisRear = chassisHome.getBladeChassis(bladeChassisItem.getModel().getModelDetailId(), SystemLookup.ChassisFace.REAR);
						modelName = bladeChassisItem.getModel().getModelName();
						if (null != bladeChassisItem.getModel().getModelMfrDetails()) {
							mfrName = bladeChassisItem.getModel().getModelMfrDetails().getMfrName();
						}
					}
				}
				else {
					// Chassis can be null, this is not an error
					return;
				}
				
				Object[] errorArgs = { modelName, mfrName };
				if (modelChassisFront == null && modelChassisRear == null) {
					errors.rejectValue("cmbChassis", "ItemValidator.invalidModelChassis", errorArgs, "No slots defined in model library for this chassis model");
				} else if (modelChassisFront.getModelChassisSlot() == null && 
							modelChassisRear.getModelChassisSlot() == null) {
					errors.rejectValue("cmbChassis", "ItemValidator.invalidModelChassis", errorArgs, "No slots defined in model library for this chassis model");
				} else if (modelChassisFront.getModelChassisSlot().size() == 0 &&
							modelChassisRear.getModelChassisSlot().size() == 0) {
					errors.rejectValue("cmbChassis", "ItemValidator.invalidModelChassis", errorArgs, "No slots defined in model library for this chassis model");
				}
				
				// TODO:: Add more validation for the available cabinet, chassis, slot
			}
			catch(Throwable t){
				 
			}
		}
	}

	private void validatePlacementAvailibility(Object target, Errors errors) {
		ItItem itItem = (ItItem) target;
		try {
			if (null == itItem.getParentItem() && 
					null != itItem.getBladeChassis()) {
				Object[] errorArgs = {};
				errors.rejectValue("cmbCabinet", "ItemValidator.chassisWithoutCabinet", errorArgs, "Chassis is selected without a Cabinet");
			}
			else if (null == itItem.getBladeChassis() &&
					itItem.getSlotPosition() > 0) {
				Object[] errorArgs = {};
				errors.rejectValue("cmbChassis", "ItemValidator.slotWithoutChassis", errorArgs, "Slot Number is assigned without a Chassis");
			} 
			else if (null != itItem.getBladeChassis() && 
					null != itItem.getModel()) {
				long locationId = (null != itItem.getDataCenterLocation()) ? itItem.getDataCenterLocation().getDataCenterLocationId() : -1;
				long cabinetId = itItem.getParentItem().getItemId();
				Long chassisId = itItem.getBladeChassis().getItemId();
				long bladeModelId = itItem.getModel().getModelDetailId(); 
				long faceLksValueCode = chassisHome.getBladeFaceLookUpLks(itItem);
				long bladeId = itItem.getItemId();
				Long itemToMoveId = itItem.getItemToMoveId();
				// for a new when-move blade check the placement against the original blade
				if (bladeId <=0 && null != itemToMoveId && itemToMoveId > 0) {
					bladeId = itemToMoveId.intValue();
				}
				long currentSlot = itItem.getSlotPosition();
				boolean cabinetFound = false;
				boolean chassisFound = false;
				/* validate if the location is selected */
				if (-1 == locationId) {
					Object[] errorArgs = {};
					errors.rejectValue("cmbLocation", "ItemValidator.noLocation", errorArgs, "No location selected");
				}
				else {
					/* validate if the selected location has the given cabinet */
					List<ValueIdDTO> cabinetList = chassisHome.getAllAvailableCabinetForBladeModel(locationId, bladeModelId, bladeId);
					for (ValueIdDTO cabinet: cabinetList) {
						Long availCabinetId = (Long) (cabinet.getData());
						if (availCabinetId == cabinetId) {
							cabinetFound = true;
							break;
						}
					}
				}
				if (!cabinetFound) {
					Object[] errorArgs = {};
					errors.rejectValue("cmbCabinet", "ItemValidator.noCabinetInLocation", errorArgs, "Selected cabinet is not available at the selected location");
				}
				else {
					/* validate if the selected cabinet have the selected chassis */
					List<ValueIdDTO> chassisList = chassisHome.getAllChassisInCabinet(cabinetId, bladeModelId, bladeId);
					for (ValueIdDTO chassis: chassisList) {
						Long availChassisId = (Long) (chassis.getData());
						if (availChassisId.longValue() == chassisId) {
							chassisFound = true;
							break;
						}
					}
				}
				if (!chassisFound) {
					Object[] errorArgs = {};
					errors.rejectValue("cmbChassis", "ItemValidator.noAvailableChassisInCabinet", errorArgs, "Selected chassis is not available in the selected Cabinet");
				}
				else {
					/* validate if the selecled chassis have the selected face */
					/* validate if the selected slot for a given face is available */
					Map<Long, String> slotsInfo = chassisHome.getAvailableSlotPositions(chassisId, bladeModelId, faceLksValueCode, bladeId);
					if (currentSlot > 0 && !slotsInfo.containsKey(currentSlot)) {
						String slotLabel = chassisHome.getChassisSlotLabel(chassisId, faceLksValueCode, (new Long(itItem.getSlotPosition())).intValue() ); //itItem.getSlotPosition();
						String chassisName = itItem.getBladeChassis().getItemName();
						Object[] errorArgs = { slotLabel, chassisName };
						errors.rejectValue("cmbSlotPosition", "ItemValidator.noAvailableSlotPosition", errorArgs, "Slot position is not available");
					}
				}
				/* validate: cannot place blade in chassis rear if the chassis rear is undefined */
				if (!chassisHome.isChassisRearDefined(chassisId) && faceLksValueCode == SystemLookup.ChassisFace.REAR) {
					String chassisName = itItem.getBladeChassis().getItemName();
					Object[] errorArgs = { chassisName };
					errors.rejectValue("cmbChassis", "ItemValidator.noChassisRearDefined", errorArgs, "Chassis rear is not defined");
				}
			}
		}
		catch(Throwable t){
			 
		}
	}
	
	private void validateCircuits(Object target, Errors errors) {
		try {
			ItItem itItem = (ItItem) target;
			ItItem origItem = chassisHome.getItItemDomainObject(itItem.getItemId());
			/* Chassis is getting changed, 
			 * 1. check if logical connection exist between the edited blade and chassis 
			 * 2. TODO:: also check if logical connection exist between this blade and another blade in the same chassis */
			if (null != itItem && null != origItem && null != origItem.getBladeChassis() &&
					(null == itItem.getBladeChassis() || 
						itItem.getBladeChassis().getItemId() != origItem.getBladeChassis().getItemId())) {
				if (dataCircuitHome.isLogicalConnectionsExist(itItem.getItemId(), origItem.getBladeChassis().getItemId())) 	{
					Object[] errorArgs = { itItem.getItemName(), origItem.getBladeChassis().getItemName() };
					errors.rejectValue("cmbChassis", "BladesValidation.logicalCircuitExist", errorArgs, "Cannot delete blade, logical circuit exist between blade and chassis");
				}
				else {
					if (dataCircuitHome.isLogicalConnectionsExist(itItem.getItemId(), 0)) 	{
						Object[] errorArgs = { itItem.getItemName(), "another blade" };
						errors.rejectValue("cmbChassis", "BladesValidation.logicalCircuitExist", errorArgs, "Cannot delete blade, logical circuit exist between blade and chassis");
					}
				}
			}
			
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
}
