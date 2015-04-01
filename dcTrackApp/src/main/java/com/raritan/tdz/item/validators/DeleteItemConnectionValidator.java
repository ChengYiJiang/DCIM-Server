package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;

public class DeleteItemConnectionValidator implements Validator {

	@Autowired
	private ItemDAO itemDao;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>) target;

		Item item = (Item)targetMap.get(errors.getObjectName());
		if (item == null) throw new IllegalArgumentException ("Item cannot be null");

		// UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
		
		String msg = (String)targetMap.get(String.class.getName());
		if (msg == null) throw new IllegalArgumentException ("operation name (delete, archive, storage) cannot be null");

		Long itemId = item.getItemId();
		
		List<Long> itemIdList = itemDao.getItemIdsToDelete(itemId);
		
		//Add the current item id into the list.
		itemIdList.add(itemId);
		
		// cannot delete items with connection.
		verifyItemConnectionsBeforeDeletion(itemIdList, msg, errors);

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

	
	private void verifyItemConnectionsBeforeDeletion(List<Long> itemIdList, String msg, Errors errors) {
		StringBuilder errmsg = new StringBuilder();
		errmsg.append("Cannot ");
		errmsg.append(msg);
		errmsg.append(" an item as this item is connected to another item(s)");
		// cannot delete item if there are connection
		List<String >itemNameList = itemDao.getItemToDeleteConnected(itemIdList);
		if(itemNameList.size() > 0){
			StringBuffer itemNames = getFormattedItemNameList(itemNameList);
			Object args[]  = {itemNameList.size(), itemNames.toString() };
			String code = "ItemValidator.deleteConnected";
			errors.reject(code, args, errmsg.toString());
		}
	}


}
