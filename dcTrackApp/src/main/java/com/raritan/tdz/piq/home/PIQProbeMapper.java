package com.raritan.tdz.piq.home;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.exception.RemoteDataAccessException;

/**
 * A class to handle to specialized mapping of Probes between dcTrack and Power IQ.
 * 
 * @author Andrew Cohen
 */
public interface PIQProbeMapper {
	
	/**
	 * Creates or updates a hidden Rack PDU item in dcTrack associated with the specified probe item.
	 * This will trigger a Rack PDU creation/update in Power IQ. We need a rack PDU in Power IQ in
	 * order to collect sensor and asset strip information for the probe.
	 * @param probeItemId the ID of the probe item
	 * @param ipAddress the IP Address of the probe
	 * @return the ID of the PDU probe item in dcT or -1 if could not be created.
	 */
	@Transactional
	public long updateProbeRackPDU(long probeItemId, String ipAddress) throws RemoteDataAccessException;
	
	
	/**
	 * Update the probe's associated dummy Rack PDU when the cabinet is changed.
	 * @param probe the probe item
	 */
	@Transactional
	public void updateProbeRPDUCabinet(long probeItemId);
	
	/**
	 * Queues a probe Rack PDU update request for later.
	 * Intended for use with the "Power IQ" update function.
	 * @param probeItemId the ID of the probe item
	 * @param ipAddress the IP Address of the probe
	 */
	public void queueProbeRequest(long probeItemId, String ipAddress);
	
	/**
	 * Flushes any queued probe requests.
	 * Intended for use with the "Power IQ" update function.
	 */
	public void flushQueuedProbeRequests();
	
	/**
	 * Deletes the dummy Rack PDU associated with the given prove item
	 * @param probeItemId item id of the probe item.
	 * @return true if associated probe RPDU existed and was successfully deleted, otherwise returns false.
	 */
	@Transactional
	public boolean deleteProbeRackPDUForProbeItemId(long probeItemId);
	
	/**
	 * Delete all probe Rack PDU associations that are orphaned 
	 * (i.e., those that reference a non-existent probe AND non-existent Rack PDU)
	 */
	@Transactional
	public void deleteOrphanedProbePDULinks();
	
	/**
	 * 
	 * @param probeItemId
	 * @return
	 */
	@Transactional
	public boolean deleteUnmappedProbePDULink(long probeItemId);
	
	/**
	 * Update the data port associated with a probe rack PDU.
	 * @param probeItemId
	 * @param port
	 */
	@Transactional
	public void updateProbeRackPDUDataPort(long probeItemId, DataPort port, String ipAddress) throws RemoteDataAccessException;
	
	/**
	 * @return the lookup service
	 */
	public PIQProbeLookup getLookup();
}
