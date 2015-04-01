package com.raritan.tdz.diagnostics;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public class DiagnosticsEventTests extends TestBase {
	
	protected LksData insertEvt;
	protected LksData updateEvt;
	protected LksData deleteEvt;
	DiagnosticsHome diagnosticsHome = null;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		diagnosticsHome = (DiagnosticsHome)  ctx.getBean("diagnosticsHome");
		
		insertEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT);
		updateEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE);
		deleteEvt = SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE);
		
		System.out.println("diagnostics test setup complete");
	}

	  @Test
	  public void testPowerPortDiagnostics() throws BusinessValidationException {
	  
		  LNEvent diagnosePowerPorts = new LNEvent(100, updateEvt, "dct_ports_power", 
					-1, null, null, DiagnosticsLookup.Action.DIAGNOSE_POWER_PORTS);
		
		  diagnosticsHome.processLNEvent(diagnosePowerPorts);
	  
	  }

	  @Test
	  public void testItemPowerPortDiagnostics() throws BusinessValidationException {
	  
		  LNEvent diagnoseItemPowerPorts = new LNEvent(100, updateEvt, "dct_ports_power", 
					-1, null, null, DiagnosticsLookup.Action.DIAGNOSE_ITEM_POWER_PORTS);
		
		  diagnosticsHome.processLNEvent(diagnoseItemPowerPorts);
	  
	  }

	  @Test
	  public void testPowerConnectionDiagnostics() throws BusinessValidationException {
	  
		  LNEvent diagnosePowerConnections = new LNEvent(100, updateEvt, "dct_ports_power", 
					-1, null, null, DiagnosticsLookup.Action.DIAGNOSE_POWER_CONNECTIONS);
		
		  diagnosticsHome.processLNEvent(diagnosePowerConnections);
	  
	  }

	  @Test
	  public void testPowerPortLoadDiagnostics() throws BusinessValidationException {
	  
		  LNEvent diagnosePowerConnections = new LNEvent(100, updateEvt, "dct_ports_power", 
					-1, null, null, DiagnosticsLookup.Action.DIAGNOSE_POWER_PORT_LOAD);
		
		  diagnosticsHome.processLNEvent(diagnosePowerConnections);
	  
	  }

	  

}
