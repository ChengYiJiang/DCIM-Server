/**
 * 
 */
package com.raritan.tdz.dctimport.integration.exceptions;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dctimport.dto.ImportStatusDTO;

/**
 * @author prasanna
 * This handler will be the central handler where all the errors/warnings are collected
 * This will be used by the ImportStatusDTO to send back the errors/warnings to the client.
 */
public class ImportErrorHandlerImpl implements ImportErrorHandler{
	
	private final Logger logger = Logger.getLogger("dcTrackImport");
	
	private Errors errorsObject = new MapBindingResult(new HashMap<String,String>(),"importErrors");
	private Errors warningsObject = new MapBindingResult(new HashMap<String,String>(),"importWarnings");
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private AtomicInteger importLineNumber;
	
	
	private Map<String,ImportExceptionHandler> exceptionHandlers;
	
	public ImportErrorHandlerImpl(Map<String,ImportExceptionHandler> exceptionHandlers){
		this.exceptionHandlers = exceptionHandlers;
	}
	
	private final static HashMap<String, String> exceptionCodeMap = new HashMap<String, String>(){{
		put(FileNotFoundException.class.getName(), "Import.fileNotFound");
	}};
	
	
	public void handleException(Message<MessagingException> message){
		Throwable finalCause = null;
		Throwable cause = message.getPayload().getCause();
		do {
			finalCause = cause;
			cause = cause.getCause();
		} while (cause != null);
		
		
		ImportExceptionHandler exceptionHandler = exceptionHandlers.get(finalCause.getClass().getName());
		if (exceptionHandler != null){
			exceptionHandler.handleException(finalCause, (Object[])null);
			if (exceptionHandler.getErrors() != null){
				handleErrors(exceptionHandler.getErrors());
			} else if (exceptionHandler.getWarnings() != null){
				handleWarnings(exceptionHandler.getWarnings());
			} else {
				synchronized(this) {
					errorsObject.reject("Import.system.exception");
				}
				logger.fatal(finalCause.getMessage());
				finalCause.printStackTrace();
			}
		} else {
			synchronized(this) {
				errorsObject.reject("Import.system.exception");
			}
			logger.fatal(finalCause.getMessage());
			finalCause.printStackTrace();
		}
		
		MessageHeaders headers = message.getHeaders();
		if (headers.containsKey("replyChannel")){
			MessageChannel replyChannel = (MessageChannel) headers.get("replyChannel");
			replyChannel.send(message);
		}
	}

	@Override
	public List<String> getErrors() {
		List<String> errorMessages = new ArrayList<String>();
		synchronized(this) {
			if (errorsObject.hasErrors()){
			
				List<ObjectError> objectErrors = errorsObject.getAllErrors();
				for (ObjectError error:objectErrors){
					errorMessages.add(messageSource.getMessage(error, Locale.getDefault()));
				}
			}
		}
		return errorMessages;
	}

	@Override
	public List<String> getWarnings() {
		List<String> warningMessages = new ArrayList<String>();
		synchronized(this) {
			if (warningsObject.hasErrors()){
				List<ObjectError> objectErrors = warningsObject.getAllErrors();
				for (ObjectError error:objectErrors){
					warningMessages.add(messageSource.getMessage(error, Locale.getDefault()));
				}
			}
		}
		
		return warningMessages;
	}

	@Override
	public synchronized void clearErrors() {
		errorsObject = new MapBindingResult(new HashMap<String,String>(),"importErrors");
	}

	@Override
	public synchronized void clearWarnings() {
		warningsObject = new MapBindingResult(new HashMap<String,String>(),"importWarnings");
	}

	@Override
	public void handleLineErrors(Errors errors) {
		synchronized(this) {
			if (errors != null && errors.hasErrors()){
				for (ObjectError error:errors.getAllErrors()){
					Object[] args = {importLineNumber.get(),messageSource.getMessage(error, Locale.getDefault())};
					errorsObject.reject("Import.Line.Error", args, error.getDefaultMessage());
				}
			}
		}
	}

	@Override
	public void handleLineWarnings(Errors warnings) {
		synchronized(this) {
			if (warnings != null && warnings.hasErrors()){
				for (ObjectError warning:warnings.getAllErrors()) {
					Object[] args = {importLineNumber.get(), messageSource.getMessage(warning, Locale.getDefault())};
					warningsObject.reject("Import.Line.WARNING", args, warning.getDefaultMessage());
				}
			}
		}
	}
	

	private void handleErrors(Errors errors) {
		synchronized(this) {
			if (errors != null && errors.hasErrors()){
				for (ObjectError error:errors.getAllErrors())
					errorsObject.reject(error.getCode(), error.getArguments(), error.getDefaultMessage());
			}
		}
	}

	private void handleWarnings(Errors errors) {
		synchronized(this) {
			if (errors != null && errors.hasErrors()){
				for (ObjectError error:errors.getAllErrors())
					warningsObject.reject(error.getCode(), error.getArguments(), error.getDefaultMessage());
			}
		}
		
	}


}
