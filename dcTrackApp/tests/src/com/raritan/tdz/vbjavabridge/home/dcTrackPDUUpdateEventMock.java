/**
 * 
 */
package com.raritan.tdz.vbjavabridge.home;



import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;

/**
 * @author prasanna
 *
 */
public class dcTrackPDUUpdateEventMock implements LNEventSubscriber {

	private int times = 0;
	
	dcTrackPDUUpdateEventMock(){
		times = 0;
	}
	
	public int getTimes() {
		return times;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.home.dcTrackListenNotifyEvent#handleEvent(org.hibernate.SessionFactory, java.lang.String)
	 */
	@Override
	public void handleEvent(LNEvent event) {
		System.out.println("Got Notification of Event: " + event);
		times++;
	}

	@Override
	public void subscribe() {
		// TODO Auto-generated method stub
		
	}

}
