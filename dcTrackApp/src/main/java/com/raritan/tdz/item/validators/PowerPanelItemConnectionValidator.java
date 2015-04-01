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

public class PowerPanelItemConnectionValidator implements Validator {

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
		@SuppressWarnings("unused")
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());

		Long itemId = item.getItemId();
		// UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
		
		// cannot delete items with connection.
		verifyItemConnectionsBeforeDeletion(itemId,errors);

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

	
	private void verifyItemConnectionsBeforeDeletion(Long itemId, Errors errors) {

		// cannot delete item if there are connection
		List<Long> itemIdList = new ArrayList<Long>();
		itemIdList.add(itemId);
		List<String > itemNameList = itemDao.getPowerPanelItemToDeleteConnected(itemId);
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
