package com.raritan.tdz.service.auth;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.home.auth.AuthHome;

public class AuthServiceImpl implements AuthService {

	private AuthHome authHome;
	
	public AuthServiceImpl(AuthHome authHome) {
		this.authHome = authHome;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public UserInfo authenticate(String username, String password)
			throws ServiceLayerException {
		return authHome.authenticate(username, password);
	}
}
