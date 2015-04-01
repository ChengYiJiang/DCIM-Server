package com.raritan.tdz.user.home;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.session.dao.UserSessionDAO;
import com.raritan.tdz.user.dao.UserDAO;

public class UserHomeImpl implements UserHome{	
	@Autowired
	UserDAO userDAO;

	@Autowired
	private UserSessionDAO userSessionDAO;

	public UserHomeImpl() {
		
	}

	@Override
	@Transactional(readOnly = true)
	public boolean getUserRequestByPassSetting(long userId) {
		return userDAO.getUserRequestByPassSetting(new Long(userId));
	}

	@Override
	@Transactional(readOnly = true)
	public BigInteger getUserAccessLevelLkpValueCode(long userId) {
		return userDAO.getUserAccessLevelLkpValueCode(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public String getLockRequestByPassButtonSetting() {
		return userDAO.getLockRequestByPassButtonSetting();
	}

	@Override
	@Transactional
	public void setUserRequestByPassSetting(boolean requestBypass, long userId) {
		userDAO.setUserRequestByPassSetting(requestBypass, userId);
	}

	@Override
	public UserInfo getCurrentUserInfo() {
		return FlexUserSessionContext.getUser();
	}

	@Override
	public UserInfo getCurrentUserInfo(String uuid) throws DataAccessException {
		return userSessionDAO.getUserInfo(uuid);
	}

}
