package com.raritan.tdz.location.validators;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.UserInfo;

/**
 * check editability user permission for the location
 * @author bunty
 *
 */
public class LocationPermissionValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
		
		validateUserPermission(userInfo, errors);
		
	}
	
	private void validateUserPermission(UserInfo sessionUser, Errors errors) {
		if (sessionUser == null){
			errors.reject("locationValidator.noPermission");
			return;
		}
		
		if (!sessionUser.isAdmin()){
			errors.reject("locationValidator.noPermission");
			return;
		}
	}


}
