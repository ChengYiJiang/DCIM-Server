package com.raritan.tdz.request.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;

public class ValidatePermission implements Validator {

	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;
	

	@Autowired
	private ItemDAO itemDAO;

	
	
	public ItemModifyRoleValidator getItemModifyRoleValidator() {
		return itemModifyRoleValidator;
	}


	
	public void setItemModifyRoleValidator(
			ItemModifyRoleValidator itemModifyRoleValidator) {
		this.itemModifyRoleValidator = itemModifyRoleValidator;
	}


	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		
		Long itemId = (Long) targetMap.get(Item.class.getName());
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());

		validate(itemId, userInfo, errors);
		
	}

	public void validate(Long itemId, UserInfo userInfo, Errors errors) {

		Item item = itemDAO.loadItem(itemId);
		String itemName = item.getItemName();
		
		if (!isPermitted(userInfo, item)){
			Object[] errorArgs = {itemName};
			errors.reject("itemRequest.isViewer", errorArgs, "Cannot submit request as you do not have permissions");
		}
		
	}
	
	private boolean isPermitted(UserInfo userInfo, Item item){
		//Actually we are not doing anything with Transition here. All we are 
		//doing is to check if the current user has permission to perform this operation
		//on the given item.
		//TODO: May be we should consider naming the method properly.
		return (itemModifyRoleValidator.canTransition(item, userInfo));
	}

}
