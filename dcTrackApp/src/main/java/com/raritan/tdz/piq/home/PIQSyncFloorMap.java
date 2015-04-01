/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.*;
import org.springframework.integration.annotation.Payload;

import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * This interface defines sync of floor map data to PowerIQ
 * @author prasanna
 *
 */
public interface PIQSyncFloorMap {

	/**
	 * Upload a floor map file given a powerIQ host.
	 * <p><b>Note:</b> piqHost can be obtained via location as each location has a corresponding powerIQ host</p>
	 * @param piqHost PowerIQ host to upload the file
	 * @param filePath Path of the file to be uploaded.
	 * @throws RemoteDataAccessException
	 */
	//public void uploadFloorMap(String piqHost,String filePath) throws RemoteDataAccessException;
	public Map uploadFloorMap(String piqHost, String filePath, String piqId, String httpUsername, String httpPassword) throws RemoteDataAccessException;
}