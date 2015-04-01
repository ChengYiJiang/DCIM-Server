package com.raritan.tdz.changemgmt.home;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.changemgmt.service.ChangeMgmtService;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.tests.TestBase;

/**
 * Change management tests.
 * @author Andrew Cohen
 */
public class ChangeMgmtTests extends TestBase {

	private ChangeMgmtHome home;
	private ChangeMgmtService service;
	private ChangeMgmtHome26 home26;
	private CircuitPDHome circuitHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		//home = (ChangeMgmtHome)ctx.getBean("changeMgmtHome");
		//service = (ChangeMgmtService)ctx.getBean("changeMgmtService");
		//circuitHome = (CircuitPDHome)ctx.getBean("circuitPDHome");
		home26 = (ChangeMgmtHome26)ctx.getBean("changeMgmtHome26");
	}
	
	@Test
	public final void testReSubmitRequest() throws Throwable {
		home26.reSubmitRequest(176);
	}
}

