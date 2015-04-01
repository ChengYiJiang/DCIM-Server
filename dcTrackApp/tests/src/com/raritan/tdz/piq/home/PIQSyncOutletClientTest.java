/**
 * 
 */
package com.raritan.tdz.piq.home;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.home.PortHome;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.Outlet;
import com.raritan.tdz.tests.TestBase;

/**
 * @author Andrew Cohen
 */
public class PIQSyncOutletClientTest extends TestBase {

	/**
	 * Tests adding/removing/updating power connections in PIQ.
	 * @throws Throwable
	 */
	//@Test
	@Test
	public final void testPowerConnections() throws Throwable {
		PIQSyncOutletClient client = (PIQSyncOutletClient)ctx.getBean("piqSyncOutletClient");
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");
		
		// Assumes we have PDU power port in the database mapped to the PIQ PDU outlet
		PowerPort destPort = itemHome.viewPowerPortsForItem(560L, false, null).get(0);
		assertNotNull("No destination port found", destPort);
		Long outletId = destPort.getPiqId();
		assertNotNull("Destination port has no PIQ ID", outletId);
		
		// Assumes we have a device item power port #1 in the database to connect to the PDU power port
		PowerPort sourcePort1 = itemHome.viewPowerPortsForItem(541L, false, null).get(0);
		Long deviceId1 = sourcePort1.getItem().getPiqId().longValue();
		
		// Assumes we have a device item power port #2 in the database to connect to the PDU power port
		PowerPort sourcePort2 = itemHome.viewPowerPortsForItem(546L, false, null).get(0);
		Long deviceId2 = sourcePort2.getItem().getPiqId().longValue();
		
		// Clear any existing device connections to the outlet
		client.deletePowerConnection(destPort);
		assertOutletNotConnected( outletId );
		Thread.sleep(500);
		
		// Make a power connection between device #1 and the outlet
		client.updatePowerConnection(sourcePort1, destPort);
		session.flush();
		assertOutletConnectedToDevice(outletId, deviceId1);
		Thread.sleep(500);
		
		// Clear the existing power connection
		client.deletePowerConnection( destPort );
		session.flush();
		assertOutletNotConnected( outletId );
		Thread.sleep(500);
		
		// Make a power connection between device #2 and the outlet
		client.updatePowerConnection(sourcePort2, destPort);
		session.flush();
		assertOutletConnectedToDevice(outletId, deviceId2);
		Thread.sleep(500);
		
		// Clear the existing power connection again
		client.deletePowerConnection( destPort );
		session.flush();
		assertOutletNotConnected( outletId );
		Thread.sleep(500);
	}
	
	//@Test
	@Test
	public final void testPDUPowerPortToOutletAssocation() throws Throwable {
		PIQSyncPDUClient pduClient = (PIQSyncPDUClient)ctx.getBean("piqSyncPDUClient");
		PIQSyncOutletClient outletClient = (PIQSyncOutletClient)ctx.getBean("piqSyncOutletClient");
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");
		Item pduItem = createPDUItemWithPowerPorts();
		DataPort dataPort = itemHome.viewDataPortsForItem(pduItem.getItemId(), false, null).get(0);
		final String ipAddress = "192.168.50.185"; // Must be a real (discoverable) PDU
		
		// Assert that the power ports are NOT yet linked to outlets in PIQ
		List<PowerPort> powerPorts = itemHome.viewPowerPortsForItem(pduItem.getItemId(), false, null);
		for (PowerPort pp : powerPorts) {
			assertNull( "Found unexpected PIQ ID for power port!", pp.getPiqId() );
		}
		
		// Add the Rack PDU to PIQ - this should trigger linking the power ports with the PIQ outlets
		pduClient.addRPDU(pduItem, dataPort, ipAddress);
		session.flush();
		
		powerPorts = itemHome.viewPowerPortsForItem(pduItem.getItemId(), false, null);
		for (PowerPort pp : powerPorts) {
			assertNotNull( "Expected PIQ ID for power port but found none!", pp.getPiqId() );
			
			Outlet outlet = outletClient.getOutlet( pp.getPiqId() );
			assertNotNull( "No outlet found for PIQ ID " + pp.getPiqId(), outlet);
			assertEquals( "Power port sort order does not match mapped outlet!", pp.getSortOrder(), outlet.getOutletId());
		}
		session.flush();
	}
	
	//
	// Private assertion methods
	//
	
	private void assertOutletConnectedToDevice(long outletId, Long deviceId) throws Throwable {
		PIQSyncOutletClient client = (PIQSyncOutletClient)ctx.getBean("piqSyncOutletClient");
		Outlet outlet = client.getOutlet( outletId );
		assertNotNull( "No outlet found for ID " + outletId, outlet );
		assertNotNull( "Expected a device to be connected to the outlet!", outlet.getDeviceId() );
		assertEquals( "Outlet connected to unexpected device!", deviceId, outlet.getDeviceId());
	}
	
	private void assertOutletNotConnected(long outletId) throws Throwable {
		PIQSyncOutletClient client = (PIQSyncOutletClient)ctx.getBean("piqSyncOutletClient");
		Outlet outlet = client.getOutlet( outletId );
		assertNotNull( "No outlet found for ID " + outletId, outlet );
		Long deviceId = outlet.getDeviceId();
		assertNull( "Expected outlet to be disconnected, but was connected to device " + deviceId, deviceId);
	}
	
	private Item createPDUItemWithPowerPorts() throws Throwable {
		ItemHome itemHome = (ItemHome)ctx.getBean("itemHome");
		PortHome portHome = (PortHome)ctx.getBean("portHome");
		
		Item pduItem = new Item();
		pduItem.setItemName("testPDUWithPowerPorts");
		pduItem.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
		pduItem.setDataCenterLocation( (DataCenterLocationDetails)session.load(DataCenterLocationDetails.class, 1L) );
		itemHome.saveItem( pduItem );
		session.flush();
		
		// Power Port #1
		PowerPort pp1 = new PowerPort();
		pp1.setItem( pduItem );
		pp1.setPortName("pp1");
		pp1.setSortOrder( 1 );
		portHome.savePowerPort( pp1 );
		session.flush();
		
		// Power Port #2
		PowerPort pp2 = new PowerPort();
		pp2.setItem( pduItem );
		pp2.setPortName("pp2");
		pp2.setSortOrder( 2 );
		portHome.savePowerPort( pp2 );
		session.flush();
		
		// Data Port #1
		DataPort dp1 = new DataPort();
		dp1.setItem( pduItem );
		dp1.setPortName( "dp1" );
		dp1.setSortOrder( 1 );
		portHome.saveDataPort( dp1 );
		session.flush();
		
		return pduItem;
	}

	
	//@Test
	@Test
	public final void testRESTError() throws Throwable {
		PIQRestClientBase rest = new PIQRestClientBase() {
		};
		rest.setCredentials("web_api", "Raritan1$");
		rest.setIPAddress("192.168.51.195");
		rest.setService("v2/events");
		rest.setRestTemplate( (RestTemplate)ctx.getBean("restTemplate") );
		rest.setEventHome( (EventHome)ctx.getBean("eventHome") );
		
		Map<String, Object> post = new HashMap<String, Object>();
		rest.doRestPost( post );
		
		System.out.println("donetest");
	}
}
