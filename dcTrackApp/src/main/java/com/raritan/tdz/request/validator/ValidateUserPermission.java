package com.raritan.tdz.request.validator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.request.home.RequestMessage;

public class ValidateUserPermission implements RequestValidator {

	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;
	
	@Autowired
	private ItemDAO itemDAO;
	
	private Validator permissionValidator;
	
	
	public ValidateUserPermission(Validator permissionValidator) {
		super();
		this.permissionValidator = permissionValidator;
	}

	public ItemModifyRoleValidator getItemModifyRoleValidator() {
		return itemModifyRoleValidator;
	}

	public void setItemModifyRoleValidator(
			ItemModifyRoleValidator itemModifyRoleValidator) {
		this.itemModifyRoleValidator = itemModifyRoleValidator;
	}

	@Override
	public void validate(RequestMessage requestMessage)
			throws DataAccessException, ClassNotFoundException {
		
		Long itemId = requestMessage.getRequest().getItemId();
		UserInfo userInfo = requestMessage.getUserInfo();
		Errors errors = requestMessage.getErrors();
		
		Map<String, Object> targeMap = new HashMap<String, Object>();
		targeMap.put(Item.class.getName(), itemId);
		targeMap.put(UserInfo.class.getName(), userInfo);
		
		permissionValidator.validate(targeMap, errors);
		
	}
	

}
