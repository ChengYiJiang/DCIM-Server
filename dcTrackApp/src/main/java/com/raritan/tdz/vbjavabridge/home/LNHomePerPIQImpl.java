package com.raritan.tdz.vbjavabridge.home;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;


public class LNHomePerPIQImpl implements LNHome, Work {
	private Boolean suspend = false;
	
	private String piqHost;
	
	Logger log = Logger.getLogger(this.getClass());
	private SessionFactory sessionFactory = null;
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}



	public String getPiqHost() {
		return piqHost;
	}



	public void setPiqHost(String piqHost) {
		this.piqHost = piqHost;
	}



	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the suspend
	 */
	@Override
	public final Boolean getSuspend() {
		synchronized(this){
			return suspend;
		}
	}



	/**
	 * @param suspend the suspend to set
	 */
	@Override
	public final void setSuspend(Boolean suspend) {
		synchronized(this){
			this.suspend = suspend;
		}
	}

	private int cnt = 0;


    
    LinkedHashMap<LNKey, LNEventSubscriber> eventSubscribers;
	
	public LNHomePerPIQImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
		eventSubscribers = new LinkedHashMap<LNKey, LNEventSubscriber>();
	}
	
	
	@Override
	public void subscribe(LksData operationLks, String tableName,
			LNEventSubscriber eventHandler) {
		LNKey dbEventNameKey = new LNKey(operationLks, tableName);
		this.subscribe(dbEventNameKey, eventHandler);
	}

	@Override
	public void subscribe(LksData operationLks, String tableName, String action, 
			LNEventSubscriber eventHandler) {
		LNKey dbEventNameKey = new LNKey(operationLks, tableName, action);
		this.subscribe(dbEventNameKey, eventHandler);
	}

	
	public void subscribe(LNKey dbEventNameKey, LNEventSubscriber eventHandler) {
		if (dbEventNameKey != null && eventHandler != null)
		{
			eventSubscribers.put(dbEventNameKey, eventHandler);
		}
	}

	@Override
	public void processEvents() {
		//Do Nothing here. This is for the main LNHomeImpl only
	}

	@Transactional
	@Override
	public void processEvent(LNEvent lnEvent) {
		log.debug("Processing Notification: " + lnEvent);
		
		//This is to make sure that we dont process those items that do not belong to this powerIQ!
		//The queue is read by all the context pollers when it starts and want to make sure that 
		//only those contexts where the piqHost belongs to is processed here!
		if (!lnEvent.getCustomField3().equals(piqHost)) {
			log.debug("Process Notification: powerIQHost " + piqHost + "does not belong to this context");
			return;
		}
			
		
		//Construct the key from the lnEvent and get the listener
		LNKey lnKey = new LNKey(lnEvent.getOperationLks(), "\"" + lnEvent.getTableName() + "\"", lnEvent.getAction());
		log.debug("lnKey = " + lnKey);
		
		LNEventSubscriber lnEventListner = eventSubscribers.get(lnKey);
		log.debug("eventSubscribers = " + eventSubscribers);
		
		//Notify in a thread by sending the event and the listner.
		//NotifyExecutor executor = new NotifyExecutor(lnEventListner, lnEvent);
		//service.execute(executor);
		
		if (lnEventListner != null){
			lnEventListner.handleEvent(lnEvent);
			
			cnt++;
		}
	}

	@Override
	public void execute(Connection connection) throws SQLException {
		// DO NOTHING HERE. This is for the main LNHomeImpl only 
		//TODO: This is an old design based code and should be removed entirely.
		
	}

}
