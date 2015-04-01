package com.raritan.tdz.ip;

import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.dao.NetworksDAO;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.home.IPServiceRESTAPI;
import com.raritan.tdz.tests.TestBase;

public class NetworksDAOTest extends TestBase {
	NetworksDAO networksDAO;
	IPAddressDetailsDAO ipAddressDetailsDAO;
	IPServiceRESTAPI ipService;


	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		networksDAO = (NetworksDAO)ctx.getBean("networksDAO");
		ipAddressDetailsDAO = (IPAddressDetailsDAO)ctx.getBean("ipAddressDetailsDAO");
		ipService = (IPServiceRESTAPI)ctx.getBean("ipServiceRESTAPI");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public void testGetNetworks() throws BusinessValidationException
	{
		System.out.println("in testGetNetworks");
		List<Networks> nets = networksDAO.getNetworkForIpAndLocation("10.1.0.5", 1L);
		assert(nets != null && nets.size() > 0);
		Networks net = nets.get(0);
		String expGateway = "10.1.0.1";
		System.out.println("## gateway=" + net.getGateway());
		assert(expGateway.equals(net.getGateway()));

		nets = networksDAO.getNetworkForIpAndLocation("10.2.4.5", 1L);
		assert(nets != null && nets.size() > 0);
		net = nets.get(0);
		expGateway = "10.2.4.1";
		System.out.println("## gateway=" + net.getGateway());
		assert(expGateway.equals(net.getGateway()));

		nets = networksDAO.getNetworkForIpAndLocation("62.2.4.5", 1L);
		assert(nets != null && nets.size() == 0 );

		nets = networksDAO.getNetworkForIpAndLocation("10.3.13.143", 1L);
		assert(nets != null && nets.size() > 0 );
		net = nets.get(0);
		expGateway = "10.3.12.1";
		System.out.println("## gateway=" + net.getGateway());
		assert(expGateway.equals(net.getGateway()));

		String ipAddress = "192.168.51.106";
		Map<String, Object> netsMap = ipService.getSubnetForIPAndLocationExtAPI(ipAddress, 1L);

		System.out.println("Got nets");

	}
}
