/**
 * 
 */
package com.raritan.tdz.settings.home;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.List;
import java.util.UUID;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.settings.service.ApplicationSettingServiceImpl;
import com.raritan.tdz.settings.service.ApplicationSettingsService;
import com.raritan.tdz.tests.TestBase;

/**
 * Tests application settings business layer.
 * 
 * @author Andrew Cohen
 */
public class AppSettingsTest extends TestBase {

	private ApplicationSettings home;
	private ApplicationSettingsService appSettingService;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		home = (ApplicationSettings)ctx.getBean("appSettings");
		appSettingService = (ApplicationSettingsService)ctx.getBean("appSettingService");
	}

	@Test
	public final void testReadWriteApplicationSettings() throws DataAccessException {
		for (Name name : Name.values()) {
			// Assert we have a value set
			// Need to set PIQ Host in order to get
			// correct setting value for right host  as below
			// home.setPowerIQHost("192.168.62.15");
			
			// skip vpc settings because there are multiple rows in db for vpc_enabled,
			/// each row for one location. It uses the locaionId to make it unique. 
			// since getProperty takes only one argument, it may fetch wrong data. 
			if (name.equals(Name.VPC_ENABLED)) continue;
			
			String origValue = home.getProperty( name );
			assertNotNull("Setting '" + name + "' has no value!", origValue );
			
			// Assert we can change the value
			String newValue = UUID.randomUUID().toString();
			home.setProperty(name, newValue);
			assertEquals(newValue, home.getProperty(name));
			
			// Assert changing back to the original value
			home.setProperty(name, origValue);
			assertEquals(origValue, home.getProperty(name));
		}
	}
	
	@Test
	public void getApplicationSettingsTest() throws Throwable {
		List<ApplicationSettingDTO> dtos = appSettingService.getApplicationSettings();
		for (ApplicationSettingDTO dto: dtos) {
			System.out.println("dto = " + dto.toString());
		}
	}
}
