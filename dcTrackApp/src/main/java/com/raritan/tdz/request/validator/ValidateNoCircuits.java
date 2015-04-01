package com.raritan.tdz.request.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;

public class ValidateNoCircuits implements RequestValidator {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	
	@Override
	public void validate(RequestMessage requestMessage)
			throws DataAccessException, ClassNotFoundException {

		Errors errors = requestMessage.getErrors();
		
		Request request = requestMessage.getRequest();
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
		

		
		int circuitCount = itemDAO.getNumOfAssociatedNonPlannedNonRequestCircuitsForItem(request.getItemId());
		
		if (circuitCount > 0) {
			
			Object[] errorArgs = { itemName, null, null, null, request.getRequestNo(), request.getDescription() };
			errors.rejectValue("item", "Request.CannotDecommission.circuitsNotRemoved", errorArgs, "Item request cannot be completed because its parent has conflicting pending request.");
			return;
			
		}


	}

}
