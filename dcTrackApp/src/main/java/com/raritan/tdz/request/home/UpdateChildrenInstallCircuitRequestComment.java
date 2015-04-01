package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.request.dao.RequestHistoryDAO;

public class UpdateChildrenInstallCircuitRequestComment implements
		RequestStageHelper {

	private static int MAX_REQ_COMMENT_LENGTH = 500;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo)
			throws Throwable {
		
		
		String currentComment = requestHistoryDAO.getCurrentRequestHistoryComment(request); 
		
		if (null != currentComment && currentComment.length() >= MAX_REQ_COMMENT_LENGTH) return;
		
		Long parentItemId = request.getItemId();
		
		List<Long> childrenItemIds = itemDAO.getChildItemIds(parentItemId);
		
		List<Long> passiveChildren = itemDAO.getPassiveChildItemIds(parentItemId);
		
		childrenItemIds.removeAll(passiveChildren);
		
		if (childrenItemIds.size() == 0) return;
		
		StringBuffer comment = new StringBuffer().
				append((currentComment != null) ? currentComment : "").
				append("The following associated items have installed circuits:\n");

		StringBuffer itemList = new StringBuffer();
		
		for (Long itemId: childrenItemIds) {
		
			// int numOfCircuits = itemDAO.getNumOfAssociatedNonPlannedNonRequestCircuitsForItem(itemId);
			// int numOfCircuits = itemDAO.getAssociatedCircuitsCountForItem(itemId);
			int numOfCircuits = itemDAO.getNumOfAssociatedNonPlannedForItem(itemId);
			
			if (numOfCircuits > 0) {
				
				itemList.append("'" + itemDAO.getItemName(itemId) + "' ");
				
			}
			
		}
		
		if (itemList.length() == 0) return;
		comment.append(itemList);
		
		if (comment.toString().length() >= MAX_REQ_COMMENT_LENGTH) {
			comment.setLength(MAX_REQ_COMMENT_LENGTH);
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 1, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 2, '.');
			comment.setCharAt(MAX_REQ_COMMENT_LENGTH - 3, '.');
		}
		requestHistoryDAO.setCurrentRequestHistoryComment(request, comment.toString());

	}

}
