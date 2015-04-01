/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.sql.Timestamp;
import java.util.Calendar;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.tests.TestBase;

/**
 * Tests automatic clearing of asset events.
 * @author Andrew Cohen
 */
public class AssetAutoClearTest extends TestBase {

	private AssetAutoClear autoClear;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		autoClear = (AssetAutoClear) ctx.getBean("autoClearAssetEventHome");
	}

	/**
	 * The method will test that a new "Asset Tag Remove" event will automatically clear the most recent
	 * "Asset Tag Connect" event at the same U-position IF there is no item at that U-position.
	 * @author Andrew Cohen
	 */
	@Test
	public final void testAutoClearAssetTagConnectEvents() throws Throwable {		
		
		// Base parameters for creating asset events for this unit test
		AssetParams params = new AssetParams();
		params.pduName = "PDU #212";
		params.pduId = "22";
		params.rackUnitNumber = "4";
		params.rackUnitId = "1";
		params.assetTagId = "000013DC11A4";
		params.assetStripNumber = "000013DC11A";
		params.assetStripId = "1";
		
		// Create asset tag connect event at U-position with no item
		Event connect1 = createAssetEvent(EventType.ASSET_TAG_CONNECTED, params);
		Thread.sleep(2000); // Simulate wait
		// Create asset tag remove event at same U-position
		Event disconnect1 = createAssetEvent(EventType.ASSET_TAG_REMOVED, params);
		session.flush();
		
		// Run the auto clear logic
		autoClear.autoClearAssetTagConnectEvent( disconnect1 );
		session.flush();
		
		// Reload the asset tag connect event and verify it has been cleared by the remove event
		Event connectEvent = (Event)session.get(Event.class, connect1.getId());
		assertNotNull("Could not load connect event!", connectEvent);
		assertEquals("Connect event is not cleared!", true, connectEvent.isCleared());
		
		Event clearingEvent = connectEvent.getClearingEvent();
		assertNotNull("Expected a clearing event but there is none!", clearingEvent);
		assertEquals("The clearing event is not the one we expected!", disconnect1.getId(), clearingEvent.getId());
		
		Event disconnectEvent = (Event)session.get(Event.class, disconnect1.getId());
		assertNotNull("Could not load disconnect event!", disconnectEvent);
		assertNotNull("Could not load connect event!", connectEvent);
		assertEquals("Disconnect event is not cleared!", true, connectEvent.isCleared());
		clearingEvent = disconnectEvent.getClearingEvent();
		assertEquals("Found a clearing a event for the disconnect event but expected none!", null, clearingEvent);
		
		//////////////////////////////////////////////////////////////////////////
		// Now run the same test, but with an item at the U-position. 
		// In this case the tag disconnect should NOT clear the tag connect event!
		//////////////////////////////////////////////////////////////////////////
		
		// TODO: Assertions commented out because we need to properly setup an item
		// at U-position 5
		params.rackUnitNumber = "5";
		
		// Create asset tag connect event at U-position with no item
		Event connect2 = createAssetEvent(EventType.ASSET_TAG_CONNECTED, params);
		Thread.sleep(2000); // Simulate wait
		// Create asset tag remove event at same U-position
		Event disconnect2 = createAssetEvent(EventType.ASSET_TAG_REMOVED, params);
		
		// Run the remove association
		autoClear.autoClearAssetTagConnectEvent( disconnect2 );
		session.flush();
		
		// Assert that the connect event has NOT been cleared
//		connectEvent = (Event)session.get(Event.class, connect1.getId());
//		assertNotNull("Could not load connect event!", connectEvent);
//		assertEquals("Event should not be cleared!", false, connectEvent.isCleared());
//		clearingEvent = connectEvent.getClearingEvent();
//		assertNull("There should not be a clearing event!", clearingEvent);
	}
	
	@Test
	public void testClearAssetStripDisconnectEvent() throws Throwable {

		AssetParams params = new AssetParams();
		params.pduName = "Test PDU";
		params.pduId = "1";
		params.assetStripNumber = "1";
		params.assetStripId = "1";
		Event disconnectEv = createAssetEvent(EventType.ASSET_STRIP_REMOVED, params);
		session.flush();
		Thread.sleep(1000);
		
		Event connectEv = createAssetEvent(EventType.ASSET_STRIP_CONNECTED, params);
		session.flush();
		
		autoClear.handleEvent( connectEv  );
		session.flush();
		
		Event clearedEvent = (Event)session.get(Event.class, disconnectEv.getId());
		assertNotNull( clearedEvent );
		assertEquals(true,  clearedEvent.isCleared());
		Event clearingEvent = clearedEvent.getClearingEvent();
		assertNotNull( clearingEvent );
		assertEquals(connectEv.getId(), clearingEvent.getId());
	}
	
	//
	// Private helper methods to support public tests
	//
	
	/**
	 * Creates a asset event for testing.
	 * @param type the event type
	 * @param params the params
	 * @return
	 */
	private Event createAssetEvent(EventType type, AssetParams params) {
		Event event = Event.createEvent(session, new Timestamp(Calendar.getInstance().getTimeInMillis()), type, "Unit Test");
		event.addParam(AssetEventParam.PDU_NAME.toString(), params.pduName);
		event.addParam(AssetEventParam.PDU_ID.toString(), params.pduId);
		event.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), params.rackUnitNumber);
		event.addParam(AssetEventParam.ASSET_TAG_ID.toString(), params.assetTagId);
		event.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), params.assetStripNumber);
		event.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), params.assetStripId );
		event.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), params.rackUnitId );
		session.save( event );
		return event;
	}
	
	/**
	 * Convenience class for encapsulating all asset specific event parameters.
	 */
	private class AssetParams implements Cloneable {
		public String pduId;
		public String pduName;
		public String rackUnitNumber;
		public String rackUnitId;
		public String assetStripNumber;
		public String assetStripId;
		public String assetTagId;
		
		AssetParams() {}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			AssetParams copy = new AssetParams();
			copy.pduId = this.pduId;
			copy.pduName = this.pduName;
			copy.rackUnitNumber = this.rackUnitNumber;
			copy.rackUnitId = this.rackUnitId;
			copy.assetStripId = this.assetStripId;
			copy.assetStripNumber = this.assetStripNumber;
			copy.assetTagId = this.assetTagId;
			return copy;
		}
	}

}
