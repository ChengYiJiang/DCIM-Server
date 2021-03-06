package com.raritan.tdz.port.home;

import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.domain.LkuData;

public class ValidateRequiredFieldLkuImpl implements ValidateRequiredFieldLku {

	/** The message source */
	protected ResourceBundleMessageSource messageSource;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	private boolean errorExist(Errors errors, String errorCode, String errorMessage) {
		List<ObjectError> errorList = errors.getAllErrors();
		for (ObjectError error : errorList) {
			String msg = messageSource.getMessage(error, Locale.getDefault());
			if (error.getCode().equals(errorCode) && msg.contains(errorMessage) /*&& msg.contains("Data")*/ ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void validate(Object target, Errors errors, String lkuField,
		String errorCode, String userField) {

		if (errorExist(errors, errorCode, "'" + userField +"'")) {
			return;
		}
		
		String requiredFieldsNotProvided = new String();
		try {
			Object value = PropertyUtils.getProperty(target, lkuField);
			LkuData lkuData = (LkuData) value;
			if (null == lkuData || null == lkuData.getLkuId()) {
				requiredFieldsNotProvided += "'" + userField +"'";
			}
			
			if (requiredFieldsNotProvided.length() > 0) {
				Object[] errorArgs = {requiredFieldsNotProvided};
				errors.rejectValue("Ports", errorCode, errorArgs, "Port required fields not provided");
			}
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for Lku: Internal Error");
		}

	}

}
