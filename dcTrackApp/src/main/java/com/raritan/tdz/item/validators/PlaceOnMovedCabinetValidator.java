package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.move.dao.PortMoveDAO;

public class PlaceOnMovedCabinetValidator implements Validator {

	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.getSuperclass().equals(Item.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item) targetMap.get(errors.getObjectName());
		
		if (null == item || item.getParentItem() == null) return;
		
		Long movingItem = powerPortMoveDAO.getMovingItemId(item.getParentItem().getItemId());
		
		if (null == movingItem || movingItem <= 0) return;

		Object[] errorArgs = { item.getParentItem().getItemName() };
		errors.rejectValue("cmbCabinet", "ItemValidator.cannotPlaceOnMovedCabinet", errorArgs, "Cannot place item in the move cabinet " + item.getParentItem().getItemName() );		
		
	}

}
