/**
 * 
 */
package com.raritan.tdz.piq.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.service.ApplicationSettingsService;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class TestAssetStripsAndPIQSettings extends TestBase {
	
	ApplicationSettingsService applicationSettingsService;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		applicationSettingsService = ctx.getBean("appSettingService",ApplicationSettingsService.class);
	}
	
	@Test
	public void testAddNullPowerIQSetting() throws Throwable {
		int count = applicationSettingsService.updatePIQSettings(null);
		
		Assert.assertEquals(count, 0, "Count is greater than 0!");
	}
	
	@Test
	public void testAddSinglePowerIQSetting() throws Throwable {
		
		//List<ApplicationSettingDTO> settingDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		List<ApplicationSettingDTO> settingDTOList = new ArrayList<>();
		settingDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.59.137", "192.168.59.137"));
		settingDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.59.137"));
		settingDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_PASSWORD, "raritan", "192.168.59.137"));
		settingDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED, "true", "192.168.59.137"));
		settingDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_LABEL, "59.137", "192.168.59.137"));
		
		int count = applicationSettingsService.updatePIQSettings(settingDTOList);
		
		Assert.assertTrue(count >= 4);
	}
	
	@Test
	public void testGetSinglePowerIQSetting() throws Throwable {
		
		List<ApplicationSettingDTO> applicationSettingDTOList = applicationSettingsService.getPIQSettings("192.168.62.26");
		
		Assert.assertTrue(applicationSettingDTOList.size() == 5);
		
		Thread.sleep(60*1000);
	}
	
	@Test
	public void testGetAllPowerIQSettings() throws Throwable {
		List<ApplicationSettingDTO> applicationSettingsDTOList = applicationSettingsService.getPIQSettings();
		
		Assert.assertTrue(applicationSettingsDTOList.size() > 0);
	}
	
	@Test
	public void testAddMultiplePowerIQSettings() throws Throwable {
		//List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		List<ApplicationSettingDTO> applicationSettingsDTOList = new ArrayList<>();
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.57.15", "192.168.57.15"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.57.15"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_PASSWORD, "raritan", "192.168.57.15"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED, "true", "192.168.57.15"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_LABEL, "57.15", "192.168.57.15"));
		
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.60.45", "192.168.60.45"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.60.45"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_PASSWORD, "raritan", "192.168.60.45"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED, "true", "192.168.60.45"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_LABEL, "60.45", "192.168.60.45"));
//		
//		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.62.32", "192.168.62.32"));
//		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.32"));
//		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_PASSWORD, "raritan", "192.168.62.32"));
//		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED, "true", "192.168.62.32"));
//		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_LABEL, "62.32", "192.168.62.32"));
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
		
		Assert.assertTrue(count >= 8);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public void testAddIPAddressPowerIQSettingsOnly() throws Throwable {
		List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.62.35", null));
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public void testAddPowerIQSettingsRepeatUserNameNullParent() throws Throwable {
		List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.62.35", null));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", null));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", null));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", null));
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public void testAddPowerIQSettingsRepeatUserName() throws Throwable {
		List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.62.35", null));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.35"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.35"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.35"));
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public void testAddDuplicatePowerIQSettings() throws Throwable {
		List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_IPADDRESS, "192.168.62.32", null));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.32"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.32"));
		applicationSettingsDTOList.add(getApplicationSettingDTO(-1, SystemLookup.ApplicationSettings.PIQ_USERNAME, "admin", "192.168.62.32"));
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
	}
	
	@Test
	public void testDeletePowerIQSettings() throws Throwable {
		List<String> piqHosts = new ArrayList<String>() {{ 
			add("192.168.62.34");
		}};
		List<ApplicationSettingDTO> applicationSettingsDTOList = removeApplicationSetting(piqHosts);
		
		int count = applicationSettingsService.updatePIQSettings(applicationSettingsDTOList);
		
		Assert.assertTrue(count > 0);
	}
	
	@Test
	public void testDeletePowerIQSettingsUsingIds() throws Throwable {
		List<Integer> piqHostIds = new ArrayList<Integer>() {{
			add(22);
			add(23);
		}};
		
		List<ApplicationSettingDTO> applicationSettingsDTOList = applicationSettingsService.deletePIQSettingsUsingHostIds(piqHostIds);
		
		Assert.assertTrue(applicationSettingsDTOList.size() > 0);
	}
	
	@Test
	public void testDeleteAllPowerIQSettings() throws Throwable {
		int count = applicationSettingsService.updatePIQSettings(null);
		
		Assert.assertTrue(count == 0);
	}
	
	private List<ApplicationSettingDTO> getPIQSettingsWithoutVersion(List<ApplicationSettingDTO> dtos){
		List<ApplicationSettingDTO> result = new ArrayList<>();
		
		for (ApplicationSettingDTO dto:dtos){
			if (dto.getSettingLkpValueCode() != SystemLookup.ApplicationSettings.PIQ_POLLING_INTERVAL 
				&& dto.getSettingLkpValueCode() != SystemLookup.ApplicationSettings.PIQ_EVENT_QUERY_DATE
				&& dto.getSettingLkpValueCode() != SystemLookup.ApplicationSettings.PIQ_VERSION){
				result.add(dto);
			}
		}
		
		return result;
	}
	private ApplicationSettingDTO getApplicationSettingDTO(long settingId, long settingLkpValueCode, String appValue, String parentAppValue) {
		ApplicationSettingDTO applicationSettingDTO = new ApplicationSettingDTO();
		applicationSettingDTO.setAppSettingId(settingId);
		applicationSettingDTO.setSettingLkpValueCode(settingLkpValueCode);
		applicationSettingDTO.setAppValue(appValue);
		applicationSettingDTO.setParentGroupingId(parentAppValue);
		return applicationSettingDTO;
	}
	
	private List<ApplicationSettingDTO> removeApplicationSetting(List<String> piqHosts) throws ServiceLayerException{
		List<ApplicationSettingDTO> applicationSettingsDTOList = getPIQSettingsWithoutVersion(applicationSettingsService.getPIQSettings());
		
		List<ApplicationSettingDTO> result = new ArrayList<>(applicationSettingsDTOList);
		
		for (String piqHost:piqHosts){
			for (ApplicationSettingDTO settingDTO:applicationSettingsDTOList){
				if (settingDTO.getAppValue().equals(piqHost) || settingDTO.getParentAppValue().equals(piqHost))
					result.remove(settingDTO);
			}
		}
		
		return result;
	}
}
