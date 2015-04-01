/**
 * 
 */
package com.raritan.tdz.interceptor;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.http.HttpStatus;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.SessionTimeoutException;
import com.raritan.tdz.session.DCTrackSessionManager;
import com.raritan.tdz.session.DCTrackSessionManagerInterface;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.session.RESTAPIUserSessionContext;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

import flex.messaging.FlexContext;
import flex.messaging.messages.Message;

/**
 * This is an aspect that intercepts every service call coming in from client to check if the session is timed out or not
 * @author prasanna
 *
 */
@Aspect
public class SessionTimeoutCheckInterceptor implements MessageInterceptor{
	
	Logger log = Logger.getLogger(getClass());
	
	private final int neverSessionTimeout = 5256000; //This is in minutes.
	
	private boolean isStartupForAMF = true;
	private boolean isStartupForREST = true;
	
	@Autowired
	private DCTrackSessionManagerInterface dcTrackSessionMgr;
	
	private boolean ignoreSession = false;
	
//	TODO: Remove this commented code when code is working fine with the flex message interceptor methods
//	//@Pointcut("execution(public * com.raritan.tdz.*.service.*Service.*(..))")
//	@Pointcut("execution(public * com.raritan.tdz.interceptor.FlexRequestInterceptor.*(..))")
//	public void isAMFCall() {}
	
	
	@Pointcut("execution(public * com.raritan.tdz.controllers.*.*Controller.*(..))")
	public void isRESTCall() {}
	
	@Pointcut("execution(public * com.raritan.tdz.controllers.html.*.checkSession(..))")
	public void isCheckSessionCall() {}
	
	public void init(){
		this.ignoreSession = Boolean.parseBoolean(System.getProperty("dcTrack.ignoreSession"));
	}
	
	@Override
	public Message postProcess(MessageProcessingContext context,
			Message inputMessage, Message outputMessage) {
		return outputMessage;
	}


	@Override
	public Message preProcess(MessageProcessingContext context,
			Message inputMessage) {
		if (FlexUserSessionContext.getUser() != null){
			checkSessionTimeout(FlexUserSessionContext.getUser());
			isStartupForAMF = false;
		} else if (ignoreSession){
			return inputMessage;
		} else if (!isStartupForAMF && (FlexContext.getFlexSession() == null && FlexUserSessionContext.getUser() == null)){
			 throw new SessionTimeoutException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass())); 
		}
		return inputMessage;
	}

	
	@Before("isRESTCall() && !isCheckSessionCall()")
	public void checkSessionForRESTAPI(JoinPoint joinPoint) throws Throwable{
		
		try {
			if (RESTAPIUserSessionContext.getUser() != null){
				checkSessionTimeout(RESTAPIUserSessionContext.getUser());
				isStartupForREST = false;
			} else if (ignoreSession){
				return;
			}else if (!isStartupForAMF && (RESTAPIUserSessionContext.getUser() == null)){
				 throw new SessionTimeoutException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass())); 
			}
		} catch (SessionTimeoutException sto){
			throw new DCTRestAPIException(HttpStatus.BAD_REQUEST,sto);
		}
	}
	
	private void checkSessionTimeout(UserInfo user) throws SessionTimeoutException{
		
		 if (dcTrackSessionMgr == null) return;
		 
		 String sessionId = user.getSessionId();
		 
		 // check if session has timed out. If timed out then throw a SessionTimeoutException so that the client can process it.
		 long currentTime = System.currentTimeMillis();
		 Timestamp lastAccessedTimestamp = dcTrackSessionMgr.getAttribute(sessionId, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) != null 
				 ? (Timestamp) dcTrackSessionMgr.getAttribute(sessionId, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) : new Timestamp(currentTime);
		 long lastAccessedTime = lastAccessedTimestamp.getTime();
		 long idleTime = currentTime - lastAccessedTime;
		 long idleTimeInMinutes = idleTime/60000;
		 
		 long sessionTimeout = user.getSessionTimeout()/60000;
		 
		 //If the sessionTimeout is valid then
		 if (sessionTimeout > 0 && sessionTimeout < neverSessionTimeout){
		   //perform the check of idle time with session time
		   if (idleTimeInMinutes > sessionTimeout){
			   log.debug("*********** Yes it has timed out ******************");
			   throw new SessionTimeoutException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()),
					   sessionId, lastAccessedTime, idleTimeInMinutes, sessionTimeout);
		   }
		 }
		
		 log.info("******************************** SETTING LAST ACCESSED TIME ***************************** "+System.currentTimeMillis());
		 // if not just update the lastAccessedtime on the session
		 Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		dcTrackSessionMgr.addAttribute(sessionId, DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED, currentTimestamp);
	}
}
