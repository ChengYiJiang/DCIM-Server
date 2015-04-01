package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RequestValidationException;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.error.RequestErrorManager;
import com.raritan.tdz.request.progress.RequestProgressUpdate;
import com.raritan.tdz.request.validator.RequestUnderProgress;
import com.raritan.tdz.request.validator.RequestValidateManager;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.ExceptionContext;

/**
 * 
 * @author bunty
 *
 */
public class RequestManagerImpl implements RequestManager {

	@Autowired(required=true)
	protected ResourceBundleMessageSource messageSource;

	private RequestValidateManager validateManager;
	
	private RequestExecuteManager executeManager;
	
	private RequestErrorManager errorManager;
	
	private RequestProgressUpdate requestProgressUpdate;
	
	@Autowired(required=true)
	private RequestHelper requestHelper;
	
	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired(required=true)
	BusinessExceptionHelper businessExceptionHelper;
	
	private boolean myErrors = false;

	private List<Request> retryRequests;
	
	private Validator requestListValidator;
	
	public Validator getRequestListValidator() {
		return requestListValidator;
	}

	public void setRequestListValidator(Validator requestListValidator) {
		this.requestListValidator = requestListValidator;
	}

	
	public RequestValidateManager getValidateManager() {
		return validateManager;
	}



	public void setValidateManager(RequestValidateManager validateManager) {
		this.validateManager = validateManager;
	}



	public RequestExecuteManager getExecuteManager() {
		return executeManager;
	}



	public void setExecuteManager(RequestExecuteManager executeManager) {
		this.executeManager = executeManager;
	}



	public RequestProgressUpdate getRequestProgressUpdate() {
		return requestProgressUpdate;
	}



	public void setRequestProgressUpdate(RequestProgressUpdate requestProgressUpdate) {
		this.requestProgressUpdate = requestProgressUpdate;
	}



	public RequestErrorManager getErrorManager() {
		return errorManager;
	}



	public void setErrorManager(RequestErrorManager errorManager) {
		this.errorManager = errorManager;
	}



	public BusinessExceptionHelper getBusinessExceptionHelper() {
		return businessExceptionHelper;
	}



	public void setBusinessExceptionHelper(
			BusinessExceptionHelper businessExceptionHelper) {
		this.businessExceptionHelper = businessExceptionHelper;
	}



	public RequestManagerImpl(RequestValidateManager validateManager,
			RequestExecuteManager executeManager, 
			RequestErrorManager errorManager, 
			RequestProgressUpdate requestProgressUpdate) {

		this.validateManager = validateManager;
		this.executeManager = executeManager;
		this.errorManager = errorManager;
		this.requestProgressUpdate = requestProgressUpdate;
		this.retryRequests = new ArrayList<Request>();
		
	}

	private void processRetryRequest(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) {
		
		List<Request> tryAgainRequests = new ArrayList<Request>(retryRequests);
		
		retryRequests.clear();
		
		for (Request request: tryAgainRequests) {
			
			if (null == request) continue;

			processRelatedRequests(request, requests, userInfo, errors, requestByPass);
		}

		// if same errors are encountered or when all requests are complete, we are done.
		if (retryRequests.size() == tryAgainRequests.size() || retryRequests.size() == 0) return;
		
		processRetryRequest(requests, userInfo, errors, requestByPass);
		
	}
	
	private void infromInternalErrors(Errors errors, UserInfo userInfo) {

		if (!errors.hasErrors()) return;
			
		List<String> requests = new ArrayList<String>();
		List<ObjectError> objectErrors = errors.getAllErrors();
		for (ObjectError error: objectErrors) {
			
			// Process only errors caused because of system error
			if (error.getCode().equals("itemRequest.processBypassFailed")) {
				
				// extract the request number
				String msg = messageSource.getMessage(error, Locale.getDefault());
				msg = msg.substring(msg.lastIndexOf(":") + 1).trim();
				String request = msg.substring(0, msg.indexOf("."));
				
				// collect all requests having system errors
				if (!requests.contains(request)) 	requests.add(request);
				
			}
		}
		
		if (requests.size() > 0) {
			Errors sysErrors = getErrorObject(errors);
			
			sysErrors.rejectValue("request", "Request.addLineSeperator", "");
			
			// frame the request list
			StringBuffer requestMsg = new StringBuffer();
			for (String request: requests) {
				requestMsg.append("\n");
				requestMsg.append("Request: ");
				requestMsg.append(request);
			}
			
			// print the error message
			Object[] errorArgs = { requestMsg.toString() };
			sysErrors.rejectValue("request", "itemRequest.processBypassFailedInformation", errorArgs, "The following request failed due to system error: " + requests.toString());
			
			requestProgressUpdate.updateErrors(sysErrors, userInfo);
		}

	}

	@Override
	public void process(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) {
		
		if (null == errors) {
			errors = getErrorObject();
			myErrors = true;
		}
		
		Errors reqListErrors = getErrorObject(errors);
		validateRequestList(requests, reqListErrors);
		
		if (reqListErrors.hasErrors()) {
			
			requestProgressUpdate.updateErrors(reqListErrors, userInfo);
			
			return;  
		}
		
		try {
			
			this.retryRequests.clear();
			
			//Do disconnect request first			
			for (Request dr: requests) {				
				if (null == dr || dr.isDisconnectReq() == false) continue;
	
				processRelatedRequests(dr, requests, userInfo, errors, requestByPass);				
			}				
						
			for (Request request: requests) {				
				if (null == request || request.isDisconnectReq()) continue;
	
				processRelatedRequests(request, requests, userInfo, errors, requestByPass);
				
			}
			
			if (retryRequests.size() > 0) {
				Errors retryErrors = getErrorObject(errors);
				
				Object[] errorArgs = { "\nRetrying the failed request(s)..." };
				retryErrors.rejectValue("request", "Request.IssueFailedFakeCode", errorArgs, "\nRetrying the failed request(s)...");
				
				requestProgressUpdate.updateErrors(retryErrors, userInfo);
			}
			
			processRetryRequest(requests, userInfo, errors, requestByPass);
			
			infromInternalErrors(errors, userInfo);
			
			this.retryRequests.clear();
		
		}
		finally {
			
			clearRequestUnderProgress(requests);
			
		}
		
	}
	
	@Override
	public void processRelatedRequests(Request request, List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) {
		
		requestProgressUpdate.nextRequestStart(request, errors, userInfo);
		
		List<Request> otherRequests = requestHelper.getAssociatedRequests(request); // requestDAO.getAssociatedPendingReqsForReq(request);			
		List<Request> allRequests = mergeRequests(requests, otherRequests);
		
		//Remove requests that are in the request list already to avoid double processing
		otherRequests = removeRequests(requests, otherRequests);
		
		//Process all disconnect first 
		for (Request r: otherRequests) {
			if(r.isDisconnectReq()) {
				try {
					requestHelper.handleRequest(this, r, allRequests, errors, requestByPass, userInfo);
				} catch (Throwable t) { t.printStackTrace(); };
				
			}
		}

		try
		{
			//	Process main request
			requestHelper.handleRequest(this, request, allRequests, errors, requestByPass, userInfo);
			
		}
		catch (Throwable t) { t.printStackTrace(); }; 

		//Process of type of requests
		for (Request r: otherRequests) {
			if(r.isDisconnectReq()) continue;
			try {
			requestHelper.handleRequest(this, r, allRequests, errors, requestByPass, userInfo);
			} catch (Throwable t) { t.printStackTrace(); };
			
		}

	}
	
	@Override
	public void handleRequest(Request request, List<Request> allRequests, Errors errors, Boolean requestByPass, UserInfo userInfo) {
		
		Errors requestErrors = getErrorObject(errors);
		
		Request req = requestDAO.getRequest(request.getRequestId());
		
		RequestMessage requestMessage = RequestMessageBuilder.createMessagePayload(req, allRequests, requestByPass, requestErrors, userInfo);
		
		try {
			
			handleMessageAtFirstStage(requestMessage);
			
		} catch (RequestValidationException e) {

			errors.addAllErrors(requestErrors);
			
			errorManager.error(requestMessage);
			
		}
		
	}
	
	// @Transactional(propagation=Propagation.REQUIRES_NEW)
	private void handleMessageAtFirstStage(RequestMessage requestMessage) throws RequestValidationException {
		
		try {

			requestProgressUpdate.nextRequestHeader(requestMessage.getRequest(), requestMessage.getErrors(), requestMessage.getUserInfo());
			requestProgressUpdate.updateStageCompleteMessage(requestMessage.getRequest(), requestMessage.getErrors(), requestMessage.getUserInfo());
			
			handleMessage(requestMessage, true);
			
		} catch (BusinessValidationException e) {
			
			throw new RequestValidationException(new ExceptionContext(ApplicationCodesEnum.REQUEST_EDIT_FAIL, this.getClass(),null));
			
		}

		if (requestMessage.getErrors().hasErrors()) {
		
			throw new RequestValidationException(new ExceptionContext(ApplicationCodesEnum.REQUEST_EDIT_FAIL, this.getClass(),null));
			
		}
		else {
			
			requestProgressUpdate.updateStageCompleteMessage(requestMessage.getRequest(), requestMessage.getErrors(), requestMessage.getUserInfo());
		}
		
	}
	
	@Override
	public void processMessage(RequestMessage requestMessage) throws BusinessValidationException {
		
		handleMessage(requestMessage, false);
	}
	
	private void handleMessage(RequestMessage requestMessage, boolean validate) throws BusinessValidationException {

		if (validate) validateManager.validate(requestMessage);
		
		Errors errors = requestMessage.getErrors();

		// It is possible that the validation failed because the order of the request matters, 
		// it is worth retrying to complete the request again.
		addRetryRequest(requestMessage);
		
		if (!errors.hasErrors()) 
		{
			// executeManager.process(request, errors);
			executeManager.process(requestMessage);

			// if during the execution of the request, the updaters throws an exception, it is quite possible it failed
			// because of the request order. It is worth trying to process the request again.
			addRetryRequest(requestMessage);
			
			Boolean requestBypass = requestMessage.getRequestByPass();
			if (null == requestBypass || !requestBypass) return;
			
		}
		else {
			
			if (myErrors) {
				throwBusinessValidationException(errors, null);
			}
		}

	}
	
	private boolean retryRequestHasRequest(Request request) {
		for (Request reTryRequest: retryRequests) {
			if (reTryRequest.getRequestId() == request.getRequestId())
				return true;
		}
		
		return false;
	}
	
	private void addRetryRequest(RequestMessage requestMessage) {

		Errors errors = requestMessage.getErrors();
		errors = requestMessage.getErrors();
		if (errors.hasErrors()) {
			
			// if (!retryRequests.contains(requestMessage.getRequest())) {
			if (!retryRequestHasRequest(requestMessage.getRequest())) {
				retryRequests.add(requestMessage.getRequest());
			}
		}

	}
	
	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}

	private MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, refErrors.getObjectName() );
		return errors;
		
	}

	private void throwBusinessValidationException(Errors errors, String warningCallBack) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}
	
	private List<Request> mergeRequests(List<Request> list1, List<Request> list2){
		if(list1.size() == 0) return list2;
		if(list2.size() == 0) return list1;
		
		List<Request> list3 = new ArrayList<Request>();
		list3.addAll(list2);
		
		for(Request r1:list1){
			for(Request r2:list2){
				if(r1.getRequestId() == r2.getRequestId()){
					list3.remove(r2);
				}
			}
		}
		
		//merge
		list3.addAll(0, list1);
		
		return list3;
	}

	private List<Request> removeRequests(List<Request> list1, List<Request> list2){
		if(list1.size() == 0 || list2.size() == 0) return list2;
		
		List<Request> list3 = new ArrayList<Request>();
		list3.addAll(list2);
		
		for(Request r1:list1){
			for(Request r2:list2){
				if(r1.getRequestId() == r2.getRequestId()){
					list3.remove(r2);
				}
			}
		}
		
		return list3;
	}
	
	private void validateRequestList(List<Request> requests, Errors errors) {
		 
		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		targetMap.put(List.class.getName(), requests);
		
		requestListValidator.validate(targetMap, errors);
		
	}
	
	private void clearRequestUnderProgress(List<Request> requests) {

		for (Request request:requests) {
			
			RequestUnderProgress.clearRequest(request.getRequestId());
		}

	}


}
