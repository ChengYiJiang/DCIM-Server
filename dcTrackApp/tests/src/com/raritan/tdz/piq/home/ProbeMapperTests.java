package com.raritan.tdz.piq.home;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPAddress;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemSNMP;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.DeviceJSON;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * Tests for mapping Probes in Power IQ.
 * @author Andrew Cohen
 */
public class ProbeMapperTests extends TestBase {

	private PIQSyncDeviceClient deviceClient;
	private PIQProbeMapper probeMapper;
	private PIQProbeLookup probeLookup;
	private PortHome portHome;
	
	private Item testProbe;
	
	@BeforeMethod
	public void setUp() throws Throwable {	
		super.setUp();
		deviceClient = (PIQSyncDeviceClient)ctx.getBean("piqSyncDeviceClient");
		probeMapper = (PIQProbeMapper)ctx.getBean("piqProbeMapper");
		probeLookup = (PIQProbeLookup)ctx.getBean("piqProbeLookup");
		portHome = (PortHome)ctx.getBean("portHome");
		
		LNHome lnHome = (LNHome)ctx.getBean("listenNotifyHome");
		lnHome.setSuspend(true);
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		if (testProbe != null) {
			session.delete( testProbe );
			session.flush();
		}
		super.tearDown();
	}
	
	@Test
	public void createIPAddress() throws Throwable {
		DataPort dp = portHome.viewDataPortsById( 47L );
		assertNotNull( dp );
		
		IPAddress ip = new IPAddress();
		ip.setIpAddress("255.254.253.252");
		dp.addIpAddress( ip );
		
		session.save( ip );
		session.flush();
		
		assertTrue( ip.getId() > 0 );
	}
	
	//@Test
	@Test
	public void testIPAddressFiltering() throws Throwable {
		final String ipAddress = "192.168.59.220";
		testProbe = createProbeItem("EMX-3434", ipAddress);
		String piqId = null;
		DeviceJSON device = null;
		
		try {
			device = new DeviceJSON(testProbe, ipAddress, 2, false);
			assertNotNull( device.getDevice().getIpAddress() );
			
			// Add Probe device
			piqId = deviceClient.addDevice(testProbe, ipAddress, 2, true);
			assertNotNull( piqId );
			testProbe.setPiqId( Integer.parseInt( piqId ) );
			
			// No IP Address should have been added to PIQ
			device = deviceClient.getDevice( piqId );
			assertNull( device.getDevice().getIpAddress() );
			
			int inSync = deviceClient.isDeviceInSync(testProbe, ipAddress, 2);
			assertEquals( PIQSyncDeviceClientImpl.deviceInSync, inSync );
		}
		catch (Throwable t) {
			throw t;
		}
		finally {
			// Cleanup - delete device
			if (piqId != null) {
				deviceClient.deleteDevice( piqId );
			}
		}
	}
	
	@Test
	public void testSNMP3Updates() throws Throwable {
		final String ip = "1.1.1.2";
		testProbe = createProbeItem("TestProbeWithSNMP3", ip);
		
		// Create the dummy rack PDU
		long pduId = probeMapper.updateProbeRackPDU(testProbe.getItemId(), ip);
		assertTrue(pduId > 0);
		
		// Update the SNMP3 credentials
		ItemSNMP probeSNMP = testProbe.getItemSnmp();
		String newAuthPassKey = probeSNMP.getSnmp3AuthPasskey() + "-mod";
		probeSNMP.setSnmp3AuthPasskey( newAuthPassKey );
		session.update( probeSNMP );
		session.flush();
		
		// Make sure the SNMP credentials on the dummy Rack PDU were updated 
		probeMapper.updateProbeRackPDU(testProbe.getItemId(), ip);
		Item probeRPDU = (Item)session.load(Item.class, pduId);
		assertEquals( newAuthPassKey, probeRPDU.getItemSnmp().getSnmp3AuthPasskey() );
	}
	
	//@Test
	@Test
	public final void testProbeLookup() throws Throwable {
		final long probeId = 440L;
		
		Item pdu = probeLookup.getDummyRackPDUForProbeItem( probeId );
		assertNotNull( pdu );
		
		Item probe = probeLookup.getProbeItemForDummyRackPDU( pdu.getItemId() );
		assertNotNull( probe );
		assertEquals(probeId, probe.getItemId());
	}
	
	
	private Item createProbeItem(String name, String ipAddress) {
		return createProbeItem(name, ipAddress, false);
	}
	
	private Item createProbeItem(String name, String ipAddress, boolean dummy) {
		Item probeItem = new Item();
		
		probeItem.setClassLookup( SystemLookup.getLksData(session, dummy ? SystemLookup.Class.RACK_PDU : SystemLookup.Class.PROBE) );
		
		probeItem.setDataCenterLocation( (DataCenterLocationDetails)session.load(DataCenterLocationDetails.class, 1L) );
		probeItem.setItemName( name );
		probeItem.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		
		DataPort dp1 = new DataPort();
		dp1.setItem( probeItem );
		dp1.setPortName( "Eth0" );
		dp1.setSortOrder( 1 );
		dp1.setIpAddress( ipAddress );
		
		CabinetItem cabItem = new CabinetItem();
		cabItem.setItemName("UNIT-TEST-CAB");
		cabItem.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.CABINET ) );
		cabItem.setDataCenterLocation( probeItem.getDataCenterLocation() );
		session.save( cabItem );
		session.flush();
		
		probeItem.setParentItem( cabItem );
		
		session.save( probeItem );
		session.flush();
		
		ItemSNMP probeSNMP = createItemSNMP();
		probeSNMP.setItem( probeItem );
		session.save( probeSNMP );
		session.flush();
		
		return probeItem;
	}
	
	private ItemSNMP createItemSNMP() {
		ItemSNMP itemSNMP = new ItemSNMP();
		itemSNMP.setPxUserName("pxUser");
		itemSNMP.setPxPassword("pxPassword");
		itemSNMP.setSnmp3AuthLevel("authLevel");
		itemSNMP.setSnmp3AuthPasskey("authKey");
		itemSNMP.setSnmp3AuthProtocol("authProtocol");
		itemSNMP.setSnmp3Enabled( true );
		itemSNMP.setSnmp3PrivPasskey("privPassKey");
		itemSNMP.setSnmp3PrivProtocol("privProtocol");
		itemSNMP.setSnmp3User("snmp3User");
		itemSNMP.setSnmpCommunity_string("comm");
		return itemSNMP;
	}
}
