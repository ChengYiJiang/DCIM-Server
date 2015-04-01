package com.raritan.tdz.item.itemState;

import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;

public class MandatoryFieldPowerOffValidator implements
		MandatoryFieldStateValidator {

	Map<String, MandatoryFieldStateValidator> mountingToMandatoryFieldValidator;
	
	
	
	public MandatoryFieldPowerOffValidator(
			Map<String, MandatoryFieldStateValidator> mountingToMandatoryFieldValidator) {
		super();
		this.mountingToMandatoryFieldValidator = mountingToMandatoryFieldValidator;
	}



	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode,
			Errors errors, String errorCodePostFix, Request request)
			throws DataAccessException, ClassNotFoundException {

		if (null == mountingToMandatoryFieldValidator || mountingToMandatoryFieldValidator.size() == 0) return;
		
		String mounting = (null != item.getModel() && null != item.getModel().getMounting()) ? item.getModel().getMounting() : null;
		
		if (null == mounting) return;
		
		StringBuilder keyBuilder = new StringBuilder(mounting);
		String itemClassLkp = item.getClassLookup().getLkpValueCode().toString();
		keyBuilder.append(":").append(itemClassLkp);
		
		// Skip mandatory field validations for floorpdu and power panels
		// if (null == item.getClassLookup() || item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)) return;
		
		MandatoryFieldStateValidator mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(keyBuilder.toString());

		if (null == mandatoryFieldValidator) {
			mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(mounting);
		}
		
		if (null == mandatoryFieldValidator) {
			mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(itemClassLkp);
		}
		
		mandatoryFieldValidator.validateMandatoryFields(item,  item.getStatusLookup().getLkpValueCode(), errors, "normal", request);

	}

}
