package com.raritan.tdz.circuit.dao;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.tests.TestBase;

/**
 * Circuit DAO tests
 * 
 * 
 * @author Prasanna Nageswar
 */
public class PowerCircuitDAOTests extends TestBase {

	private PowerCircuitDAO powerCircuitDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		powerCircuitDAO = (PowerCircuitDAO)ctx.getBean("powerCircuitDAO");
		log = Logger.getLogger(PowerCircuitDAO.class);
	}
	
	@Test
	public void testGetPowerWattUsedSummaryBreaker(){
		List<PowerWattUsedSummary> recList = powerCircuitDAO.getPowerWattUsedSummary(7805, null, null, null, false);
		
		assertTrue(recList.size() > 0);
	}	

	@Test
	public void testGetPowerWattUsedSummaryFuse(){
		List<PowerWattUsedSummary> recList = powerCircuitDAO.getPowerWattUsedSummary(9108, null, 900L, null, false);
		
		assertTrue(recList.size() > 0);
	}	
	
	@Test
	public void testGetPowerWattUsedSummaryExcludePort(){
		List<PowerWattUsedSummary> recList = powerCircuitDAO.getPowerWattUsedSummary(7805, 10750L, null, null, false);
		
		assertTrue(recList.size() > 0);
	}	

	@Test
	public void testGetPowerWattUsedTotalBreaker(){
		long total = powerCircuitDAO.getPowerWattUsedTotal(288, null);
		
		assertTrue(total > 0);
	}	
	
}


