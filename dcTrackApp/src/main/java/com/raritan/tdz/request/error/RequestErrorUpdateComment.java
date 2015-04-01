package com.raritan.tdz.request.error;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.request.dao.RequestHistoryDAO;
import com.raritan.tdz.request.home.RequestMessage;

public class RequestErrorUpdateComment implements RequestErrorHandler {

	private static int MAX_REQ_COMMENT_LENGTH = 500;
	
	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;
	
	@Autowired (required=true)
	public ResourceBundleMessageSource messageSource;
	
	
	
	@Override
	public void handleError(RequestMessage requestMessage) {
		
		Errors requestErrors = requestMessage.getErrors();
		
		if (null == requestErrors || !requestErrors.hasErrors()) return;
		
		String currentComment = requestHistoryDAO.getCurrentRequestHistoryComment(requestMessage.getRequest()); 
		
		if (null != currentComment && currentComment.length() >= MAX_REQ_COMMENT_LENGTH) return;
		
		List<ObjectError> objectErrors = requestErrors.getAllErrors();
		for (ObjectError error: objectErrors) {

			String msg = messageSource.getMessage(error, Locale.getDefault());
				
			String newComment = ((null != currentComment) ? (currentComment + "\n") : "") + msg;
				
			if (newComment.length() >= MAX_REQ_COMMENT_LENGTH) break;
				
			requestHistoryDAO.setCurrentRequestHistoryComment(requestMessage.getRequest(), newComment);
				
		}
		
	}

}
