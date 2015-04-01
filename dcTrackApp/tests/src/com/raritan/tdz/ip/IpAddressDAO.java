package com.raritan.tdz.ip;

import java.util.List;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.tests.TestBase;

public class IpAddressDAO extends TestBase {
	IPAddressDetailsDAO ipAddrDetailsDAO;
	//IPTeamingDAO ipTeamingDAO;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		ipAddrDetailsDAO = (IPAddressDetailsDAO)ctx.getBean("ipAddressDetailsDAO");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void testGetIpAddressDetailsByIpId() throws Throwable {
		System.out.println("===== testGetIpAddressDetailsByIpAddress()");
		Long ipId = 1L;
		IPAddressDetails ip = ipAddrDetailsDAO.read(ipId);
		System.out.println("ipAddress=" + ip.getIpAddress() + ", netmask=" + ip.getMask().getMask());
		Set<DataPort> dataPorts = ip.getDataPortsUsingIP();
		if( dataPorts.size() > 0){
			for( DataPort dp : dataPorts ){
				System.out.println("item id=" + dp.getItem().getItemId() + ", dp NAME=" + dp.getPortName() + 
						", dp id=" + dp.getPortId());

			}
		}
		IPAddressDetails ip2 = ipAddrDetailsDAO.read(288L);
		System.out.println("ipAddress=" + ip2.getIpAddress() + ", netmask=" + ip2.getMask().getMask());		
	}

	@Test
	public final void testGetIpAddressDetailsByIpAddress() throws Throwable {
		System.out.println("===== testGetIpAddressDetailsByIpAddress()");
		String ipAddress = "10.3.9.18";
		List<IPAddressDetails> ips = ipAddrDetailsDAO.getIpAddressByName(ipAddress, 1L);
		IPAddressDetails ip = ips.get(0);
		System.out.println("ipAddress=" + ip.getIpAddress() + ", netmask=" + ip.getMask().getMask());
		Set<DataPort> dataPorts = ip.getDataPortsUsingIP();
		if( dataPorts.size() > 0){
			for( DataPort dp : dataPorts ){
				System.out.println("item id=" + dp.getItem().getItemId() + ", dp NAME=" + dp.getPortName() + 
						", dp id=" + dp.getPortId());

			}
		}
		IPAddressDetails ip2 = ipAddrDetailsDAO.read(288L);
		System.out.println("ipAddress=" + ip2.getIpAddress() + ", netmask=" + ip2.getMask().getMask());		
	}

	@Test
	public final void testGetIPAddressesForPortId() throws Throwable {
		System.out.println("===== testGetIPAddressesForPortId()");
		List<IPAddressDetails> ips = ipAddrDetailsDAO.getIpAddressForDataPort(8504L);
		if( ips.size() > 0 ){
			for(IPAddressDetails ip : ips ){
				System.out.println("ip=" + ip.getIpAddress() + ", netmask=" + ip.getMask().getMask());
			}
		}
	}

	@Test
	public final void testGetIPAddressesForItem() throws Throwable {
		System.out.println("===== testGetIPAddressesForItem()");
		List<IPAddressDetails> ips = ipAddrDetailsDAO.getIpAddressForItem(233L);
		if( ips.size() > 0 ){
			for(IPAddressDetails ip : ips ){
				System.out.println("ip=" + ip.getIpAddress() + ", netmask=" + ip.getMask().getMask());
			}
		}
	}

	@Test
	public final void testGetIPAddressesesInLocation() throws Throwable {
		System.out.println("===== testGetIPAddressesForItem()");
		List<String> ips = ipAddrDetailsDAO.getAllUsedIPAddressesInSubnet(1L);
		if( ips.size() > 0 ){
			for(String ip : ips ){
				System.out.println("ip=" + ip);
			}
		}
		System.out.println("got " + ips.size() + " ip addresses");
	}

/*	@Test
	public final void testGetAllTeamedIPAddressesesInLocation() throws Throwable {
		System.out.println("===== testGetIPAddressesForItem()");
		List<String> ips = ipAddrDetailsDAO.getAllTeamedIPAddressesInLocation(1L);
		if( ips.size() > 0 ){
			for(String ip : ips ){
				System.out.println("ip=" + ip);
			}
		}
		System.out.println("got " + ips.size() + " ip addresses");
	}*/

	@Test
	public final void testGetAllGatewaysInSubnet() throws Throwable {
		System.out.println("===== testGetIPAddressesForItem()");
		List<String> ips = ipAddrDetailsDAO.getAllGatewaysInSubnet(1L, "10.3.12.0");
		if( ips.size() > 0 ){
			for(String ip : ips ){
				System.out.println("ip=" + ip);
			}
		}
		System.out.println("got " + ips.size() + " ip addresses");
	}

	@Test
	public final void testGetIPAddressesUsingGateway() throws Throwable {
		System.out.println("====== testGetIPAddressesUsingGateway()");
		List<IPAddressDetails> ips = ipAddrDetailsDAO.getIPAddressUsingGateway("10.1.8.1");
		if( ips.size() > 0 ){
			for(IPAddressDetails ip : ips ){
				System.out.println("ip=" + ip.getIpAddress() + ", netmask=" + ip.getMask().getMask());
			}
		}
	}
}

