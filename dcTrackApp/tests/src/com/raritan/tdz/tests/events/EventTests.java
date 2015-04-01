/**
 * 
 */
package com.raritan.tdz.tests.events;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.events.home.EventSummary;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Unit tests for the Event Business Layer.
 * 
 * @author Andrew Cohen
 */
public class EventTests extends TestBase {
	
	private static final String EVENT_SOURCE = "Unit Test";
	
	private EventHome eventHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		eventHome = (EventHome)ctx.getBean("eventHome");
		disableListenNotify();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		clearEvents();
		super.tearDown();
	}
	
	/**
	 * Test basic persistence of events and related parameters.
	 * @throws Throwable
	 */
	@Test
	public final void testCreateEvents() throws Throwable {
		final long start = System.currentTimeMillis();
		final int numEvents = 100;
		
		// Create some random events mimicking asset tag attach/detach
		createTestAssetTagEvents(numEvents);
		
		log.debug("testCreateEvent: succeeded created " + numEvents + 
				" events in "+(System.currentTimeMillis()-start) + "ms");
		
		// Assert the events where created
		EventSummary events = eventHome.getEvents();
		runEventListAssertions(numEvents, events, null);
		
		List<Event> eventList = eventHome.filterEvents(null, null, null);
		// Assert parameters
		for (Event event : eventList) { 
			if (!event.getSource().equals( EVENT_SOURCE) ) continue;
			//assertNotNull(event.getCabinet());
			AssertJUnit.assertNotNull(event.getLocation());
			
			Map<String, EventParam> params = event.getEventParams();
			runEventParamsAssertions(eventParams.length, params);
			for(String eventParam : eventParams) {
				AssertJUnit.assertNotNull( "Expected param '"+eventParam+"' but not found", params.get(eventParam) );
			}
		}
	}
	
	@Test
	public final void testEventLists() throws Throwable {
		List<Long> eventIds = new LinkedList<Long>();
		
		Event ev1 = Event.createEvent(session, new Timestamp(System.currentTimeMillis()), EventType.ASSET_STRIP_CONNECTED, EVENT_SOURCE);
		session.save(ev1);
		Event ev2 = Event.createEvent(session, new Timestamp(System.currentTimeMillis()), EventType.ASSET_STRIP_CONNECTED, EVENT_SOURCE);
		session.save(ev2);
		session.flush();
		
		runEventListAssertions(2, eventHome.getEvents(), null);
		runEventListAssertions(2, eventHome.getActiveEvents(), false);
		runEventListAssertions(0, eventHome.getClearedEvents(), true);
		
		eventIds.add( ev1.getId() );
		eventHome.clearEvents( eventIds );
		eventIds.clear();
		session.flush();
		
		runEventListAssertions(2, eventHome.getEvents(), null);
		runEventListAssertions(1, eventHome.getActiveEvents(), false);
		runEventListAssertions(1, eventHome.getClearedEvents(), true);
		
		eventIds.add( ev2.getId() );
		eventHome.clearEvents( eventIds );
		eventIds.clear();
		session.flush();
		
		runEventListAssertions(2, eventHome.getEvents(), null);
		runEventListAssertions(0, eventHome.getActiveEvents(), false);
		runEventListAssertions(2, eventHome.getClearedEvents(), true);
	}
	
	@Test
	public final void testBulkDelete() throws Throwable {
		Calendar cal = Calendar.getInstance();
		Event ev = null;
		
		// Simulate event for on 1/1/211
		cal.set(2011, 1, 1);
		ev = createTestEvent( cal.getTime() );
		ev.clear( null );
		
		// Simulate event for on 2/1/211
		cal.set(2011, 2, 1);
		ev = createTestEvent( cal.getTime() );
		ev.clear( null );
		
		// Simulate event for on 3/1/211
		cal.set(2011, 3, 1);
		ev = createTestEvent( cal.getTime() );
		ev.clear( null );
		
		// Simulate event for on 4/1/211
		cal.set(2011, 4, 1);
		ev = createTestEvent( cal.getTime() );
		ev.clear( null );
		
		sf.getCurrentSession().flush();
		
		EventSummary events = eventHome.getClearedEvents();
		runEventListAssertions(4, events, true);
		
		// Delete events before 12/1/2010 - should delete nothing
		cal.set(2010, 12, 1);
		AssertJUnit.assertEquals(0, eventHome.purgeEvents( cal.getTime() ) );
		runEventListAssertions(4, eventHome.getEvents(), null);
		
		// Delete events before 1/15/2011 - should delete 1 event
		cal.set(2011, 1, 15);
		AssertJUnit.assertEquals(1, eventHome.purgeEvents( cal.getTime() ) );
		runEventListAssertions(3, eventHome.getEvents(), null);
		
		// Delete events before 2/15/2011 - should delete 1 more event
		cal.set(2011, 2, 15);
		AssertJUnit.assertEquals(1, eventHome.purgeEvents( cal.getTime() ) );
		runEventListAssertions(2, eventHome.getEvents(), null);
		
		// Delete events before 3/15/2011 - should delete 1 more event
		cal.set(2011, 3, 15);
		AssertJUnit.assertEquals(1, eventHome.purgeEvents( cal.getTime() ) );
		runEventListAssertions(1, eventHome.getEvents(), null);
	}
	
	@Test
	public final void testFilterEvents() throws Throwable {
		EventType type =  EventType.ASSET_STRIP_CONNECTED;
		
		Event ev = createTestEvent( Calendar.getInstance().getTime() );
		ev.addParam("p1", "v1");
		ev.addParam("p2", "v2");
		ev.addParam("p3", "v3");
		ev.addParam("p3", "v3");
		
		Event ev2 = createTestEvent( Calendar.getInstance().getTime() );
		ev2.addParam("p1", "v1");
		ev2.addParam("p2", "v21");
		ev2.addParam("p3", "v31");
		
		session.flush();
		
		AssertJUnit.assertEquals(2, eventHome.filterActiveEvents(type, "p1", "v1").size());
		AssertJUnit.assertEquals(1, eventHome.filterActiveEvents(type, "p2", "v2").size());
		AssertJUnit.assertEquals(1, eventHome.filterActiveEvents(type, "p2", "v21").size());
		AssertJUnit.assertEquals(0, eventHome.filterActiveEvents(type, "p2", "v22").size());
		AssertJUnit.assertEquals(1, eventHome.filterActiveEvents(type, "p3", "v3").size());
		AssertJUnit.assertEquals(1, eventHome.filterActiveEvents(type, "p3", "v31").size());
	}
	
	@Test
	public final void testClearEvents() throws Throwable {
		Event clearingEvent = eventHome.getEventDetail( 43909L );
		
		List<Long> eventIds = new LinkedList<Long>();
		eventIds.add( 43906L );
		eventIds.add( 43907L );
		
		eventHome.clearEvents( eventIds, clearingEvent );
		
		System.out.println("Done!");
	}
	
	/**
	 * Test clearing all events.
	 * @throws Throwable
	 */
	@Test
	public final void testClearAllEvents() throws Throwable {
		createTestAssetTagEvents( 200 );
		session.flush();
		
		AssertJUnit.assertTrue(eventHome.getEventCount() > 0);
		
		eventHome.clearAllEvents();
		session.flush();
		
		EventSummary summary = eventHome.getActiveEvents();
		AssertJUnit.assertEquals(200, summary.getGrandTotal());
		AssertJUnit.assertEquals(0, summary.getEvents().size());
	}

	/**
	 * Test purging an event which does not clear another event.
	 */
	@Test
	public final void testPurgeNonClearingEvent() throws Throwable {
		Event event = createTestEvent( new Date() );
		List<Long> ids = new LinkedList<Long>();
		ids.add( event.getId() );
		eventHome.clearEvents( ids );
		eventHome.purgeEvents( ids );
	}
	
	/**
	 * Test purging an event which clears another event.
	 */
	@Test
	public final void testPurgeClearingEvent() throws Throwable {
		Event event = createTestEvent( new Date() );
		Event clearingEvent = createTestEvent( new Date() );
		clearingEvent.setType(session, EventType.ASSET_STRIP_REMOVED);
		event.clear( clearingEvent );
		session.flush();
		
		List<Long> ids = new LinkedList<Long>();
		ids.add( event.getId() );
		eventHome.purgeEvents( ids );
	}
	
	/**
	 * Test purge all events.
	 * @throws Throwable
	 */
	@Test
	public final void testPurgeAllEvents() throws Throwable {
		createTestAssetTagEvents( 313 );
		session.flush();
		
		AssertJUnit.assertTrue(eventHome.getEventCount() > 0);
		
		eventHome.purgeAllEvents();
		session.flush();
		
		EventSummary summary = eventHome.getActiveEvents();
		AssertJUnit.assertEquals(313, summary.getGrandTotal());
		AssertJUnit.assertEquals(313, summary.getEvents().size());
		
		summary = eventHome.getClearedEvents();
		AssertJUnit.assertEquals(313, summary.getGrandTotal());
		AssertJUnit.assertEquals(0, summary.getEvents().size());
	}
	
	//
	// Private methods
	//
	
	private void clearEvents() {
		log.debug("Called clear events");
		Session session = null;
		try {
			session = sf.getCurrentSession();
			int count = session.createQuery("delete from Event where source = '" + EVENT_SOURCE + "'").executeUpdate();
			log.debug("clearEvents: Cleared "+count+" events");
		}
		catch (Throwable t) {
			throw new RuntimeException("Error clearing events", t);
		}
	}

	private void clearAllEvents() {
		log.debug("Called clear events");
		Session session = null;
		try {
			session = sf.getCurrentSession();
			int count = session.createQuery(" delete from Event ").executeUpdate();
			log.debug("clearEvents: Cleared "+count+" events");
		}
		catch (Throwable t) {
			throw new RuntimeException("Error clearing events", t);
		}
	}

	private void runEventListAssertions(int size, EventSummary events, Boolean clearedEvent) throws DataAccessException {
		AssertJUnit.assertNotNull(events);
		List<Event> eventList = null;
		if (null == clearedEvent) {
			eventList = eventHome.filterEvents(null, null, null);
		}
		else if (clearedEvent == true) {
			eventList = eventHome.filterClearedEvents(null, null, null);
		}
		else if (clearedEvent == false) {
			eventList = eventHome.filterActiveEvents(null, null, null);
		}
		AssertJUnit.assertNotNull(eventList);
		int actualSize = 0;
		for (Event e : eventList) {
			if (e.getSource().equals( EVENT_SOURCE ) ) {
				actualSize++;
			}
		}
		
		AssertJUnit.assertEquals("Unexpected size of event list", size, actualSize);
	}
	
	private void runEventParamsAssertions(int size, Map<String, EventParam> params) {
		AssertJUnit.assertNotNull(params);
		AssertJUnit.assertEquals("Unexpected size of event params map", size, params.size());
	}
	
	private final String[] eventParams = {
		"PDU ID",
		"PDU Name",
		"RU",
		"Asset Tag Id",
		"Asset Strip Number",
		"Asset Strip Id",
		"Rack Unit Id",
		"Notification Status"
	};
	
	private void createTestAssetTagEvents(int numEvents) throws Throwable {
		Session session = null;
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		
		try {
			session = sf.getCurrentSession();
			
			for (int i=1; i<=numEvents; i++) {
				EventType type = r.nextBoolean() ? EventType.ASSET_TAG_CONNECTED : EventType.ASSET_TAG_REMOVED;
				
				// Create event record wiexcath basic details
				Event event = Event.createEvent(session, createdAt, type, "Unit Test");
				event.setuPosition( new Long(r.nextInt(47) + 1) );
				//event.setCabinet( (CabinetItem)session.load(CabinetItem.class, 1L) );
				event.setLocation( getUnitTestLocation() );
				
				Item item = createTestItem("testAssetTagItem-" + i, SystemLookup.Class.DEVICE, null);
				if (r.nextBoolean()) {
					event.setItem( item ); // Associate with item
				}
				
				// Set a random processed status 
				event.setProcessedStatus( SystemLookup.getLksData(session, r.nextBoolean() ? 70211L : 70212L) );
				
				event.addParam(eventParams[0], Integer.toString(r.nextInt(10000)+1));
				event.addParam(eventParams[1], "PDU #212");
				event.addParam(eventParams[2], Integer.toString(r.nextInt(47)+1));
				event.addParam(eventParams[3], "000013DC11A" + (r.nextInt(8)+1) );
				event.addParam(eventParams[4], "000013DC11A" + (r.nextInt(8)+1) );
				event.addParam(eventParams[5], Integer.toString(r.nextInt(10000)+1) );
				event.addParam(eventParams[6], Integer.toString(r.nextInt(10000)+1) );
				event.addParam(eventParams[7], Integer.toString(r.nextInt(10000)+1) );
				
				session.save( event );
			}
			session.flush(); 
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext("Failed to fetch asset events", this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext("Failed to fetch asset events", this.getClass(), e));
		}	
	}
	
	// Creates a test event with no parameters
	private Event createTestEvent(Date createdAt) throws Throwable {
		Event event = null;
		try {
			session = sf.getCurrentSession();
			event = Event.createEvent(session, new Timestamp(createdAt.getTime()), EventType.ASSET_STRIP_CONNECTED, EVENT_SOURCE);
			event.setLocation( getUnitTestLocation() );
			session.save(event);
			session.flush(); 
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext("Failed to fetch asset events", this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext("Failed to fetch asset events", this.getClass(), e));
		}
		return event;
	}
	
	@Test
	public final void testTimeForQuery() throws DataAccessException {
		eventHome.getActiveEvents();
	}
}
