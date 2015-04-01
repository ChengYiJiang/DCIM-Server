package com.raritan.tdz.move.home;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.dao.RequestHistoryDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;

public class ItemMoveRequestCommentBehavior implements ItemSaveBehavior {

	private static int MAX_REQ_COMMENT_LENGTH = 500;
	
	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired
	private RequestHistoryDAO requestHistoryDAO;
	
	@Autowired
	private RequestDAO requestDAO;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		Errors errors = businessExceptionHelper.getErrorObject(this.getClass());
		
		Errors placementErrors = itemMoveHelper.getPlacementInMoveCabinetError(item, errors, true);
		
		Errors placementChassisErrors = itemMoveHelper.getPlacementInMoveChassisError(item, errors, true);
		
		placementErrors.addAllErrors(placementChassisErrors);
		
		if (!placementErrors.hasErrors()) return;
		
		String errorMsg = businessExceptionHelper.getMessage(placementErrors);
		if (null == errorMsg || errorMsg.length() == 0) return;
		
		List<Request> itemMoveRequest = requestDAO.getPendingRequestsForItem(Arrays.asList(item.getItemToMoveId()), Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_MOVE));

		if (null == itemMoveRequest || itemMoveRequest.size() != 1) return;
		
		Request request = itemMoveRequest.get(0);
		
		String currentComment = requestHistoryDAO.getCurrentRequestHistoryComment(request); 
		
		if (null != currentComment && currentComment.length() >= MAX_REQ_COMMENT_LENGTH) return;
		
		StringBuffer comment = new StringBuffer().
				append((null != currentComment) ? currentComment : "").
// 			append("The following requests exist for associated items:\n").
				append(errorMsg);
		
		if (comment.toString().length() >= MAX_REQ_COMMENT_LENGTH) {
			comment.setLength(MAX_REQ_COMMENT_LENGTH);
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 1, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 2, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 3, '.');
		}
		
		requestHistoryDAO.setCurrentRequestHistoryComment(request, comment.toString());

		
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}

}
