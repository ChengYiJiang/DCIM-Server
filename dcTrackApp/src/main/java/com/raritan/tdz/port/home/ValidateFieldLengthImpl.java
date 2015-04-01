package com.raritan.tdz.port.home;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.validation.Errors;

public class ValidateFieldLengthImpl implements ValidateFieldLength {

	// PortValidator.dataPortNameLength , PortValidator.powerPortNameLength, PortValidator.sensorPortNameLength
	@Override
	public void validate(Object target, Errors errors, String field,
			Long fieldMaxLength, String userField, String errorCode) {
		// TODO Auto-generated method stub
		try {
			Object value = PropertyUtils.getProperty(target, field);
			if (null != value && value instanceof String) {
				if (((String)value).length() > fieldMaxLength) {
					Object[] errorArgs = { userField };
					errors.rejectValue("Ports", errorCode, errorArgs, userField + " exceeds max length");
				}
			}
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for "+ field +": Internal Error");
		}
	}

}
