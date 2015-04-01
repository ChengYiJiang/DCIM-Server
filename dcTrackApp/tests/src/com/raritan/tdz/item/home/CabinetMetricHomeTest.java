package com.raritan.tdz.item.home;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.component.inspector.home.CabinetMetricHome;
import com.raritan.tdz.tests.TestBase;

public class CabinetMetricHomeTest extends TestBase {

	CabinetMetricHome metricHome;
	
	@BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	                                             
    	metricHome = (CabinetMetricHome)ctx.getBean("cabinetMetricHome");
    }
	
	@Test
	public final void testGetPowerPowerMetrics() throws Throwable {
		System.out.println("\n\n");
		
		CabinetMetricDto cab = metricHome.getCabinetMetrics(3L, "1");
		
		System.out.println(cab);
		
		System.out.println("\n\n");
		
	}
}
    