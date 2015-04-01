/**
 * 
 */
package com.raritan.tdz.item.itemState;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class MFInstallValidatorPowerOutlet implements
		MandatoryFieldStateValidator {

	private final long ABOVE = -1;
	private final long BELOW = -2;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.MandatoryFieldStateValidator#validateMandatoryFields(com.raritan.tdz.domain.Item, org.springframework.validation.Errors)
	 */
	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePostFix, Request request)
			throws DataAccessException, ClassNotFoundException {
		
		StringBuffer errorCodeBuffer = new StringBuffer();
		
		errorCodeBuffer.append("ItemValidator.mandatoryForState"); 
		errorCodeBuffer.append(".");
		errorCodeBuffer.append(errorCodePostFix);
		
		String errorCode = errorCodeBuffer.toString();
		
		String uiFieldName = "";
		String itemStatus = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValue() :"<Unknown>";
		String itemName = item.getItemName() != null ? item.getItemName() : "<Unknown>";
		
		//Check to see if either location reference or cabinet is set
		if (item.getParentItem() == null && item.getLocationReference() == null){
			uiFieldName = "Cabinet with U position or Location Ref";
			Object[] errorArgs = { uiFieldName, itemName, itemStatus,  
					(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null};
			errors.rejectValue("cmbCabinet",
					errorCode, errorArgs,
					"Mandatory field");
		} else if (item.getParentItem() == null
				 	&& item.getLocationReference() != null && item.getLocationReference().isEmpty()){
			uiFieldName = "Cabinet with U position or Location Ref";
			Object[] errorArgs = { uiFieldName, itemName, itemStatus, 
					(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null};
			errors.rejectValue("tiLocationRef",
					errorCode, errorArgs,
					"Mandatory field");
		}
		
		//Check to see the uPosition is set to either Above or Below and nothing else
		if (item.getParentItem() != null && item.getUPosition() != SystemLookup.SpecialUPositions.ABOVE && item.getUPosition() != SystemLookup.SpecialUPositions.BELOW){
			uiFieldName = "UPosition";
			Object[] errorArgs = { uiFieldName, itemName, itemStatus, 
					(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null };
			errors.rejectValue("cmbUPosition",
					"ItemValidator.mandatoryForState.aboveBelow.request", errorArgs,
					"Mandatory field");
		}

	}

}
