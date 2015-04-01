package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.item.itemState.RoleValidator;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.user.dao.UsersDAO;

/**
 * check user permission for the circuit
 * @author bunty
 *
 */
public class CircuitPermissionValidator implements Validator {
	@Autowired
	private UsersDAO usersDAO;
	
	@Autowired
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired
	private RoleValidator roleValidatorBase;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {

		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		
		CircuitViewData circuitViewData = (CircuitViewData) targetMap.get("circuitView");
		UserInfo userInfo = (UserInfo) targetMap.get("userInfo");
		
		boolean userPermitted = false;
		
		if(!(userInfo.isViewer())) {
			if (null == circuitViewData) return;
			
			Users circuitUser = usersDAO.getUser(circuitViewData.getCreatedBy());
			LkuData teamLookup = (null != circuitViewData.getTeamId() && circuitViewData.getTeamId() > 0) ? userLookupFinderDAO.findById(circuitViewData.getTeamId()).get(0) : null;
			
			userPermitted = roleValidatorBase.isPermittedUser(userInfo, circuitUser, teamLookup);
		}
		
		if (!userPermitted) {
			
			// Circuit.permission
			Object[] errorArgs = { userInfo.getUserName() };
			errors.rejectValue("circuit", "Circuit.permission", errorArgs, "User " + userInfo.getUserName() + " do not have permission on this circuit.");
			
		}
		
	}

}
