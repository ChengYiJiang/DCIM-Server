package com.raritan.tdz.move.validator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;
import com.raritan.tdz.request.validator.RequestValidator;

public class ItemMoveUniquenessValidator implements RequestValidator {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	private Validator itemUniquenessValidator;
	
	public Validator getItemUniquenessValidator() {
		return itemUniquenessValidator;
	}

	public void setItemUniquenessValidator(Validator itemUniquenessValidator) {
		this.itemUniquenessValidator = itemUniquenessValidator;
	}

	public ItemMoveUniquenessValidator(Validator itemUniquenessValidator) {
		
		this.itemUniquenessValidator = itemUniquenessValidator;
	}

	@Override
	public void validate(RequestMessage requestMessage)
			throws DataAccessException, ClassNotFoundException {

		if (null == itemUniquenessValidator) return;
		
		Request request = requestMessage.getRequest();
		
		PowerPortMove moveData = powerPortMoveDAO.getPortMoveDataUsingRequest(request.getRequestId());
		if (null == moveData) return;
		Item movingItem = moveData.getMoveItem();
		Item origItem = moveData.getOrigItem();

		if (null == movingItem || null == origItem) return;

		Errors errors = requestMessage.getErrors();
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		// edit the name of the the moving item to the original item and then validate the uniqueness
		String movingItemName = movingItem.getItemName();
		movingItem.setItemName(origItem.getItemName());
		
		// prepare the arguments for validation
		targetMap.put(errors.getObjectName(), movingItem);
		targetMap.put(UserInfo.class.getName(), requestMessage.getUserInfo());
		
		// validate
		itemUniquenessValidator.validate(targetMap, errors);
		
		// put the name of the moving item name back
		movingItem.setItemName(movingItemName);
		
	}

}
