package com.raritan.tdz.tests.events;

import org.testng.annotations.Test;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ExceptionContext;

/**
 * A Unit test for quickly generating events.
 * @author Andrew Cohen
 */
public class GenerateEvents extends TestBase {
	
	private static final int NUM_EVENTS = 2500;
	/**
	 * Test basic persistence of events and related parameters.
	 * @throws Throwable
	 */
	@Test
	public final void testCreateEvents() throws Throwable {
		final long start = System.currentTimeMillis();
		System.out.println("Creating " + NUM_EVENTS + " events...");
		// Create some random events mimicking asset tag attach/detach
		createTestAssetTagEvents( NUM_EVENTS );
		System.out.println("Finished "+(System.currentTimeMillis()-start)+" events");
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
	
	private void createTestAssetTagEvents(int numEvents) throws DataAccessException {
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
				event.setLocation( (DataCenterLocationDetails) session.get(DataCenterLocationDetails.class, 1L));
				
//				if (r.nextBoolean()) {
//					event.setItem( (Item)session.load(Item.class, 10L) ); // Associate with item
//				}
				
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
	/*private Event createTestEvent(Date createdAt) throws DataAccessException {
		Event event = null;
		try {
			session = sf.getCurrentSession();
			event = Event.createEvent(session, new Timestamp(createdAt.getTime()), EventType.ASSET_STRIP_CONNECTED, "Unit Test");
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
	}*/
}
