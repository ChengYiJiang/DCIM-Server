package com.raritan.tdz.home.auth;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Interface to dcTrack authentication services.
 * @author Andrew Cohen
 */
public interface AuthHome {
	
	/**
	 * Authenticates a username and password.
	 * @param username
	 * @param password
	 * @return
	 */
	public UserInfo authenticate(String username, String password) throws DataAccessException;
}