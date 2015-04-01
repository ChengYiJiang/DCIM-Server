package com.raritan.tdz.unit.tests;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.domain.cmn.Users;

public class UserMock {
	
	public static UserInfo getUserInfo(UserInfo.UserAccessLevel accessLevel){
		UserInfo userInfo = new UserInfo();
		userInfo.setAccessLevelId(new Integer(accessLevel.getAccessLevel()).toString());
		return userInfo;
	}
	  
	public static Users getUser(UserInfo.UserAccessLevel accessLevel){
		Users user = new Users();
		user.setAccessLevelId(new Integer(accessLevel.getAccessLevel()).toString());
		return user;
	}
	
	public static UserInfo getTestAdminUser(String userId, String userName, UserAccessLevel userAccess ) {
		UserInfo user = new UserInfo();
		user.setUserName(userName);
		user.setUserId(userId);
		user.setAccessLevelId( Integer.toString( userAccess.getAccessLevel() ) );
		return user;
	}

	public static UserInfo getTestAdminUser() {
		return getTestAdminUser("1", "AdminUser", UserAccessLevel.ADMIN);
	}	
}
