package com.raritan.tdz.item.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;

public class DeleteMovedItemValidator implements Validator {

	private List<Validator> validators; // refer: validators.xml 
	 
	public List<Validator> getValidators() {
		return validators;
	}

	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}

	private ItemRequestDAO itemRequestDAO;

	public ItemRequestDAO getItemRequestDAO() {
		return itemRequestDAO;
	}

	public void setItemRequestDAO(ItemRequestDAO itemRequestDAO) {
		this.itemRequestDAO = itemRequestDAO;
	}
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;


	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		if (!(target instanceof Map)) {
			throw new IllegalArgumentException("You must provide a Map of item and userInfo for Item delete validation");
		}
		
		// TODO:: get the original item against the moved item
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>) target;

		Item item = (Item)targetMap.get(errors.getObjectName());
		if (item == null) throw new IllegalArgumentException ("Item cannot be null");

		if (item.getItemId() <= 0) return;
		
		// Long movedItemId = itemRequestDAO.getMovingItemId(item.getItemId());
		Long movedItemId = powerPortMoveDAO.getMovingItemId(item.getItemId());
		if (null == movedItemId) return;
		
		Item originalItem = itemDAO.getItem(movedItemId);
		if (null == originalItem) return;
		
		Map<String,Object> movingItemTargetMap = new HashMap<String, Object>();
		movingItemTargetMap.putAll(targetMap);
		
		// Update the target map to have the original item 
		// targetMap.remove(errors.getObjectName());
		movingItemTargetMap.put(errors.getObjectName(), originalItem);
		
		// do not include all the deleting item(s). Validate the stage of only the moving item
		movingItemTargetMap.put(Boolean.class.getName(), false);
		
		for (Validator v: validators) {
			v.validate(movingItemTargetMap, errors);
		}
		
	}

}
