package com.raritan.tdz.piq.home;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.exceptions.PIQUpdateException;
import com.raritan.tdz.piq.json.ErrorJSON;

/**
 * A service for logging various PIQ update related warnings and errors to the system event log.
 * This bean is (and must be) thread scoped to allow tracking duplicate exceptions. 
 * @author Andrew Cohen
 */
public class PIQSystemEventLoggerImpl implements PIQSystemEventLogger {
	
	private static final String EVENT_SOURCE = "Power IQ";
	
	/**
	 * A set of item IDs that we've already logged PIQ Updates on.
	 */
	private Set<Long> itemExceptionsLogged;
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	private EventHome eventHome;
	
	private String piqHost;
	
	public PIQSystemEventLoggerImpl(EventHome eventHome) {
		this.eventHome = eventHome;
	}
	
	@Override
	public void enableDuplicateTracking() {
		this.itemExceptionsLogged = new HashSet<Long>();
	}
	
	@Override
	public void clearDuplicateTracking() {
		if (itemExceptionsLogged != null) {
			itemExceptionsLogged.clear();
		}
	}
	
	@Override
	public Event logIPAddressConflict(Long eventId, PIQIPAddressConflictException ex, String eventSource) {
		Event ev = null;
		Item item = ex.getItem();
		
		if (!hasLoggedException( item, "ipConflict" )) {
			try {
				String item_lkp = item.getClassLookup().getLkpValue();
				if( item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK ) item_lkp += " Item";
				
				deleteEvent( eventId );
				
				MessageSource msg = eventHome.getMessageSource();
				
				ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
				ev.setSummary( buildIPAddressConflictMsg(ex, item, item_lkp) );
				eventHome.addItemEventParams(ev, item);
				ev.addParam(msg.getMessage("piqUpdate.eventDetail.ipAddress", null, null), ex.getIpAddress());
				if (ex.isPdu()) {
					ev.addParam(msg.getMessage("piqUpdate.eventDetail.proxyIndex", null, null), ex.getProxyIndex());
				}
				eventHome.saveEvent( ev );
			} 
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
		
		return ev;
	}

	@Override
	public Event logPDUDoesNotExistInPIQ(Item pduItem, String ipAddress, String eventSource) {
		Event ev = null;
		
		if (!hasLoggedException( pduItem, "pduDoesNotExist" )) {
			MessageSource msg = eventHome.getMessageSource();
			try {
				ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
				ev.setSummary( msg.getMessage(
						"piqUpdate.rackPDUDoesNotExist",
						new Object[] { pduItem.getItemName(), piqHost },
						null)
				);
				eventHome.addItemEventParams(ev, pduItem);
				ev.addParam( msg.getMessage("piqUpdate.eventDetail.ipAddress", null, null), ipAddress );
				eventHome.saveEvent( ev );
			} 
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
		
		return ev;
	}

	@Override
	public Event logItemDoesNotExistInPIQ(Item item, String ipAddress, String eventSource) {
		Event ev = null;
		
		if (!hasLoggedException( item, "itemDoesNotExist" )) {
			try {
				String item_lkp = item.getClassLookup().getLkpValue();
				if( item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK ) item_lkp += " Item";
				MessageSource msg = eventHome.getMessageSource();
				ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
				ev.setSummary( msg.getMessage(
						"piqUpdate.itemDoesNotExist",
						new Object[] { item_lkp, item.getItemName(), piqHost },
						null)
				);
				eventHome.addItemEventParams(ev, item);
				ev.addParam( msg.getMessage("piqUpdate.eventDetail.ipAddress", null, null), ipAddress);
				eventHome.saveEvent( ev );
			} 
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
		
		return ev;
	}

	@Override
	public Event logInstalledPDUHasNoIpAddress(Item pduItem, String eventSource) {
		Event ev = null;
		
		if (!hasLoggedException( pduItem, "pduDoesNotExist" )) {
			try {
				ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
				ev.setSummary( eventHome.getMessageSource().getMessage("piqUpdate.installedPDUWithoutIPAddressError",
						new Object[]{ pduItem.getItemName(), piqHost },
						null)
				);
				eventHome.addItemEventParams(ev, pduItem);
				eventHome.saveEvent( ev );
			} 
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
		
		return ev;
	}
	
	@Override
	public void addPIQUpdateExceptionEventParams(Event ev, PIQUpdateException ex) {
		if (ev == null) return;
		MessageSource msg = eventHome.getMessageSource();
		 
		eventHome.addItemEventParams(ev, ex.getItem());
		
		if (StringUtils.hasText(ex.getIpAddress())) {
			ev.addParam(msg.getMessage("piqUpdate.eventDetail.ipAddress", null, null), ex.getIpAddress());
			if (ex.isPdu()) {
				ev.addParam(msg.getMessage("piqUpdate.eventDetail.proxyIndex", null, null), ex.getProxyIndex());
			}
		}
		
		int i = 1;
		for (String message : ex.getPiqMessages()) {
			ev.addParam("Message #" + i, message);
			i++;
		}
		
		if (StringUtils.hasText(ex.getPiqErrorCode())) {
			ev.addParam("Error Code", ex.getPiqErrorCode());
		}
	}
	
	
	public Event logGenericItemError(ErrorJSON error, Long eventId, Item item, String ipAddress, String proxyIndex, String eventSource) {
		Event ev = null;
		
		if (!hasLoggedException( item, "genericItemError" )) {
			try {
				deleteEvent( eventId );
				MessageSource msg = eventHome.getMessageSource();
				ev = eventHome.createEvent(EventType.PIQ_UPDATE, EventSeverity.WARNING, eventSource);
				ev.setSummary( buildGenericItemErrorSummary(error) );
				if (item != null) {
					eventHome.addItemEventParams(ev, item);
					ev.addParam(msg.getMessage("piqUpdate.eventDetail.ipAddress", null, null), ipAddress);
					if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU) {
						ev.addParam(msg.getMessage("piqUpdate.eventDetail.proxyIndex", null, null), proxyIndex);
					}
				}
				eventHome.saveEvent( ev );
			} 
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
		
		return ev;
	}
	
	//
	// Private methods
	//
	
	private void deleteEvent(Long eventId) {
		if (eventId != null && eventId > 0) {
			// Purge the original REST communication error
			List<Long> eventIds = new LinkedList<Long>();
			eventIds.add( eventId );
			try {
				eventHome.clearEvents( eventIds );
				eventHome.purgeEvents( eventIds );
			}
			catch (DataAccessException e) {
				log.error("", e);
			}
		}
	}
	
	/**
	 * Checks if we've already logged an exception for an item.
	 * @param item
	 * @return
	 */
	private boolean hasLoggedException(Item item, String type) {
		if (item == null) return false;
		if (itemExceptionsLogged == null) return false;
		
		if (itemExceptionsLogged.contains( getErrorKey(item, type) ) ) {
			//log.error("SKIPPING DUPLICATE EXCEPTION FOR ITEM : " + item.getItemName());
			return true;
		}
		
		itemExceptionsLogged.add( item.getItemId() );
		//log.error("LOGGED EXCEPTIONS: " + itemExceptionsLogged.size());
		
		return false;
	}
	
	private String getErrorKey(Item item, String type) {
		StringBuffer sb = new StringBuffer();
		sb.append(item.getItemId());
		sb.append(":");
		sb.append(type);
		return sb.toString();
	}
	
	private String buildGenericItemErrorSummary(ErrorJSON error) {
		String msg = null;
		MessageSource msgSrc = eventHome.getMessageSource();
		
		if (error == null) {
			msg = msgSrc.getMessage("piqUpdate.noPIQErrorJSON",
					new Object[] { piqHost },
					null
			);
			return msg;
		}
		
		List<String> errs = error.getMessages();
		
		if (errs != null && !errs.isEmpty()) {
			int errCount = errs.size();
			StringBuffer buf = new StringBuffer();
			int num = 1;
			for (String err : errs) {
				if (errCount > 1) {
					buf.append("\n(");
					buf.append(num);
					buf.append(") ");
				}
				
				buf.append( err );
				if (!err.endsWith(".")) {
					buf.append(". ");
				}
				num++;
			}
			msg = msgSrc.getMessage("piqUpdate.genericItemError",
				new Object[] { errs.size(), buf.toString(), piqHost },
				null
			);
		}
		
		return msg;
	}
	
	private String buildIPAddressConflictMsg(PIQUpdateException ex, Item item, String item_lkp) {
		String msg;
		MessageSource msgSrc = eventHome.getMessageSource();
		
		if (ex.isPdu()) {
			msg = msgSrc.getMessage("piqUpdate.pduIPAddressConflictError",
					new Object[] { item.getItemName(), ex.getIpAddress(), ex.getProxyIndex(), piqHost },
					null);
		}
		else {
			msg = msgSrc.getMessage("piqUpdate.deviceIPAddressConflictError",
					new Object[] { item_lkp, item.getItemName(), ex.getIpAddress(), piqHost },
					null);
		}
		
		return msg;
	}

	public String getPiqHost() {
		return piqHost;
	}

	public void setPiqHost(String piqHost) {
		this.piqHost = piqHost;
	}
}
