package com.raritan.tdz.user.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.cmn.Users;

/**
 * DAO for the Users table "users"
 * @author bunty
 *
 */
public interface UsersDAO extends Dao<Users> {

	/**
	 * get the users information against the user id
	 * @param userId
	 * @return
	 */
	public Users getUser(String userId);
	
}
