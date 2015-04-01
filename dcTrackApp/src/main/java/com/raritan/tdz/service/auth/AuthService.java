package com.raritan.tdz.service.auth;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * Authentication Services interface.
 * @author Andrew Cohen
 *
 */
public interface AuthService {
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ServiceLayerException
	 */
	public UserInfo authenticate(String username, String password) throws ServiceLayerException;

}
