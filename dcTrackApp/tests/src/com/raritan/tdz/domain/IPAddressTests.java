package com.raritan.tdz.domain;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.Set;

import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * Tests for the IP Address domain object.
 * @author andrewc
 */
public class IPAddressTests extends TestBase {

	private PortHome portHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {	
		super.setUp();
		portHome = (PortHome)ctx.getBean("portHome");
		
		LNHome lnHome = (LNHome)ctx.getBean("listenNotifyHome");
		lnHome.setSuspend(true);
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
	@Test
	public void testAddMultiIpAddresses() throws Throwable {
		// TODO: Dynamically create data ports
		final long dataPort1 = 562L;
		final long dataPort2 = 46L;
		
		DataPort dp1 = portHome.viewDataPortsById( dataPort1 );
		AssertJUnit.assertNotNull( dp1 );
		DataPort dp2 = portHome.viewDataPortsById( dataPort2 );
		AssertJUnit.assertNotNull( dp2 );
		
		final IPAddress ip1 = new IPAddress("1.1.1.1");
		session.save( ip1 );
		
		// Add first IP Address to both data ports
		dp1.addIpAddress( ip1 );
		session.update( dp1 );
		session.flush();
		
		dp2.addIpAddress( ip1 );
		session.update( dp1 );
		session.flush();
		
		dp1 = portHome.viewDataPortsById( dataPort1 );
		Set<IPAddress> dp1Ips = dp1.getIpAddresses();
		AssertJUnit.assertEquals(1, dp1Ips.size());
		
		dp2 = portHome.viewDataPortsById( dataPort2 );
		Set<IPAddress> dp2Ips = dp2.getIpAddresses();
		AssertJUnit.assertEquals(1, dp2Ips.size());
		
		IPAddress dp1Ip1 = dp1Ips.iterator().next();
		IPAddress dp2Ip1 = dp2Ips.iterator().next();
		AssertJUnit.assertEquals( dp1Ip1.getId(), dp2Ip1.getId() );
		AssertJUnit.assertEquals( dp1Ip1, dp2Ip1 );
	}
	
}
