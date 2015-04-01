/**
 * 
 */
package com.raritan.tdz.session.dao;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 *
 */
public interface UserSessionDAO {
	/**
	 * Get the userInfo given the sessionId
	 * @param sessionUUID
	 * @return
	 * @throws DataAccessException
	 */
	public UserInfo getUserInfo(String sessionUUID) throws DataAccessException;
}
