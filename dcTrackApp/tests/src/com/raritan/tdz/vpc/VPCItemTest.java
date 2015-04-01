package com.raritan.tdz.vpc;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vpc.home.VPCHome;

public class VPCItemTest extends TestBase {
	
	private VPCHome vpcHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		
		super.setUp();

		vpcHome = (VPCHome) ctx.getBean("vpcHome");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
		
		// TransactionSynchronizationManager.unbindResource(sf);
	}
	
	@Test
	public final void createVPCItems() throws BusinessValidationException {
		
		List<Long> locationIds = Arrays.asList(1L, 2L, 6L, 7L, 8L, 9L);
		
		for (Long locationId: locationIds) {
			vpcHome.create(locationId, getTestAdminUser());
		}
		
	}

	@Test
	public final void deleteVPCItems() throws BusinessValidationException, ClassNotFoundException, Throwable {

		List<Long> locationIds = Arrays.asList(1L, 2L, 6L, 7L, 8L, 9L);
		
		for (Long locationId: locationIds) {
			vpcHome.delete(locationId, getTestAdminUser());
		}
		
		
	}
	
	@Test
	public final void updateVPCItems() throws BusinessValidationException, ClassNotFoundException, Throwable {
		
		vpcHome.update(1L, getTestAdminUser());
		
	}
	
	@Test
	public final void clearPOUnusedPorts() {
		
		vpcHome.clearPowerOutletPort();
		
	}

}
