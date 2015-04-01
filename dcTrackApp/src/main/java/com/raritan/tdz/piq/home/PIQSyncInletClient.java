package com.raritan.tdz.piq.home;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.Inlet;

/**
 * An interface for synchronizing inlet readings into dcTrack with those in PIQ.
 * @author basker
 */
public interface PIQSyncInletClient extends PIQRestClient {

	/**
	 * Find a specific inlet on a PDU in PIQ.
	 * @param pduPiqId the PIQ ID of the PDU
	 * @param inletNumber - the inlet number respective to other inlet on the same PDU
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public Inlet findInlet(long pduPiqId, int inletNumber) throws RemoteDataAccessException;

}
