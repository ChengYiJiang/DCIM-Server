package com.raritan.tdz.vpc;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vpc.home.VPCHome;

/**
 * test creating circuits using vpc items
 * @author bunty
 *
 */
public class VPCCircuitTest extends TestBase {

	private VPCHome vpcHome;
	
	private UserInfo userInfo;
	
	private PowerCircuitHome powerCircuitHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		
		super.setUp();

		userInfo = getTestAdminUser();
		
		vpcHome = (VPCHome) ctx.getBean("vpcHome");
		
		powerCircuitHome = (PowerCircuitHome) ctx.getBean("powerCircuitHome");

	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
		
	}
	
	@Test
	public final void createCircuitFromInputCordUsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.createVPCCircuit("A", 1L, 39900L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}
	
	@Test
	public final void createCircuitFromPowerSupplyUsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.createVPCCircuit("A", 1L, 11025L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}
	
	@Test
	public final void createCircuitFromPowerSupply_120_240_SP_3_UsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.createVPCCircuit("A", 1L, 54633L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}
	
	@Test
	public final void createCircuitFromPowerSupply_120_240_SP_2_UsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.createVPCCircuit("A", 1L, 54642L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}

	@Test
	public final void createCircuitFromPowerSupply_120_SP_3_UsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.createVPCCircuit("A", 1L, 54644L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}

	@Test
	public final void createTempCircuitFromPowerSupply_120_SP_3_UsingVPC() throws NumberFormatException, BusinessValidationException, DataAccessException {
		
		PowerCircuit powerCircuit = vpcHome.getVPCPartialCircuit("A", 1L, 54644L, userInfo);

		System.out.println("Circuit = " + powerCircuit.getCircuitId());
		
	}
	
	@Test
	public final void getVPCCircuitView() throws DataAccessException, BusinessValidationException {
		
		CircuitCriteriaDTO c = new CircuitCriteriaDTO();
		c.setStartPortId(54586L); // set the start port id for the input cord or the power supply port
		c.setLocationId(1L);
		c.setVpcChainLabel("A");

		List<PowerCircuit> circuits = powerCircuitHome.viewPowerCircuitByCriteria(c);

		for (PowerCircuit powerCircuit: circuits) {
			System.out.println("Circuit = " + powerCircuit.getCircuitId());
		}
		
	}

}
