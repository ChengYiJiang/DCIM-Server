/**
 * 
 */
package com.raritan.tdz.item.itemState;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 * This is a special field validator for items that have both ZeroU and rackable types
 */
public class MFValidatorRackableBladeFS implements
		MandatoryFieldStateValidator {

	MandatoryFieldStateValidator rackableValidator;
	MandatoryFieldStateValidator bladeValidator;
	MandatoryFieldStateValidator freeStandingValidator;
	
	@Autowired
	ItemDAO itemDAO;
	
	public MandatoryFieldStateValidator getRackableValidator() {
		return rackableValidator;
	}


	public void setRackableValidator(MandatoryFieldStateValidator rackableValidator) {
		this.rackableValidator = rackableValidator;
	}

	public MandatoryFieldStateValidator getBladeValidator() {
		return bladeValidator;
	}


	public void setBladeValidator(MandatoryFieldStateValidator bladeValidator) {
		this.bladeValidator = bladeValidator;
	}

	

	public MandatoryFieldStateValidator getFreeStandingValidator() {
		return freeStandingValidator;
	}


	public void setFreeStandingValidator(
			MandatoryFieldStateValidator freeStandingValidator) {
		this.freeStandingValidator = freeStandingValidator;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.MandatoryFieldStateValidator#validateMandatoryFields(com.raritan.tdz.domain.Item, org.springframework.validation.Errors)
	 */
	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePostFix, Request request)
			throws DataAccessException, ClassNotFoundException {
		ModelDetails model = item.getModel();
		if (model != null){
			String mounting = model.getMounting();
			
			if (mounting != null && mounting.equals(SystemLookup.Mounting.RACKABLE)){
				rackableValidator.validateMandatoryFields(item, newStatusLkpValueCode, errors, errorCodePostFix, request);
			} else if (mounting != null && mounting.equals(SystemLookup.Mounting.BLADE)){
				bladeValidator.validateMandatoryFields(item, newStatusLkpValueCode, errors, errorCodePostFix, request);
			} else if (mounting != null && mounting.equals(SystemLookup.Mounting.FREE_STANDING)){
				if (item.getParentItem() != null && item.getParentItem().getSubclassLookup() != null && item.getParentItem().getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CONTAINER)){
					//We perform a get on the parent item as it is giving a proxy object and jx path fails due to this.
					CabinetItem parent = (CabinetItem) itemDAO.initializeAndUnproxy(itemDAO.read(item.getParentItem().getItemId()));
					freeStandingValidator.validateMandatoryFields(parent, newStatusLkpValueCode, errors, errorCodePostFix, request);
				}
				else {
					//CR Number: 49403
					//Basically, since we do not have a parent item, obviously the rowLabel and Position in Row is not set
					//Therefore checking against the item will essentially be the same effect as hardcoding the errors for 
					//rowLabel and positionInRow.
					freeStandingValidator.validateMandatoryFields(item, newStatusLkpValueCode, errors, errorCodePostFix, request);
				}
			}
		}

	}

}
