/**
 * 
 */
package com.raritan.tdz.beanvalidation.home;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 *
 */
public class BeanValidationHomeImpl implements BeanValidationHome {
	
	private Validator springValidator;
	private MessageSource messageSource;

	
	public BeanValidationHomeImpl(Validator springValidator, MessageSource messageSource){
		this.springValidator = springValidator;
		this.messageSource = messageSource;
	}
	
	
	public MessageSource getMessageSource() {
		return messageSource;
	}


	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	public Validator getSpringValidator() {
		return springValidator;
	}


	public void setSpringValidator(Validator springValidator) {
		this.springValidator = springValidator;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.beanvalidation.home.BeanValidationHome#validate(java.lang.Object, java.lang.String)
	 */
	@Override
	public Errors validate(Object target){
		if (target != null){
			DataBinder binder = new DataBinder(target, target.getClass() != null ? target.getClass().getName() : null);
			getSpringValidator().validate(target, binder.getBindingResult() );
			List<ObjectError> errors = binder.getBindingResult().getAllErrors();
			return binder.getBindingResult();
		}
		
		return null;
	}

}
