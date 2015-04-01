package com.raritan.tdz.events;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.mapping.Collection;
import org.springframework.util.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.dto.EventDTOImpl;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.events.home.EventSummary;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.tests.TestBase;

public class DeleteEvents  extends TestBase  {
  
	private EventHome eventHome;
	
	@BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	eventHome = (EventHome)ctx.getBean("eventHome");
   }

    @AfterMethod
    public void tearDown() throws Throwable {
    	super.tearDown();
    }
    

    /** NOTE:
     * This test is the best to run on a DB that has 100,000 events. i.e. the one that reached the max
     * value (like wipro's DB)
     * 
     * @throws ClassNotFoundException
     */
    @Test
    public void testInsertNewEvents() throws ClassNotFoundException {
    	Event ev = null;
    	long numNewEvents = 20;
    	Session session = null;

    	try{
    		session = sf.getCurrentSession();
    		long numEventsBefore = eventHome.getEventCount();
    		EventSummary summaryBefore = eventHome.getEvents();
    		List <EventDTOImpl> events = summaryBefore.getEvents();
    		List<Long> beforeEventIds = new ArrayList<Long>();
    		
    		if( events.size() > 0 ){
    			for( EventDTOImpl dto : events ){
    				beforeEventIds.add(dto.getEventId());
    			}
    			Collections.sort(beforeEventIds);
    		}
    		
    		for ( int i = 0; i < numNewEvents; i++ ){
    			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());

    			ev = eventHome.createEvent(createdAt, Event.EventType.PIQ_UPDATE, EventSeverity.WARNING, "dcTrack");
    			eventHome.saveEvent(ev);
    		}
    		long numEventsAfter = eventHome.getEventCount();
    		System.out.println("======= numEventsBefore = " + numEventsBefore + ", numEventsAfter=" + numEventsAfter);
    		Assert.isTrue(numEventsAfter <= 100000 && numEventsBefore <= numEventsAfter, "numEventsAfter is not correct");
    		
    		EventSummary summaryAfter = eventHome.getEvents();
    		events = summaryAfter.getEvents();
    		List<Long> afterEventIds = new ArrayList<Long>();
    		
    		if( events.size() > 0 ){
    			for( EventDTOImpl dto : events ){
    				afterEventIds.add(dto.getEventId());
    			}
    			Collections.sort(afterEventIds);
    		}
    		
    		long extraLimit = numEventsBefore + numNewEvents - 100000;
    		if( extraLimit > 0 ){
    			System.out.println("### Checking if evbents were added in proper order");
    			//Oldest should be removed and new events placed in their place.

    			long val1 = beforeEventIds.get((int) extraLimit);
    			long val2 = afterEventIds.get(0);
    			
    			Assert.isTrue(val1 == val2 );
    		}
    		  		
    	}catch (DataAccessException e) {
    		System.out.print("Failed to set event");
    	}

    }
}