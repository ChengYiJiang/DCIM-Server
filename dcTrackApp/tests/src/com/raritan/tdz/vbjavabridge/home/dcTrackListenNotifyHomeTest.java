/**
 * 
 */
package com.raritan.tdz.vbjavabridge.home;

import org.testng.annotations.Test;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.tests.TestBase;


/**
 * @author prasanna
 *
 */
public class dcTrackListenNotifyHomeTest extends TestBase {

	/**
	 * Test method for {@link com.raritan.tdz.vbjavabridge.home.LNHomeImpl#subscribe(java.lang.String, com.raritan.tdz.vbjavabridge.home.dcTrackListenNotifyEvent)}.
	 */
	@Test
	public final void testSubscribe() {
	
		PIQSyncDeviceClient client = (PIQSyncDeviceClient)ctx.getBean("piqSyncDeviceClient");
		client.getClass();
		
		
	/*	dcTrackPDUUpdateEventMock updateEvent = new dcTrackPDUUpdateEventMock();
		sf = (SessionFactory)ctx.getBean("sessionFactory");
		dcTrackNotifyEventMock notifyEvent = new dcTrackNotifyEventMock(sf);
		
		
		
		dcTrackListenNotifyHome lnHome = (dcTrackListenNotifyHome) ctx.getBean("dcTrackListenNotifyHome");
		lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE), "dct_items", updateEvent);
		*/
		
		
		//notifyEvent.notifyMessage(message, times);
		try {
			//FIXME: Why this long sleep?? 
			//JB Thread.sleep(15 * 60000);
			Thread.sleep(15);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//assertEquals("Missing notifications: ", times , updateEvent.getTimes());
	}

}
