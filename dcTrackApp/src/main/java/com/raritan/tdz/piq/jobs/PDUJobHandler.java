package com.raritan.tdz.piq.jobs;

import java.util.Map;

import org.apache.log4j.Logger;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.piq.json.JobJSON.Job;

/**
 * Handles Job statuses for newly added PDUs by updating the piq_id of the PDU item in dcTrack.
 * @author Andrew Cohen
 */
public class PDUJobHandler implements PIQJobHandler {

	private static Logger log = Logger.getLogger( PDUJobHandler.class );
	
	private EventHome eventHome;
	private PIQSyncPDUClient pduClient;
	
	public PDUJobHandler(EventHome eventHome, PIQSyncPDUClient pduClient) throws DataAccessException {
		this.eventHome = eventHome;
		this.pduClient = pduClient;
	}
	
	@Override
	public void onJobComplete(Job job, Map<String, Object> jobData) {
		log.debug("Job completed");
		updatePiqId(job, jobData);
	}

	@Override
	public void onJobError(Job job, Map<String, Object> jobData) {
		log.debug("Job has errors");
		updatePiqId(job, jobData);
		
		// Add additional information about this PDU to the event
		Long eventId = (Long)jobData.get("errorEventId");
		if (eventId != null) {
			Event ev = null;
			try {
				ev = eventHome.getEventDetail( eventId );
			} 
			catch (DataAccessException e) {
				log.error("", e);
				ev = null;
			}
			
			if (ev != null) {
				Item pduItem = (Item)jobData.get("pduItem");
				
				// Event summary
				ev.setSummary( eventHome.getMessageSource().getMessage(
						"piqUpdate.pduDiscoveryFailed",
						new Object[] { pduItem.getItemName(), jobData.get("ipAddress"), pduItem.getGroupingNumber() },
						null)
				);
				
				// Event details
				ev.addParam("PDU Name", pduItem.getItemName());
				Item cabinet = pduItem.getParentItem();
				if (cabinet != null) {
					ev.addParam("Cabinet", cabinet.getItemName());
					DataCenterLocationDetails loc = cabinet.getDataCenterLocation();
					if (loc != null) {
						ev.addParam("Location", loc.getDcName());
					}
				}
			}
		}
	}

	@Override
	public void onJobUpdate(Job job, Map<String, Object> data) {
		log.debug("Updated job");
	}
	
	//
	// Start private methods
	//
	
	private void updatePiqId(Job job, Map<String, Object> jobData) {
		String ipAddress = (String)jobData.get("ipAddress");
		
		if (ipAddress == null) {
			log.error("IP Address is null for PIQ job " +  job.getId());
			return;
		}
		
		Item pduItem = (Item)jobData.get("pduItem");
		if (pduItem == null) {
			log.error("PDU item is null for PIQ job " +  job.getId());
			return;
		}
		
		Integer pduId;
		try {
			if (pduItem.getGroupingNumber() != null)
				pduId = pduClient.lookupByIPAddressAndProxyIndex( ipAddress, pduItem.getGroupingNumber() );
			else
				pduId = pduClient.lookupByIPAddress( ipAddress );
		} 
		catch (RemoteDataAccessException e) {
			log.warn("PIQ PDUs search call failed for ipAddress " + ipAddress + " and Proxy Index is " + pduItem.getGroupingNumber());
			pduId = null;
		}
		
		if (pduId != null) {
			jobData.put("piqId", pduId);
		}
		else {
			if (!job.getHasErrors()) {
				log.warn("Could not find PDU for IP Address " + ipAddress + " and Proxy Index is " + pduItem.getGroupingNumber());
			}
		}
	}
}
