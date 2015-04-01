package com.raritan.tdz.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.exception.BusinessValidationException;

public class BusinessExceptionHelper {

	@Autowired (required=true)
	public ResourceBundleMessageSource messageSource;
	
	
	
	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@SuppressWarnings("deprecation")
	public void throwBusinessValidationException(BusinessValidationException e, Errors errors, String warningCallBack) throws BusinessValidationException {
		if (null == e) { 
			e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		}
		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors) {
  				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0) {
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0) {
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}
	
	public BusinessValidationException getBusinessValidationException(BusinessValidationException e, Errors errors, String warningCallBack) {
		if (null == e) { 
			e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		}
		if (null != errors && errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors) {
				String msg = null;
				try {
					msg = messageSource.getMessage(error, Locale.getDefault());
				}
				catch(NoSuchMessageException ex) {
					msg = messageSource.getMessage(error, null);
				}
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0) {
			e.setCallbackURL(null);
			return e;
		} else if (e.getValidationWarnings().size() > 0) {
			e.setCallbackURL(warningCallBack);
			return e;
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public MapBindingResult getErrorObject(Class clazz) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, clazz.getName() );
		return errors;
	}
	
	public MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, refErrors.getObjectName() );
		return errors;
		
	}
	
	public String getMessage(Errors errors) {
		
		StringBuffer msgBuffer = new StringBuffer();
		
		if (null != errors && errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors) {
				String msg = null;
				try {
					msg = messageSource.getMessage(error, Locale.getDefault());
				}
				catch(NoSuchMessageException ex) {
					msg = messageSource.getMessage(error, null);
				}
				msgBuffer.append(msg);
				msgBuffer.append("\n");
			}
		}

		return msgBuffer.toString();
	}
	
	
}
