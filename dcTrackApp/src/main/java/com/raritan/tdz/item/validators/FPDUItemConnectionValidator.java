package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;

public class FPDUItemConnectionValidator implements Validator {

	@Autowired
	private ItemDAO itemDao;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if (target instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String,Object> targetMap = (Map<String,Object>) target;
			if (targetMap.size() != 3) throw new IllegalArgumentException("Invalid number of arguments provided for FPDU item connection validator");
			
			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) throw new IllegalArgumentException("Item cannot be null");
			
			UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
			if (userInfo == null) throw new IllegalArgumentException("UserInfo cannot be null");
	
			@SuppressWarnings("unused")
			Long itemId = item.getItemId();
			
			// cannot delete items with connection.
			verifyItemConnectionsBeforeDeletion(item, errors);
		} else {
			throw new IllegalArgumentException();
		}

	}

	private StringBuffer getFormattedItemNameList(List<String> itemNameList) {
		StringBuffer itemNames = new StringBuffer();
		int MAX_ITEM_NAME_LENGTH_IN_ERR_MSG_ALLOWED = 500;
		int MAX_ITEM_NAMES_IN_ERR_MSG = 12;

		if (itemNameList.size() > 0) {
			int count = 0;
			
			for(String s:itemNameList){
				itemNames.append("\t").append(s).append("\n");
				
				if(itemNames.length() > MAX_ITEM_NAME_LENGTH_IN_ERR_MSG_ALLOWED ||
						(count == MAX_ITEM_NAMES_IN_ERR_MSG && itemNameList.size() > MAX_ITEM_NAMES_IN_ERR_MSG)) {
					itemNames.append("..........");
					break;
				}
				count++;								
			}
		}
		return itemNames;
	}
	
	private void verifyItemConnectionsBeforeDeletion(Item item, Errors errors) {

		// cannot delete item if there are connection
		Long itemId = item.getItemId();
		
		List<String> itemNameList = itemDao.getFPDUItemToDeleteConnected(itemId);
		if(itemNameList.size() > 0) {
			/*Object args[]  = { };
			String code = "ItemValidator.deleteFPDUWithPanelPowerOutletConnected";
			errors.reject(code, args, "Cannot delete item because power outlets are circuited to panel's breaker port");*/
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString() };
			String code = "ItemValidator.deleteConnected";
			errors.reject(code, args, "Cannot delete item as this item is connected with other items");
		}
	}

}
