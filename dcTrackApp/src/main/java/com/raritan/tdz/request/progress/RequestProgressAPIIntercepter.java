package com.raritan.tdz.request.progress;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;

public class RequestProgressAPIIntercepter {

	private static Logger log = Logger.getLogger("RequestProgress");
	
	private RequestProgressUpdate requestProgressUpdate;

	public RequestProgressAPIIntercepter(
			RequestProgressUpdate requestProgressUpdate) {
		super();
		this.requestProgressUpdate = requestProgressUpdate;
	}

	public void createRequestProgress(JoinPoint joinPoint) throws InstantiationException, IllegalAccessException {
		
		if (log.isDebugEnabled()) log.debug("intercepted method : " + joinPoint.getSignature().getName());
		
		Object[] args = joinPoint.getArgs();
		@SuppressWarnings("unchecked")
		List<Request> requests = (List<Request>) args[0]; 
		UserInfo userInfo = (UserInfo) args[1]; 
		Errors errors = (Errors) args[2]; 

		requestProgressUpdate.startProgress(requests, userInfo, errors);
		
	}
	
	/*@After("execution(* com.raritan.tdz.request.home.RequestManager.process(..))")*/
	public void completeRequestProgress(JoinPoint joinPoint) {
		
		if (log.isDebugEnabled()) log.debug("intercepted method : " + joinPoint.getSignature().getName());
		
		Object[] args = joinPoint.getArgs();
		UserInfo userInfo = (UserInfo) args[1];
		Errors errors = (Errors) args[2]; 
		
		requestProgressUpdate.endProgress(userInfo, errors);
		
	}
	
	public void updateRequestNumber(JoinPoint joinPoint) {
		
		if (log.isDebugEnabled()) log.debug("intercepted method : " + joinPoint.getSignature().getName());
		
		Object[] args = joinPoint.getArgs();
		Request request = (Request) args[0]; 
		UserInfo userInfo = (UserInfo) args[2]; 
		Errors errors = (Errors) args[3];
		
		requestProgressUpdate.nextRequestStart(request, errors, userInfo);
		
	}

	public void updateRequestErrors(JoinPoint joinPoint) {
		
		if (log.isDebugEnabled()) log.debug("intercepted method : " + joinPoint.getSignature().getName());
		
		Object[] args = joinPoint.getArgs();
		UserInfo userInfo = (UserInfo) args[2]; 
		Errors errors = (Errors) args[3]; 
		
		if (null == errors || !errors.hasErrors()) return;
		
		requestProgressUpdate.updateErrors(errors, userInfo);
		
	}

	
}
