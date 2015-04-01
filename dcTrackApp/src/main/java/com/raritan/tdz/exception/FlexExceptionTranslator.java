package com.raritan.tdz.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.flex.core.ExceptionTranslator;

import flex.messaging.MessageException;

/**
 * Custom Exception Translator for BlazeDS.
 *
 * @author Andrew Cohen
 */
public class FlexExceptionTranslator implements ExceptionTranslator {
	
	@Override
	public boolean handles(Class<?> clazz) {
//		if (ServiceLayerException.class.isAssignableFrom( clazz )) {
//			return true;
//		}
//		return false;
		return true;
	}

	@Override
	public MessageException translate(Throwable exceptionObj) {
		MessageException msgException = new MessageException();
		msgException.setMessage( exceptionObj.getMessage() );
		
		if (!(exceptionObj instanceof BusinessValidationException || exceptionObj instanceof SessionTimeoutException)) {
			// Anything other than a business validation exception
			// gets wrapped in a SystemException for the client.
			SystemException se = new SystemException( exceptionObj );
			exceptionObj = se;
		}
		
		Map<String, Object> data = new HashMap<String, Object>(2);
		// Add the exception class name - the client needs this because remote alias cannot be used for exceptions
		data.put("exceptionType", exceptionObj.getClass().getSimpleName());
		// Add the root cause exception object
		data.put("exceptionObject", exceptionObj);
		msgException.setExtendedData( data );
		
		return msgException;
	}
}
