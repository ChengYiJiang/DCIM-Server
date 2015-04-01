package com.raritan.tdz.dao;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.ip.dao.NetMaskDAO;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.tests.TestBase;

public class NetMaskDAOTest extends TestBase {
	private NetMaskDAO netMaskDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		netMaskDAO = (NetMaskDAO)ctx.getBean("netMaskDAO");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		NetMask netMask = netMaskDAO.read(2L);
		Assert.assertTrue( netMask != null );
		System.out.println("mask=" + netMask.getMask());
		
		List<NetMask> netMask2 = netMaskDAO.getById(2L);
		Assert.assertTrue( netMask2 != null );
		Assert.assertTrue(netMask2.size() == 1);
		Assert.assertTrue(netMask.getMask().equals(netMask2.get(0).getMask()));
		
		List<NetMask> netMask3 = netMaskDAO.getByCidr(31L);
		Assert.assertTrue(netMask3.size() > 0);
		
		List<NetMask> netMask4 = netMaskDAO.getByMask("255.255.255.254");
		Assert.assertTrue(netMask4.size() > 0);
		
		List<NetMask> netMask5 = netMaskDAO.getAll();
		Assert.assertTrue(netMask5.size() > 0);
		
	}
	}
