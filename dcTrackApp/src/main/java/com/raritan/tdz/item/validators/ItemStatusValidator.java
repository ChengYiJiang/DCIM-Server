package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;

public class ItemStatusValidator implements Validator {
	
	@Autowired
	private ItemDAO itemDAO;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		if (target instanceof Map) {

			Map<String,Object> targetMap = (Map<String,Object>) target;
			
			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) throw new IllegalArgumentException ("Item cannot be null");
			
			UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
			if (userInfo == null) throw new IllegalArgumentException ("UserInfo cannot be null");
			
			List<Long> itemIdList = itemDAO.getItemIdsToDelete(item.getItemId());
			
			//Add the current item id into the list.
			itemIdList.add(item.getItemId());
			
			// you can delete only those items in 'installed' state
			verifyItemStatusBeforeDeletion(itemIdList,errors);
		} else {
			throw new IllegalArgumentException ("You must provide a Map of Item and UserInfo for ItemStatus Validation");
		}
	}

	private void verifyItemStatusBeforeDeletion(List<Long> itemIdList, Errors errors) {
		
		// you can delete only those items in 'installed' state
		if (itemIdList.size() > 0){
			List<String> itemNameList = itemDAO.getItemsToDeleteNotNew(itemIdList);
			if(itemNameList.size() > 0){
				StringBuffer itemNames = getFormattedItemNameList(itemNameList);
				Object args[]  = {itemNameList.size(), itemNames.toString() };
				String code = "ItemValidator.deleteNotNew";
				errors.reject(code, args, "Cannot delete item. This is not in Planned item");
			}
		}
	}	
	
	private StringBuffer getFormattedItemNameList(List<String> itemNameList) {
		StringBuffer itemNames = new StringBuffer();

		if (itemNameList.size() > 0) {
			int count =0;
			
			for(String s:itemNameList){
				itemNames.append("\t").append(s).append("\n");
				
				if(itemNames.length() > 500 || (count == 12 && itemNameList.size() > 12)){
					itemNames.append("..........");
					break;
				}
				count++;								
			}
		}
		return itemNames;
	}
}
