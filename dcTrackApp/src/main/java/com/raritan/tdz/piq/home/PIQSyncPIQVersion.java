package com.raritan.tdz.piq.home;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.PIQInfoJSON;

public interface PIQSyncPIQVersion {

	/**
	 * This function gets Power IQ information via Rest call.
	 * @return PIQInfoJSON
	 * @throws RemoteDataAccessException
	 */
	public PIQInfoJSON getPIQInfoJSON() throws RemoteDataAccessException;
	
	/**
	 * This function returns the Power IQ version
	 * @return String
	 * @throws RemoteDataAccessException
	 */
	
	public String getPIQVersion() throws RemoteDataAccessException;
	
	/**
	 * update powerIQ version in appSettings table
	 * @return void
	 */
	public void syncPIQVersion ();
	
}
