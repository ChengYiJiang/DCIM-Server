package com.raritan.tdz.request.home;

import org.springframework.integration.annotation.Router;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author bunty
 *
 */

public class RequestRouter {

	@Router
	public String validatorRouter(RequestMessage requestMessage) throws DataAccessException {
		
		return getInitialChannel(requestMessage) + "ValidateRouter";
		
	}

	@Router
	public String executorRouter(RequestMessage requestMessage) throws DataAccessException {
		
		return getInitialChannel(requestMessage) + "ExecuteRouter";
		
	}
	
	private String getInitialChannel(RequestMessage requestMessage) throws DataAccessException {

		Request request = requestMessage.getRequest();
		
		// TODO:: change the hard-coded ITEM_MOVE to the new lkp value code for the "request type lkp value code"
		return new Long(request.getRequestTypeLookup().getLkpValueCode()).toString() + ".";
		
	}


}
