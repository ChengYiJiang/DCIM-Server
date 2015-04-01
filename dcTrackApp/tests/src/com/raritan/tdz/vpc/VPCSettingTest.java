package com.raritan.tdz.vpc;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.service.ApplicationSettingsService;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vpc.home.VPCHome;

public class VPCSettingTest extends TestBase {

	private ApplicationSettingsService appSettingService;
	
	private VPCHome vpcHome;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		
		super.setUp();

		appSettingService = (ApplicationSettingsService) ctx.getBean("appSettingService");

		vpcHome = (VPCHome) ctx.getBean("vpcHome");

	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
		
	}
	
	@Test
	public final void enableVPCForAllLocation() throws ServiceLayerException {
		
		List<ApplicationSettingDTO> appSettings = appSettingService.getVPCSettings();
		
		for (ApplicationSettingDTO dto: appSettings) {
			
			dto.setAppValue("true");
			
		}
		
		appSettingService.updateVPCSettings(appSettings);
		
		getVPCSettings();
		
	}

	@Test
	public final void disableVPCForAllLocation() throws ServiceLayerException {
		
		List<ApplicationSettingDTO> appSettings = appSettingService.getVPCSettings();
		
		for (ApplicationSettingDTO dto: appSettings) {
			
			dto.setAppValue("false");
			
		}
		
		appSettingService.updateVPCSettings(appSettings);
		
		getVPCSettings();
		
	}

	@Test
	public final void getVPCSettings() throws ServiceLayerException {
		
		List<ApplicationSettingDTO> appSettings = appSettingService.getVPCSettings();
		
		for (ApplicationSettingDTO appSetting: appSettings) {
			
			System.out.println("App Setting : " + appSetting.toString());
			
		}
		
	}

}
