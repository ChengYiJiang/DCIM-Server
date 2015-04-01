package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * 
 * @author bunty
 *
 */

public interface RequestManager {

	/**
	 * process set the request (requests) for the user (userInfo)
	 * Read the bypass information from the database or it could be provided in the userInfo
	 * @param requests
	 * @param userInfo
	 * @param errors
	 * @param requestByPass TODO
	 */
	public void process(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass);

	/**
	 * process the reply mesage from the request stage executors
	 * @param requestMessage
	 */
	public void processMessage(RequestMessage requestMessage) throws BusinessValidationException;

	/**
	 * process all related request for a given request
	 * @param request
	 * @param requests
	 * @param userInfo
	 * @param errors
	 * @param requestByPass
	 */
	public void processRelatedRequests(Request request, List<Request> requests,
			UserInfo userInfo, Errors errors, Boolean requestByPass);

	/**
	 * handle the request message at the first entry point
	 * @param request
	 * @param allRequests
	 * @param errors
	 * @param requestByPass
	 * @param userInfo
	 */
	public void handleRequest(Request request, List<Request> allRequests,
			Errors errors, Boolean requestByPass, UserInfo userInfo);

}
