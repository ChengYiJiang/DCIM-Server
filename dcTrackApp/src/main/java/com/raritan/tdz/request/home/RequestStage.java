package com.raritan.tdz.request.home;

import com.raritan.tdz.domain.Request;

/**
 * 
 * @author bunty
 *
 */

public interface RequestStage {

	/**
	 * process the request 
	 * @param request
	 */
	public void process(Request request, Error error);

	/**
	 * process the request message
	 * @param requestMessage
	 * @return TODO
	 * @throws Throwable 
	 */
	public RequestMessage process(RequestMessage requestMessage) throws Throwable;
	
}
