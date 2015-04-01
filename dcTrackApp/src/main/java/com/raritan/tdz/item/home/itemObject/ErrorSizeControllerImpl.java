package com.raritan.tdz.item.home.itemObject;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.ObjectError;

public class ErrorSizeControllerImpl implements ErrorSizeController {

	private Map<String, Integer> errorCodeMaxLength;
	
	private Map<String, Integer> errorCodeMaxLine;
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;
	
	public ErrorSizeControllerImpl(Map<String, Integer> errorCodeMaxLength,
			Map<String, Integer> errorCodeMaxLine) {
		super();
		this.errorCodeMaxLength = errorCodeMaxLength;
		this.errorCodeMaxLine = errorCodeMaxLine;
		
	}

	@Override
	public String getMessage(ObjectError error) {
		
		// Get message
		String msg = getErrorMessage(error);
		
		// Set the message to the max length
		msg = setLength(error.getCode(), msg);
		
		// Set the message to the max line
		msg = setLine(error.getCode(), msg);
		
		return msg;
	}
	
	
	private String getErrorMessage(ObjectError error) {
		String msg = messageSource.getMessage(error, Locale.getDefault());
		
		return msg;
	}
	
	private String setLength(String errorCode, String msg) {
	
		if (null == msg) return msg;
		
		if (!errorCodeMaxLength.containsKey(errorCode)) return msg;
		
		int maxLength = errorCodeMaxLength.get(errorCode);
		
		String msgNoNewLine = msg.trim();
		
		if (msgNoNewLine.length() <= maxLength) return msg;
		
		StringBuffer msgBuf = new StringBuffer()
		.append(msg.substring(0, maxLength))
		.append("...\n\n");
	
		return msgBuf.toString();
		
	}
	
	private String setLine(String errorCode, String msg) {
		
		if (null == msg) return msg;
		
		if (!errorCodeMaxLine.containsKey(errorCode)) return msg;
		
		int maxLines = errorCodeMaxLine.get(errorCode);
		
		int maxLineIndex = StringUtils.ordinalIndexOf(msg, "\n", maxLines);
		
		if (maxLineIndex <= 0) return msg;
		
		StringBuffer msgBuf = new StringBuffer()
			.append(msg.substring(0, maxLineIndex))
			.append("\n...\n\n");
		
		return msgBuf.toString();
		
	}
	
	
	
}
