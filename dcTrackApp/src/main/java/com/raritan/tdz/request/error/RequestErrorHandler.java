package com.raritan.tdz.request.error;

import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */
public interface RequestErrorHandler {

	/**
	 * handle errors collected when processing a request
	 * @param requestMessage
	 */
	public void handleError(RequestMessage requestMessage);
}
