package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;

public class ItemStageValidator implements Validator {
	
	@Autowired
	private ItemDAO itemDAO;

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
			
			String msg = (String) targetMap.get(String.class.getName());
			if (msg == null) throw new IllegalArgumentException ("operation name (delete, archive, storage) cannot be null");
			
			Boolean includeOtherDeletingItems = (Boolean) targetMap.get(Boolean.class.getName());
			if (null == includeOtherDeletingItems) includeOtherDeletingItems = true;
			
			List<Long> itemIdList = null;
			if (includeOtherDeletingItems) {
				itemIdList = itemDAO.getItemIdsToDelete(item.getItemId());
			}
			else {
				itemIdList = new ArrayList<Long>();
			}
			
			//Add the current item id into the list.
			itemIdList.add(item.getItemId());
			
			// cannot delete item in request_approved, workorder_issued, workorder_complete states
			verifyItemStagesBeforeDeletion(itemIdList, msg, errors);
		} else {
			throw new IllegalArgumentException ("You must provide a Map of Item and UserInfo for Item Stage Validation");
		}
	}

	private boolean verifyItemStagesBeforeDeletion(List<Long> itemIdList, String msg, Errors errors){

		// check if item stage allowes modification
		List<String> itemNameList = itemDAO.getItemsToDeleteInvalidStages (itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer errMsg = new StringBuffer();
			errMsg.append("Cannot ");
			errMsg.append(msg);
			errMsg.append(" item as there is a request pending.");
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString(), msg };
			String code = "ItemValidator.itemDeleteRequestStageNotAllowed";
			errors.reject(code, args, errMsg.toString());
		}
		return true;
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
