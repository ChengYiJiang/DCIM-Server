/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.PortHome;
import com.raritan.tdz.item.home.SaveUtils;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * Test Asset Strip association logic.
 * @author Andrew Cohen
 */
public class AssetStripAutoAssociateTest extends TestBase {

	private AssetStripAutoAssociation assetStripAssoc = null;
	private PortHome portHome = null;
	
	private Item pdu; // Test PDU to connect to asset strip
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		assetStripAssoc = (AssetStripAutoAssociation) ctx.getBean("aaAssetStripHome");
		portHome = (PortHome) ctx.getBean("portHome");
		
	    //pdu = createTestItem("assetStripAutoAssociateTestPDU", SystemLookup.Class.RACK_PDU, 1);
		pdu = createRackPDU("assetStripAutoAssociateTestPDU");
	}
	
	@AfterMethod
    public void tearDown() throws Throwable {
    	super.tearDown();
    	
    	itemHome.deleteItem(pdu.getItemId(), false, null);
    }

	@Test
	public final void testAddAssetStripToPDU() throws Throwable {
		// Add the first asset strip connect event (index=1)
		Event event = createAssetStripEvent(EventType.ASSET_STRIP_CONNECTED, "ANDREW RACK PDU #1", "1", "1", "AS001");
		Item item = assetStripAssoc.addAssociation( event );
		session.flush();
		
		// Item should have a sensor port
		assertNotNull( item );
		assertEquals(1, getAssetStripSensorPortCount(item, 1));
		
		// Add more connect events for the same asset strip
		for (int i=0; i<5; i++) {
			event = createAssetStripEvent(EventType.ASSET_STRIP_CONNECTED, "ANDREW RACK PDU #1", "1", "1", "AS001");
			item = assetStripAssoc.addAssociation( event );
			session.flush();
			
			// Item should still still have only one sensor port for that asset strip
			assertNotNull( item );
			assertEquals(1, getAssetStripSensorPortCount(item, 1));
			Thread.sleep(2000);
		}
		
		// Add an additional asset strip to the item.
		// NOTE: This will only apply to EMX items, but we are just testing the findSensorPort logic here
		event = createAssetStripEvent(EventType.ASSET_STRIP_CONNECTED, "ANDREW RACK PDU #1", "1", "2", "AS002");
		item = assetStripAssoc.addAssociation( event );
		session.flush();
		
		assertEquals(1, getAssetStripSensorPortCount(item, 2));
	}
	
	@Test
	public final void testRemoveAssetStripToPDU() throws Throwable {
		// Disconnnect the asser strip
		Event event = createAssetStripEvent(EventType.ASSET_STRIP_REMOVED, "ANDREW RACK PDU #1", "1", "1", "AS001");
		Item item = assetStripAssoc.removeAssociation( event );
		session.flush();
		
		// Add the asset strip - this should create a sensor port
		event = createAssetStripEvent(EventType.ASSET_STRIP_CONNECTED, "ANDREW RACK PDU #1", "1", "1", "AS001");
		assetStripAssoc.addAssociation( event );
		session.flush();
		
		// Disconnect the asset strip - the port should remain
		event = createAssetStripEvent(EventType.ASSET_STRIP_REMOVED, "ANDREW RACK PDU #1", "1", "1", "AS001");
		item = assetStripAssoc.removeAssociation( event );
		session.flush();
		
		assertNotNull( item );
		assertEquals(1, getAssetStripSensorPortCount(item, 1 ) );
	}
	
	//
	// Internal helper methods
	//
	
	private Event createAssetStripEvent(EventType eventType, String pduName, String pduId, String assetStripNum, String assetStripId) {
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), pduName);
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), pduId);
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), assetStripNum);
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), assetStripId);
		
		return dctEvt;
	}
	
	private int getAssetStripSensorPortCount(Item item, int assetStripNumber) throws DataAccessException {
		List<Integer> sortOrders = new LinkedList<Integer>();
		sortOrders.add( assetStripNumber );
		
		Collection<SensorPort> ports = portHome.viewSensorPortsByCriteria(
			item.getItemId(), // the rack PDU item
			-1, // Ignore item class code
			null, // We aren't looking for ports by Id
			SystemLookup.PortSubClass.ASSET_STRIP,
			sortOrders,
			-1, // Ignore cabinet
			true,
			null // Ignore sensor name
		);
		
		return ports != null ? ports.size() : 0;
	}
	
	private Item createRackPDU(String itemName) throws Throwable {
		List<ValueIdDTO> itemData = new LinkedList<ValueIdDTO>();
		
		itemData.addAll( SaveUtils.addItemHardwareFields(61L, 1446L) ); // Raritan, Dominion PX DPCR20-20
		itemData.addAll( SaveUtils.addItemIdentityFields(itemName, null) );
		itemData.addAll( SaveUtils.addRackablePlacementFields(1L, 10L, SystemLookup.RailsUsed.BOTH)); // Site A, Cabinet 1H
		
		Map<String, UiComponentDTO> savedData = itemHome.saveItem(-1L, itemData, getTestAdminUser());
		assertNotNull( savedData );
		session.flush();
		
		return (Item)session.get(Item.class, SaveUtils.getItemId(savedData) );
	}
}
