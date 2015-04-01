package com.raritan.tdz.assetstrip.jobs;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.context.MessageSource;
import org.springframework.flex.messaging.MessageTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.raritan.tdz.assetstrip.home.AssetEventBuilder;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.dto.EventDTO;
import com.raritan.tdz.events.dto.EventDTOImpl;
import com.raritan.tdz.events.home.EventHandlerDelegate;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQEvent;
import com.raritan.tdz.piq.home.PIQEventClient;
import com.raritan.tdz.piq.home.PIQSyncPIQVersion;

/**
 * A service which periodically runs to fetch new asset events from PIQ.
 * The details of the scheduling are defined in the "jobs.xml" spring configuration file.
 * 
 * On each execution it does the following:
 * 
 *  1) Makes a call to fetch all "asset" related events from PIQ.
 *  
 *  2) Feeds the raw PIQ events to the Asset Event Builder service to construct and persist
 *     these new asset events in the dcTrack event table.
 *     
 *  3) Calls the EventHandlerDelegate to apply Auto-Association and Auto-Clear event handlers.
 * 
 * @author Andrew Cohen
 */
public class PIQAssetEventPoller extends QuartzJobBean implements StatefulJob {
	private Logger log = Logger.getLogger(this.getClass());

	// The service for communicating with PIQ to get events
	private PIQEventClient piqEventClient;
	
	// The service that will construct and persist the dcTrack asset events 
	private AssetEventBuilder assetEventBuilder;
	
	// The message service used to "push" notification of new events back to the client
	private MessageTemplate messageTemplate;

	private EventHandlerDelegate eventHandlerDelegate;
	
	private MessageSource messageSource;
	
	private PIQSyncPIQVersion piqSyncPiqVersion;
	                           
	public void setAssetEventBuilder(AssetEventBuilder assetEventBuilder) {
		this.assetEventBuilder = assetEventBuilder;
	}
	
	public void setPiqEventClient(PIQEventClient piqEventClient) {
		this.piqEventClient = piqEventClient;
	}
	
	public void setEventHandlerDelegate(EventHandlerDelegate eventHandlerDelegate) {
		this.eventHandlerDelegate = eventHandlerDelegate;
	}
	
	public void setMessageTemplate(MessageTemplate messageTemplate) {
		this.messageTemplate = messageTemplate;
	}
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public PIQSyncPIQVersion getPiqSyncPiqVersion() {
		return piqSyncPiqVersion;
	}

	public void setPiqSyncPiqVersion(PIQSyncPIQVersion piqSyncPiqVersion) {
		this.piqSyncPiqVersion = piqSyncPiqVersion;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		//long time = System.currentTimeMillis();
		List<Event> assetEvents = null;
		List<PIQEvent> piqAssetEvents = null;
		
		try {
			piqAssetEvents = piqEventClient.getAssetEvents();

			// We are using this asset event poller to poll PIQ address and
			// and update dct_settings table.
			piqSyncPiqVersion.syncPIQVersion(); 
		}
		catch (RemoteDataAccessException e) {
			String msg = messageSource.getMessage("remote.error",
					new Object[] { e.getUrl(), e.getMessage() },
					null);
			log.error(msg);
			return;
		}
		
		//System.out.println("Done fetching asset events: " + (System.currentTimeMillis() - time) + "ms");
		
		try {
			// Create new dcTrack system events from new asset events that we fetch from PIQ
			assetEvents = assetEventBuilder.buildAssetEvents( piqAssetEvents );
			
			// Update the event query date for the next job execution
			if (!piqAssetEvents.isEmpty()) {
				piqEventClient.updateEventQueryDate();
			}
			
			// TODO: It would be nice to have the EventHandlerDelegate automatically listen for new events
			// as a JPA EventListener instead of having to explicitly call it here! This would require
			// changes to our current Spring/Hibernate configuration however, so we'll leave this for now.
			for (Event event : assetEvents) {
				eventHandlerDelegate.processEvent( event );
			}
		}
		catch (Throwable t) {
			// Just catch and log any problem that bubbles up here.
			// We don't want to stop the scheduling nor re-execute the task until the next interval, so we will NOT throw a JobExecutionException here
			log.error("Unexpected error in PIQ Asset Event polling process : ", t);
		}
		finally {
			// "Push" new events to the client
			if (messageTemplate != null && assetEvents != null && !assetEvents.isEmpty()) {
				List<EventDTO> events = new LinkedList<EventDTO>();
				for (Event event : assetEvents) {
					events.add( new EventDTOImpl( event ) );
				}
				messageTemplate.send("eventUpdates", events);
			}
		}
		
		if (log.isInfoEnabled()) {
			log.info("Processed " + (assetEvents != null ? assetEvents.size() : 0) + " new asset events from PIQ");
		}
		
		//System.out.println("Done: " + (System.currentTimeMillis() - time) + "ms");
	}

}
