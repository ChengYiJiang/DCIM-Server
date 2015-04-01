package com.raritan.tdz.assetstrip.home;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.home.PIQEvent;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Default Asset Event builder implementation.
 *  
 * @author Andrew Cohen
 */
@Transactional(propagation=Propagation.REQUIRED)
public class AssetEventBuilderImpl implements AssetEventBuilder {
	
	private static final Map<Integer, EventType> eventMap;
	
	static {
		eventMap = new HashMap<Integer, Event.EventType>(4);
		eventMap.put(PIQEvent.ASSET_STRIP_CONNECTED, EventType.ASSET_STRIP_CONNECTED);
		eventMap.put(PIQEvent.ASSET_STRIP_REMOVED, EventType.ASSET_STRIP_REMOVED);
		eventMap.put(PIQEvent.ASSET_TAG_CONNECTED, EventType.ASSET_TAG_CONNECTED);
		eventMap.put(PIQEvent.ASSET_TAG_REMOVED, EventType.ASSET_TAG_REMOVED);
	}
	
	private Logger log = Logger.getLogger(this.getClass());
	private SessionFactory sessionFactory;
	
	public AssetEventBuilderImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List<Event> buildAssetEvents(List<PIQEvent> piqEvents) {
		List<Event> events = new LinkedList<Event>();
		for (PIQEvent piqEvent : piqEvents) {
			try {
				events.add( buildAssetEvent(piqEvent) );
			}
			catch (DataAccessException e) {
				log.warn("Problem creating asset event: " + piqEvent, e);
			}
		}
		return events;
	}
	
	/**
	 * Builds and persists a new dcTrack asset event.
	 * @param piqEvent
	 * @return
	 * @throws DataAccessException
	 */
	private Event buildAssetEvent(PIQEvent piqEvent) throws DataAccessException {
		Event ev = null;
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			Timestamp createdAt = new Timestamp(piqEvent.getCreatedAt().getTime());
			
			// Create the asset event with default severity level
			ev = Event.createEvent(session, createdAt, eventMap.get(piqEvent.getEventConfigId()), EVENT_SOURCE);
			
			// Set cleared info
			Date clearedAt = piqEvent.getClearedAt();
			if (clearedAt != null) {
				ev.clear( null );
			}
			
			// Add asset tag event parameters
			AssetEventParam.PDU_ID.addToEvent(ev, piqEvent.getPduId());
			AssetEventParam.ASSET_STRIP_ID.addToEvent(ev, piqEvent.getAssetStripId());
			AssetEventParam.NOTIFICATION_STATUS.addToEvent(ev, piqEvent.getNotificationStatus());
			
			if (piqEvent.getRackUnitId() != null) {
				AssetEventParam.ASSET_TAG_RACKUNIT_ID.addToEvent(ev, piqEvent.getRackUnitId());
			}
			
			Map<String, String> piqParams = piqEvent.getParams();
			for (String key : piqParams.keySet()) {
				AssetEventParam param = AssetEventParam.get( key );
				
				if (param != null) {
					param.addToEvent(ev, piqParams.get(key) );
				}
				else {
					log.warn("Unrecognized PIQ asset parameter: " + key);
				}
			}
			
			session.save( ev );
		}
		catch (HibernateException ex) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CREATION_FAILED, this.getClass(), null));
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CREATION_FAILED, this.getClass(), null));
		}
		
		return ev;
	}
}
