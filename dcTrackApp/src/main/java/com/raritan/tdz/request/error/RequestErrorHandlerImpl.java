package com.raritan.tdz.request.error;

import java.util.List;

import com.raritan.tdz.request.home.RequestMessage;

/**
 * Calls error handler in the list
 * @author bunty
 *
 */
public class RequestErrorHandlerImpl implements RequestErrorHandler {

	List<RequestErrorHandler> errorHandlers;
	
	
	
	public List<RequestErrorHandler> getErrorHandlers() {
		return errorHandlers;
	}



	public void setErrorHandlers(List<RequestErrorHandler> errorHandlers) {
		this.errorHandlers = errorHandlers;
	}



	@Override
	public void handleError(RequestMessage requestMessage) {
		
		if (null == errorHandlers || errorHandlers.size() == 0) return;
		
		for (RequestErrorHandler errorHandler: errorHandlers) {
			
			errorHandler.handleError(requestMessage);
			
		}

	}

}
