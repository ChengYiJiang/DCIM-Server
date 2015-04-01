package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;

public class PermissionValidator implements Validator {
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object target, Errors errors) {
		
		if (target instanceof Map) {
		
			Map<String,Object> targetMap = (Map<String,Object>) target;
			
			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) throw new IllegalArgumentException ("Item cannot be null");
			
			UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
			if (userInfo == null) throw new IllegalArgumentException ("UserInfo cannot be null");
			
			String msg = (String)targetMap.get(String.class.getName());
			if (msg == null) throw new IllegalArgumentException ("operation name (delete, archive, storage) cannot be null");
			
			List<Long> itemIdList = itemDAO.getItemIdsToDelete(item.getItemId());
			
			//Add the current item id into the list.
			itemIdList.add(item.getItemId());
			
			// verify user permission for deleting child items
			verifyDeleteItemPermission(itemIdList, userInfo, msg, errors);
		} else {
			throw new IllegalArgumentException ("You must provide a Map of Item and UserInfo for item permission validation");
		}
	}
	
	//Private methods
	private boolean verifyDeleteItemPermission(List<Long> itemIdList, UserInfo userInfo, String msg, Errors errors) {

		// check if user has permission to delete children in itemIdList
		for (Long id: itemIdList) {
			if (id != null) verifyDeleteItemPermission (id, userInfo, msg, errors);
		}
		return true;
	}
	
	private boolean verifyDeleteItemPermission(long itemId, UserInfo userInfo, String msg, Errors errors) {

		Item item = null;
		
		if (itemId <= 0) {
			if (userInfo.isViewer()) {
				Object args[] = {userInfo != null ? userInfo.getUserName() : "", msg};
				String code = "ItemValidator.deleteAccessDenied";
				errors.reject(code, args, "You do not have permission to " + msg);
				return false;
			}
			return true;
		}
			
		item = (Item)itemDAO.getItem(itemId);
		
		if (item == null) {
			Object args[] = {itemId, msg};
			String code = "ItemValidator.deleteInvalidItem";
			errors.reject(code, args, "This is an invalid item. Cannot " + msg);
		}
	
		// verify if user has permission to delete items
		if (itemModifyRoleValidator.canTransition(item, userInfo) == false) {
			Object args[] = {userInfo != null ? userInfo.getUserName() : "", msg};
			String code = "ItemValidator.deleteAccessDenied";
			errors.reject(code, args, "You do not have permission to " + msg);
		}
		return true;
	}

}
