/**
 * This class implements functionality to sync the PowerPort (outlet) readings
 * from PowerIQ
 */
package com.raritan.tdz.piq.home;

 import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.port.dao.PowerPortUpdateDAO;

/**
 * @author basker
 *
 */
public class PIQSyncPDUOutletReadingImpl implements PIQSyncReading {
	
	private static Logger log = Logger.getLogger("PIQSyncPDUOutletReadingsImpl");

	@Autowired
	private PIQSyncOutletClient piqSyncOutletClient;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private LocationDAO locationDAO;
	
	@Autowired
	SystemLookupFinderDAO slfDao;
	
	@Autowired
	EventHome eventHome;
	
	public PIQSyncPDUOutletReadingImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncPDUOutletReadings#SyncReadings()
	 */
	@Transactional
	@Override
	public void SyncReading()  {
		PowerPortFinderDAO ppFinderDao = (PowerPortFinderDAO) powerPortDAO;
		PowerPortUpdateDAO ppUpdateDao = (PowerPortUpdateDAO) powerPortDAO;
		
		List<Long> locationIds = locationDAO.getLocationIdByPIQHost(piqSyncOutletClient.getIpAddress());
		
		for (Long locationId:locationIds){
			List<Long> piqIds = ppFinderDao.findPiqId(locationId);
			for (Long id : piqIds) {
				try {
					Double reading = piqSyncOutletClient.getOutletCurrentReading(id);
					ppUpdateDao.updateOutletReading(reading, id, locationId);
				} catch (RemoteDataAccessException e) {
					setEvent(id);
				}
			}
		}
	}
	
	private void setEvent(long piqId) {
		try {
			Event ev = null;
			String EVENT_SOURCE = "dcTrack";
			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
			// TODO: change event_type to PIQ_SYNC_READING after adding entry to System lookup table.
			ev = eventHome.createEvent(createdAt, Event.EventType.PIQ_UPDATE, EventSeverity.WARNING, EVENT_SOURCE);
			ev.setSummary("Unable to get reading for piqId = " + piqId);
			eventHome.saveEvent(ev);
		} catch (DataAccessException e) {
			log.warn("Failed to set event for outlet id " + piqId);
		}
	}
}
