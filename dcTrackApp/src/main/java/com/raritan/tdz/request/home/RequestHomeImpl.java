package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.user.dao.UserDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.RequestDTO;

public class RequestHomeImpl implements RequestHome {

	@Autowired(required=true)
	private RequestManager requestManager;

	@Autowired
	RequestDAO requestDAO;
	
	@Autowired
	BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired
	ItemRequestDAO itemRequestDAO;
	
	@Autowired
	UserDAO userDAO;
	
	@Autowired
	RequestHelper requestHelper;
	
	public RequestManager getRequestManager() {
		return requestManager;
	}

	public void setRequestManager(RequestManager requestManager) {
		this.requestManager = requestManager;
	}

	public BusinessExceptionHelper getBusinessExceptionHelper() {
		return businessExceptionHelper;
	}

	public void setBusinessExceptionHelper(
			BusinessExceptionHelper businessExceptionHelper) {
		this.businessExceptionHelper = businessExceptionHelper;
	}

	// @Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public void processRequestDTO(UserInfo userInfo, List<RequestDTO> requestDTOs, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException {

		List<Request> requests = requestHelper.getRequestFromDTO(requestDTOs);
		
		/*
		 * Collect all the error messages from the request issue stage. Filter all request success messages
		 * and create an error object. Pass this error object to the request manager to further update 
		 * additional errors if any.
		 *  
		 * If request bypass is enabled use the errors from the request manager and DO NOT throw any exception.
		 * The error information is available via the request progress
		 * 
		 * If request bypass is disabled, use the exception from
		 * the request issue stage for this release and throw the exception.
		 */
		
		processRequests(userInfo, requests, bvex);
		
	}

	/*@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)*/
	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRED)
	@Override
	public void processRequests(UserInfo userInfo, List<Request> requests, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException {

		/*
		 * Collect all the error messages from the request issue stage. Filter all request success messages
		 * and create an error object. Pass this error object to the request manager to further update 
		 * additional errors if any.
		 *  
		 * If request bypass is enabled use the errors from the request manager and DO NOT throw any exception.
		 * The error information is available via the request progress
		 * 
		 * If request bypass is disabled, use the exception from
		 * the request issue stage for this release and throw the exception.
		 */
		Errors errors = businessExceptionHelper.getErrorObject(Request.class);
		@SuppressWarnings("deprecation")
		List<String> requestIssueMsgs = (null != bvex) ? bvex.getValidationErrors() : new ArrayList<String>();
		
		for (String requestIssueMsg: requestIssueMsgs) {
			if (requestIssueMsg.contains("Request regenerated successfully") || requestIssueMsg.contains("Request Issued successfully")) continue;
			
			Object[] errorArgs = { requestIssueMsg };
			errors.rejectValue("request", "Request.IssueFailedFakeCode", errorArgs, requestIssueMsg);
		}

		boolean requestBypassSetting = requestHelper.getRequestBypassSetting(userInfo);
		requestManager.process(requests, userInfo, errors, requestBypassSetting);
		
		if (!requestBypassSetting) {
			if (null != bvex) throw bvex;
		}
		else {
			if (!errors.hasErrors()) {
				if (null != bvex) throw bvex;
			}
		}
	}

	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public Errors processRequestUsingIds(UserInfo userInfo, List<Long> requestIds) throws BusinessValidationException, DataAccessException {
		List<Request> requests = requestDAO.getRequests(requestIds);
		
		Errors errors = businessExceptionHelper.getErrorObject(Request.class);
		
		boolean requestBypassSetting = userDAO.getUserRequestByPassSetting(new Long(userInfo.getUserId()));
		requestManager.process(requests, userInfo, errors, requestBypassSetting);

		return errors;
		
	}
	
	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRED)
	@Override
	public void processRequests(UserInfo userInfo, List<Request> requests, Errors errors) throws BusinessValidationException, DataAccessException {
		
		if (null == errors) {
			errors = businessExceptionHelper.getErrorObject(Request.class);
		}
		
		boolean requestBypassSetting = userDAO.getUserRequestByPassSetting(new Long(userInfo.getUserId()));
		requestManager.process(requests, userInfo, errors, requestBypassSetting);

		if (errors.hasErrors() && !requestBypassSetting) {
			businessExceptionHelper.throwBusinessValidationException(null, errors, null);
		}
	}

}
