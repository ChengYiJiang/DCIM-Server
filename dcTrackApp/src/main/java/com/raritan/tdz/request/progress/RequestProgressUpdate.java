package com.raritan.tdz.request.progress;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;

/**
 * request progress update interface
 * @author bunty
 *
 */
public interface RequestProgressUpdate {

	/**
	 * starts the progress report
	 * @param requests
	 * @param userInfo
	 * @param errors TODO
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void startProgress(List<Request> requests, UserInfo userInfo, Errors errors) throws InstantiationException, IllegalAccessException;
	
	/**
	 * ends the progress report
	 * @param userInfo
	 * @param refErrors TODO
	 */
	public void endProgress(UserInfo userInfo, Errors refErrors);
	
	/**
	 * clears the progress report
	 * @param userInfo
	 */
	public void cleanProgress(UserInfo userInfo);
	
	/**
	 * processing the next request starts, update the progress
	 * @param request
	 * @param refErrors TODO
	 * @param userInfo
	 */
	public void nextRequestStart(Request request, Errors refErrors, UserInfo userInfo);
	
	/**
	 * update the request stage progress string
	 * @param request TODO
	 * @param requestStage
	 * @param userInfo
	 */
	public void updateRequestStage(Request request, Long requestStage, UserInfo userInfo);
	
	/**
	 * update the errors generated when validating request
	 * @param errors
	 * @param userInfo
	 */
	public void updateErrors(Errors errors, UserInfo userInfo);

	/**
	 * get the request progress dto
	 * @param userInfo
	 * @return RequestProgressDTO
	 */
	public RequestProgressDTO getDto(UserInfo userInfo);

	/**
	 * update the progress when the stage is completed
	 * @param request
	 * @param refErrors
	 * @param userInfo
	 */
	public void updateStageCompleteMessage(Request request, Errors refErrors,
			UserInfo userInfo);

	/**
	 * informs if the request processing is active (in progress) for the given user session 
	 * @param userInfo
	 * @return
	 */
	public boolean active(UserInfo userInfo);

	/**
	 * set the request header 
	 * @param request
	 * @param refErrors
	 * @param userInfo
	 */
	public void nextRequestHeader(Request request, Errors refErrors, UserInfo userInfo);
	
}
