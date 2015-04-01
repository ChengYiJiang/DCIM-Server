package com.raritan.tdz.user.dao;

import java.math.BigInteger;

public interface UserDAO {
	
	// GKCanToggleRequestBypass="0" indicate GK has permission to enable/disable RB.
	public static final String GKCanToggleRequestBypass = "0";
	
	/**
	 * get user's request bypass setting from the database 
	 * @param userId
	 * @return True if request bypass is supported or else return false
	 */
	public Boolean getUserRequestByPassSetting(long userId);
	
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
}
