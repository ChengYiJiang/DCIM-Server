package com.raritan.tdz.user.home;

import java.math.BigInteger;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

public interface UserHome {
	
	
	/**
	 * get user's request bypass setting from the database 
	 * @param userId
	 * @return True if request bypass is supported or else return false
	 */
	public boolean getUserRequestByPassSetting(long userId);
	
	/**
	 * get user's access level lkp value code.
	 * @param userId
	 * @return BigInteger lkpValueCode
	 */
	public BigInteger getUserAccessLevelLkpValueCode(long userId);
	
	/**
	 * This function returns current settings for request bypass lock 
	 * @return -1 if lock is set else return 0
	 */
	public String getLockRequestByPassButtonSetting ();
	
	/**
	 * Set users request bypass setting.
	 * @param requestBypass
	 * @param userId
	 */
	public void setUserRequestByPassSetting(boolean requestBypass, long userId);
	
	/**
	 * Get Current User Info
	 * @param 
	 */
	public UserInfo getCurrentUserInfo();
	public UserInfo getCurrentUserInfo(String uuid) throws DataAccessException;

}
