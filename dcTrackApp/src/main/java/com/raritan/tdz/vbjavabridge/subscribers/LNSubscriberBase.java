/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.Joinable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.vbjavabridge.dao.LNEventDAO;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
public abstract class LNSubscriberBase implements
		LNEventSubscriber {
	
	private MessageSource messageSource = null;
	protected SessionFactory sessionFactory = null;
	protected LNHome lnHome = null;
	
	private static String BAD_MAC_ERROR = "bad_record_mac"; 
	private static long BAD_MAC_ERROR_RETRY = 5;
	private static long BAD_MAC_ERROR_MAX_TIME_TO_CLEANUP = 600 *1000;
	
	@Autowired
	private LNEventDAO lnEventDAO;
	
	
	private Map<LNEvent,BadMACErrorCounter> retryCountMap = new HashMap<>();
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public LNSubscriberBase(SessionFactory sessionFactory, LNHome lnHome) {
		this.sessionFactory = sessionFactory;
		this.lnHome = lnHome;
	}
	
	@Required
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	
	protected void subscribe(SessionFactory sessionFactory, 
						Class<?> dcTrackDomain,
						LksData operationLks,
						LNHome lnHome)
	{
		
	    ClassMetadata cmd = sessionFactory.getClassMetadata(dcTrackDomain);

	    String tableName = null;
	    //check that the class is mapped to something with a table name
	    if (cmd != null || Joinable.class.isInstance(cmd) == true)
		 tableName = Joinable.class.cast(cmd).getTableName();

	    if (tableName != null && !tableName.isEmpty()){
	    	lnHome.subscribe(operationLks, tableName, this);
	    }
		
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.home.dcTrackListenNotifyEventListener#handleEvent(org.hibernate.SessionFactory, com.raritan.tdz.vbjavabridge.domain.ListenNotifyEvent)
	 */
	@Transactional
	@Override
	public void handleEvent(LNEvent event) {
		cleanupOldMacErrors();
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			
			//Process the event
			try {
				processEvent(session, event);
				
				//Delete the event
				deleteEvent(session, event);
			} catch (RemoteDataAccessException e) {
				handleRemoteDataAccessException(event, session, e);
			} catch (Throwable t){ 
				//Delete the event
				//This is a critical error and could be potentially an hibernate error, we should not continue
				//processing such event
				log.error("",t);
				//Delete the event
				deleteEvent(session, event);
			}
		}
	}

	/**
	 * Make sure that user is notified of any 4xx errors
	 * An entry in the event table will provide user with
	 * detail. <p>For example 404 occur when there is a total mismatch
	 * in the piqIds between what is there in dcTrack and
	 * PIQ, we should handle this.</p><p> In the sync clients
	 * we generally try to handle this situation gracefully
	 * When we cannot we let the user know what to do and clear the event. 
	 * <p> We have seen such situations in the field where it goes to
	 * a never ending loop and with a GotoMeeting we need to delete the entries
	 * in event table</p>
	 * @param event
	 * @param session
	 * @param e
	 */
	private void handleRemoteDataAccessException(LNEvent event,
			Session session, RemoteDataAccessException e) {
		ExceptionContext ec = e.getExceptionContext();
		if (ec != null) {
			Object ex = ec.getExceptionItem( ExceptionContext.EXCEPTION );
			
			if ((ex != null) && (ex instanceof HttpClientErrorException || ex instanceof ResourceAccessException)) {
			
				if (ex instanceof ResourceAccessException){
					//Log it
					String msg = messageSource.getMessage("remote.error",
							new Object[] { e.getUrl(), e.getMessage() },
							null
					);
					log.error( msg );
					
					handleBadRecordMacError(event, ex);						
				} else {
					//Log it
					String msg = messageSource.getMessage("remote.error",
							new Object[] { e.getUrl(), e.getMessage() },
							null
					);
					log.error( msg );
					
					//Notify user with CRITICAL severity. They could atleast try to sync this
					//manually via classic client by unmapping and re-mapping method.
					//NOTE: Entering the event is taken care by the RestClient object. So no need to handle it here
					//@See: handleError method
					
					
					//Delete the event
					deleteEvent(session,event);
				}	
			}
		} else {
		
			// When we have a remote API failure, we do not delete the event.
			String msg = messageSource.getMessage("remote.error",
					new Object[] { e.getUrl(), e.getMessage() },
					null
			);
			log.error( msg );
		}
	}


	
	protected void deleteEvent(Session session, LNEvent event){
		StatelessSession statelessSession = sessionFactory.openStatelessSession();
		Query eventQry = statelessSession.createQuery("delete from LNEvent where eventId = :id");
		eventQry.setLong("id", event.getEventId());
		eventQry.executeUpdate();
		statelessSession.close();
	}
	

	private void processEvent(Session session, LNEvent event) throws Throwable{
		/*if(event.getTableRowId() < 1){
			return;
		}*/
		
		if (event.getOperationLks().getLkpValueCode() == SystemLookup.VBJavaBridgeOperations.INSERT){
			processInsertEvent(session, event);
		} else if (event.getOperationLks().getLkpValueCode() == SystemLookup.VBJavaBridgeOperations.UPDATE){
			processUpdateEvent(session, event);
		} else if (event.getOperationLks().getLkpValueCode() == SystemLookup.VBJavaBridgeOperations.DELETE){
			processDeleteEvent(session, event);
		}
	}
	
	private void handleBadRecordMacError(LNEvent event, Object ex) {
		ResourceAccessException re = (ResourceAccessException)ex;
		if (re.getMessage().contains(BAD_MAC_ERROR)){
			BadMACErrorCounter badMacCounter = retryCountMap.get(event);
			if (badMacCounter == null){
				badMacCounter = new BadMACErrorCounter();
				badMacCounter.setCount(0);
				badMacCounter.setTimestamp(Calendar.getInstance().getTimeInMillis());
			} else {
				badMacCounter.setCount(badMacCounter.getCount() + 1);
				badMacCounter.setTimestamp(Calendar.getInstance().getTimeInMillis());
			}
			
			if (badMacCounter.getCount() < BAD_MAC_ERROR_RETRY){
				lnEventDAO.create(event);
			}
		
			retryCountMap.put(event, badMacCounter);
		}
	}
	
	private void cleanupOldMacErrors(){
		List<LNEvent> eventsToBeRemoved = new ArrayList<>();
		for (Map.Entry<LNEvent, BadMACErrorCounter> entry:retryCountMap.entrySet()){
			BadMACErrorCounter counter = entry.getValue();
			if (counter.getTimestamp() - counter.getPrevTimestamp() > BAD_MAC_ERROR_MAX_TIME_TO_CLEANUP){
				eventsToBeRemoved.add(entry.getKey());
			}
		}
		
		for (LNEvent eventToBeRemoved:eventsToBeRemoved){
			retryCountMap.remove(eventToBeRemoved);
		}
	}
	
	protected abstract void processInsertEvent(Session session, LNEvent event) throws RemoteDataAccessException, Throwable;
	protected abstract void processDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException, Throwable;
	protected abstract void processUpdateEvent(Session session, LNEvent event) throws RemoteDataAccessException, Throwable;
	
	private class BadMACErrorCounter {
		private long prevTimestamp = 0;
		private int count = 0;
		private long timestamp = 0;
		public int getCount() {
			return count;
		}

		public long getPrevTimestamp() {
			return prevTimestamp;
		}

		public void setCount(int count) {
			this.count = count;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
		
		public void setTimestamp(long timestamp) {
			this.prevTimestamp = timestamp;
			this.timestamp = timestamp;
		}
		
		
	}
}
