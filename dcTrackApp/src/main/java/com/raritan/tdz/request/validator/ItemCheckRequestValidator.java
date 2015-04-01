package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */
public class ItemCheckRequestValidator implements RequestValidator {

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	private ValidateConflictState validateConflictState;
	
	public ValidateConflictState getValidateConflictState() {
		return validateConflictState;
	}

	public void setValidateConflictState(ValidateConflictState validateConflictState) {
		this.validateConflictState = validateConflictState;
	}

	public ItemCheckRequestValidator(
			ValidateConflictState validateConflictState) {

		this.validateConflictState = validateConflictState;
		
	}

	/**
	 * if parent is not included in the request, check if the parent item has no outstanding requests
	 * if all parent's request is included in the requests, then it has to be non-conflicting requests
	 * (for example: for item move: all parent's outstanding request must be either install or move request and no other requests)
	 */
	@Override
	public void validate(RequestMessage requestMessage) throws DataAccessException {
		
		Errors errors = requestMessage.getErrors();
		
		Request request = requestMessage.getRequest();
		List<Request> requests = requestMessage.getRequests();
		UserInfo userInfo = requestMessage.getUserInfo();
		
		Item item = null;
		String itemName = null;
		PowerPortMove moveData = powerPortMoveDAO.getPortMoveDataUsingRequest(request.getRequestId());
		if (null != moveData) {
			item = moveData.getMoveItem();
			itemName = moveData.getOrigItem().getItemName();
		}
		else {
			item = itemDAO.getItem(request.getItemId());
			itemName = item.getItemName();
		}
		
		validateConflictState.validate(item, request, requests, errors, itemName, userInfo);
		
	}
	
}
