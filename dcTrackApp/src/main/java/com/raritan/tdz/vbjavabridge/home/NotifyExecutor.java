package com.raritan.tdz.vbjavabridge.home;

import org.apache.log4j.Logger;

import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;

/**
 * A helper class to execute the notification in a separate thread.
 * 
 * @author Prasanna Nageswar
 */
class NotifyExecutor implements Runnable {
	
	/**
	 * 
	 */
	private LNEventSubscriber lnEventSubscriber = null;
	private LNEvent notificationEvent = null;
	
	Logger log = Logger.getLogger(this.getClass());
	
	public NotifyExecutor(LNEventSubscriber lnEventListner, 
				LNEvent notificationEvent ){
		this.lnEventSubscriber = lnEventListner;
		this.notificationEvent = notificationEvent;
	}
	
	
	@Override
	public void run() {
		if (lnEventSubscriber != null){
			lnEventSubscriber.handleEvent(notificationEvent);
		}
	}

	public LNEventSubscriber getLnEventListner() {
		return lnEventSubscriber;
	}

	public void setLnEventListner(LNEventSubscriber lnEvent) {
		this.lnEventSubscriber = lnEvent;
	}
	
	public LNEvent getEvent(){
		return this.notificationEvent;
	}
	
	public void setEvent(LNEvent event){
		this.notificationEvent = event;
	}
	
}