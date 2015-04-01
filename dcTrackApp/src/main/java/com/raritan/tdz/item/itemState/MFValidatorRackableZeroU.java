/**
 * 
 */
package com.raritan.tdz.item.itemState;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 * This is a special field validator for items that have both ZeroU and rackable types
 */
public class MFValidatorRackableZeroU implements
		MandatoryFieldStateValidator {

	MandatoryFieldStateValidator rackableValidator;
	MandatoryFieldStateValidator zeroUValidator;
	
	
	public MandatoryFieldStateValidator getRackableValidator() {
		return rackableValidator;
	}


	public void setRackableValidator(MandatoryFieldStateValidator rackableValidator) {
		this.rackableValidator = rackableValidator;
	}


	public MandatoryFieldStateValidator getZeroUValidator() {
		return zeroUValidator;
	}


	public void setZeroUValidator(MandatoryFieldStateValidator zeroUValidator) {
		this.zeroUValidator = zeroUValidator;
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
			} else if (mounting != null && mounting.equals(SystemLookup.Mounting.ZERO_U)){
				zeroUValidator.validateMandatoryFields(item, newStatusLkpValueCode, errors, errorCodePostFix, request);
			}
		}

	}

}
