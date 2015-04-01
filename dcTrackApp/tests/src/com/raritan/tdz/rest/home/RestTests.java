package com.raritan.tdz.rest.home;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQEventClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.tests.TestBase;


/**
 * Tests that Rest Clients are properly handling communication failures.
 * 
 * @author Andrew Cohen
 */
public class RestTests extends TestBase {
	private static String PDU_IP="192.168.50.185";
	private static String PIQ_IP="192.168.57.35";
	private static String PIQ_UNAME="admin";
	private static String PIQ_PASSWD="raritan";
	
	private PIQEventClient piqEventClient = null;
	private ApplicationSettings appSettings = null;
	private EventHome eventHome = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		piqEventClient = (PIQEventClient)ctx.getBean("piqEventClient");
		appSettings = (ApplicationSettings)ctx.getBean("appSettings");
		eventHome = (EventHome)ctx.getBean("eventHome");
		disableListenNotify();
	}

	// @Test // commenting the test till we have a clear solution to run PIQ integration related tests
	public final void testPIQHttpStatusCodeExceptions() throws Throwable {
		PIQSyncPDUClient pduClient = (PIQSyncPDUClient)ctx.getBean("piqSyncPDUClient");
		Item pduItem = createTestItem("testPIQHttpStatusCodeExceptions", SystemLookup.Class.RACK_PDU, null);
		pduClient.addRPDU(pduItem, null, PDU_IP);
	}
	
	/**
	 * Tests logging and clearing of REST call failures.
	 */
	// @Test // commenting the test till we have a clear solution to run PIQ integration related tests
	public final void testCommunicationFailures() throws Throwable {
		
		appSettings.setProperty(Name.PIQ_IPADDRESS, PIQ_IP);
		appSettings.setProperty(Name.PIQ_USERNAME, PIQ_UNAME);
		appSettings.setProperty(Name.PIQ_PASSWORD, PIQ_PASSWD);

		String host = appSettings.getProperty( Name.PIQ_IPADDRESS );
		System.out.println ("PowerIQ host =" + host);
		
		// Set a bad host name to cause the REST calls to fail
		piqEventClient.setIPAddress("badhostname");
		
		// This call should fail and log an event
		piqEventClient.getAllEvents();
		session.flush();
		
		// Check that the latest event is a communication failure
		Event latestEvent = getLatestEvent();
		assertEquals(true, latestEvent != null);
		assertEquals(SystemLookup.EventType.CANNOT_ACCESS_RESOURCE, latestEvent.getType().getLkpValueCode().longValue());
		
		// This call should fail, but NOT log another event
		piqEventClient.getAllEvents();
		session.flush();
		
		// Check we get the original failure event back
		Event event = getLatestEvent();
		assertEquals(true, event != null);
		assertEquals(latestEvent.getId(), event.getId());
		
		// Reset to the original host name
		piqEventClient.setIPAddress( host );
		
		piqEventClient.getAllEvents();
		session.flush();
		
		// Check that the latest event is not "restore" event
		// The above call should succeeed - If the above call DOES cause an error, then this assertion will fail
		Event restoreEvent = getLatestEvent();
		assertEquals(true, restoreEvent != null);
		assertTrue(SystemLookup.EventType.COMMUNICATION_RESTORED != restoreEvent.getType().getLkpValueCode().longValue());
		
		// TODO: Check that previous failure was cleared by restore event.
	}
	
	private Event getLatestEvent() throws DataAccessException {
		List<Event> events = eventHome.filterActiveEvents(null, null, null);
		if (events.isEmpty()) return null;
		return events.get(0);
	}
	
}
