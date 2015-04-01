package com.raritan.tdz.exception;

import java.util.Map;

import org.apache.log4j.Logger;

import flex.messaging.MessageException;

/**
 * Custom exception logger that ensures the exception root cause stack trace is always logged.
 * @author Andrew Cohen
 */
public class ExceptionLogger implements org.springframework.flex.core.ExceptionLogger {

	private final Logger log = Logger.getLogger("ExceptionLogger"); 
	
	@Override
	public void log(Throwable t) {
		Throwable rootCause = getRootCause(t);
		if (rootCause instanceof BusinessValidationException) {
			StringBuffer sb = new StringBuffer("dcTrack Business Validation Exception Stack Trace:\n");
			BusinessValidationException be = (BusinessValidationException)rootCause;
			for (String e : be.getValidationErrors()) {
				sb.append("Error: ");
				sb.append( e );
				sb.append("\n");
			}
			log.error( sb.toString() );
		}
		else {
			log.error("dcTrack System Exception Stack Trace", rootCause);
		}
	}
	
	private Throwable getRootCause(Throwable t) {
		if (t instanceof MessageException) {
			Map<?, ?> data = ((MessageException)t).getExtendedData();
			if (data != null) {
				Throwable exceptionObj = (Throwable)data.get("exceptionObject");
				if (exceptionObj != null) {
					return getRootCause( exceptionObj );
				}
			}
		}
		else if (t instanceof ApplicationBaseException) {
			Throwable cause = ((ApplicationBaseException)t).getExceptionContext().getCause();
			if (cause != null) {
				return getRootCause( cause );
			}
			else {
				return t;
			}
		}
			
		return t;
	}
}
