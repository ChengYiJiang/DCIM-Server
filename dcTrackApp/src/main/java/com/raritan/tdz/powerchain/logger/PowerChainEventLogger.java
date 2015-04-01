package com.raritan.tdz.powerchain.logger;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.events.dao.EventDAO;

public class PowerChainEventLogger implements PowerChainLogger {

	Logger log = Logger.getLogger(getClass());
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;
	
	@Autowired(required=true)
	private EventDAO eventDAO;
	
	private ThreadPoolTaskExecutor dctExecutor;
	
	public ThreadPoolTaskExecutor getDctExecutor() {
		return dctExecutor;
	}

	public void setDctExecutor(ThreadPoolTaskExecutor dctExecutor) {
		this.dctExecutor = dctExecutor;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void log(Errors errors) {
		
		// Generate events for all the errors in the background
		dctExecutor.submit( new EventLogAllErrors(errors) );
		
		return;
	}

	/**
	 * A runnable task to clear all active events in the event log.
	 */
	private class EventLogAllErrors implements Runnable {
		
		Errors errors;
		
		public EventLogAllErrors(Errors errors) {
			this.errors = errors;
		}

		@Override
		public void run() {
			logEventUsingErrors(errors);
		}
	}

	private void logEventUsingErrors(Errors errors) {
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
  				String itemName = null;
  				/*
  				// Plan is to put the item name as the last argument in the error object
  				Object[] args = error.getArguments();
  				if (null != args && args.length > 0) {
  					itemName = args[args.length - 1].toString();
  				}
  				*/
  				eventDAO.generatePowerChainEvent(msg, itemName);
			}
		}
		
		// Collect circuit errors, this will run in the background
		
		
	}

}
