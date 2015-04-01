package com.raritan.tdz.request.home;

import com.raritan.tdz.request.progress.RequestProgressDTO;

/**
 * 
 * @author bunty
 *
 */
public interface RequestService {

	/**
	 * get the progress information for the work flow request for a given session
	 * @return
	 */
	public RequestProgressDTO getRequestProgress();

	/**
	 * clean the progress dto information for a given session
	 */
	public void cleanRequestProgressDTO();
	
}
