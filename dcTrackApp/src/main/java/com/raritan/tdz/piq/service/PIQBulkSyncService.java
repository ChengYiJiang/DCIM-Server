package com.raritan.tdz.piq.service;

import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.piq.dto.PIQBulkSyncStatusDTO;

/**
 * Service for running a job which adds/updates all items from dcTrack into PIQ
 * as well as getting detailed status on a currently running job.
 * @author Andrew Cohen
 */
public interface PIQBulkSyncService {

	/**
	 * Start the process of updating Power IQ with all items in dcTrack.
	 * @param ipAddress - ip address of the piq to update
	 * @throws ServiceLayerException
	 */
	public PIQBulkSyncStatusDTO updatePIQData(String ipAddress) throws ServiceLayerException;
	
	/**
	 * Stops the current running job.
	 * @param ipAddress - ip address of the piq to stop update
	 * @return
	 * @throws ServiceLayerException
	 */
	public PIQBulkSyncStatusDTO stopPIQDataUpdate(String ipAddress) throws ServiceLayerException;
	
	/**
	 * Get status of PIQ sync job.
	 * @param ipAddress - ip address of the piq to status
	 * @return
	 */
	public PIQBulkSyncStatusDTO getPIQUpdateDataStatus(String ipAddress) throws ServiceLayerException;
}