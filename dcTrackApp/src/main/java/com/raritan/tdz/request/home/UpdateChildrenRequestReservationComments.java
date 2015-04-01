package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.move.home.ItemMoveHelper;
import com.raritan.tdz.request.dao.RequestHistoryDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;

public class UpdateChildrenRequestReservationComments implements
		RequestStageHelper {

	private static int MAX_REQ_COMMENT_LENGTH = 500;
	
	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;

	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo)
			throws Throwable {

		Errors errors = businessExceptionHelper.getErrorObject(this.getClass());
		
		Errors childReservationErrors = itemMoveHelper.getChildrenReservationErrors(request.getItemId(), errors);
		
		String errorMsg = businessExceptionHelper.getMessage(childReservationErrors);

		if (null == errorMsg || errorMsg.length() == 0) return;
		
		String currentComment = requestHistoryDAO.getCurrentRequestHistoryComment(request); 
		
		if (null != currentComment && currentComment.length() >= MAX_REQ_COMMENT_LENGTH) return;
		
		StringBuffer comment = new StringBuffer().
				append((null != currentComment) ? currentComment : "").
				append(errorMsg);
		
		if (comment.toString().length() >= MAX_REQ_COMMENT_LENGTH) {
			comment.setLength(MAX_REQ_COMMENT_LENGTH);
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 1, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 2, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 3, '.');
		}
		
		requestHistoryDAO.setCurrentRequestHistoryComment(request, comment.toString());


	}

}
