/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.validation.Errors;

/**
 * @author prasanna
 *
 */
public interface ImportErrorHandler {
	public void handleException(Message<MessagingException> message);

	public void handleLineErrors(Errors errors);
	public void handleLineWarnings(Errors warnings);
	
	public List<String> getErrors();
	public List<String> getWarnings();
	
	public void clearErrors();
	public void clearWarnings();
}
