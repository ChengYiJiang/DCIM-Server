/**
 * 
 */
package com.raritan.tdz.piq.home;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.tests.TestBase;

/**
 * Test PIQ deletion calls.
 * @author Andrew Cohen
 */
public class PIQDeleteClientTest extends TestBase {

	private Item testPDU;
	private DataCenterLocationDetails testLocation;
	private CabinetItem testRack;
	private Item testItem;
	
	@BeforeMethod
	public void setUp() throws Throwable {	
		super.setUp();
	    testPDU = createDcTrackPDU();
	    testLocation = createDcTrackLocation();
	    testRack = createDcTrackCabinet();
	    testItem = createDcTrackItem();
	}

	@Test
	public final void testDeleteDataCenter() throws RemoteDataAccessException {
		PIQSyncLocationClient client = (PIQSyncLocationClient)ctx.getBean("piqSyncLocationClient");
		String dataCenterId = testLocation.getPiqId();
		
		assertTrue( client.isLocationInSync( dataCenterId ) );
		
		client.deleteDataCenter( dataCenterId );
	}
	
	@Test
	public final void testDeleteRack() throws RemoteDataAccessException {
		PIQSyncRackClient client = (PIQSyncRackClient)ctx.getBean("piqSyncRackClient");
		String rackId = testRack.getPiqId().toString();
		
		assertTrue( client.isRackInSync( rackId  ) );
		
		client.deleteRack( rackId );
	}
	
	@Test
	public final void testDeletePDU() throws RemoteDataAccessException {
		PIQSyncPDUClient client = (PIQSyncPDUClient)ctx.getBean("piqSyncPDUClient");
		String pduId = testPDU.getPiqId().toString();
		
		client.deletePDU( pduId );
	}
	
	@Test
	public final void testDeleteItem() throws RemoteDataAccessException {
		PIQSyncDeviceClient client = (PIQSyncDeviceClient)ctx.getBean("piqSyncDeviceClient");
		String itemId = testItem.getPiqId().toString();
		
		assertTrue( client.isDeviceInSync( itemId ) );
		
		client.deleteDevice( itemId );
	}
	
	//
	// Private methods
	//
	
	private Item createDcTrackPDU() {
		Item item = new Item();
		item.setPiqId( 1 ); // Simulate a real PDU in dcTrack with a PIQ ID
		return item;
	}
	
	private Item createDcTrackItem() {
		Item item = new Item();
		item.setPiqId( 1 ); // Simulate a item PDU in dcTrack with a PIQ ID
		return item;
	}
	
	private DataCenterLocationDetails createDcTrackLocation() {
		DataCenterLocationDetails loc = new DataCenterLocationDetails();
		// TODO: The PIQ id for location is some colon delimited format
		loc.setPiqId( "3" ); // Simulate a real Location in dcTrack with a PIQ ID
		return loc;
	}
	
	private CabinetItem createDcTrackCabinet() {
		CabinetItem cab = new CabinetItem();
		// TODO: The PIQ id for location is some colon delimited format
		cab.setPiqId( 1 ); // Simulate a real Cabinet in dcTrack with a PIQ ID
		return cab;
	}
}
