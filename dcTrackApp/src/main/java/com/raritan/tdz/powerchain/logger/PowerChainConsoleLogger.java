package com.raritan.tdz.powerchain.logger;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;


public class PowerChainConsoleLogger implements PowerChainLogger {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;
	
	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void log(Errors errors) {
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = "Power Chain Migration Warning: " + messageSource.getMessage(error, Locale.getDefault());
  				log.warn(msg);
			}
		}
	}

}
