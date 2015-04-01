package com.raritan.tdz.item.itemState;

 import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;

public class MFInstallValidatorPowerPanel implements
		MandatoryFieldStateValidator {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.MandatoryFieldStateValidator#validateMandatoryFields(com.raritan.tdz.domain.Item, org.springframework.validation.Errors)
	 */
	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePostFix, Request request)
			throws DataAccessException, ClassNotFoundException {

		// Validate only for power panels, since the panel share the same class as the floor pdu, add an extra check here..
		if (null == item.getSubclassLookup()) return;

		StringBuffer errorCodeBuffer = new StringBuffer();
		
		errorCodeBuffer.append("ItemValidator.mandatoryForState"); 
		errorCodeBuffer.append(".");
		errorCodeBuffer.append(errorCodePostFix);
		
		String errorCode = errorCodeBuffer.toString();
		
		String uiFieldName = "";
		// String itemStatus = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValue() :"<Unknown>";
		String itemName = item.getItemName() != null ? item.getItemName() : "<Unknown>";
		
		//Check to see if floor pdu (parent item) is set for the power panel 
		if (item.getParentItem() == null){
			uiFieldName = "parent Floor PDU";
			Object[] errorArgs = { uiFieldName, itemName, "Installed", 
					(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null };
			errors.rejectValue("cmbCabinet",
					errorCode, errorArgs,
					"Mandatory field");
		} 

	}

}
