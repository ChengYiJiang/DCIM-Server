package com.raritan.tdz.move.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;

public class ItemMoveEditValidator implements Validator {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());

		if (item.getSkipValidation() != null && item.getSkipValidation()) return;
		
		if (item.getItemId() > 0) return;
		
		if (null == item.getItemToMoveId()) return;
		
		String classPostfix = (null != item.getClassLookup()) ? item.getClassLookup().getLkpValue() : "";

		String errorCode = "ItemMoveValidator.MoveItemEdited" + classPostfix;
		
		String origItemName = null;
		if (item.getItemId() <= 0) {
			origItemName = itemDAO.getItemName(item.getItemToMoveId());
		}
		else {
			origItemName = powerPortMoveDAO.getMovingItemName(item.getItemId());
		}
		Object[] errorArgs = { origItemName };
		errors.rejectValue("item", errorCode, errorArgs, "Edit Move Item?");

	}

}
