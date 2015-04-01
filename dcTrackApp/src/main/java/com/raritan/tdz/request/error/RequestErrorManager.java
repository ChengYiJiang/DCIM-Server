package com.raritan.tdz.request.error;

import com.raritan.tdz.request.home.RequestMessage;

/**
 * Request error manager, handles errors encountered while processing request work flow
 * @author bunty
 *
 */
public interface RequestErrorManager {

	/**
	 * handles all the errors encountered when the request was being processed
	 * @param requestMessage
	 */
	public void error(RequestMessage requestMessage);
	
}
