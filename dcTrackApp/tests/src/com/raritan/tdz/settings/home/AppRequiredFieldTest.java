/**
 * 
 */
package com.raritan.tdz.settings.home;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.settings.service.ApplicationSettingsService;
import com.raritan.tdz.tests.TestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */
public class AppRequiredFieldTest extends TestBase {
	

	//private static final String EVENT_SOURCE = "Unit Test";
	
	private ApplicationSettingsService appSettingService;
        
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		appSettingService = (ApplicationSettingsService)ctx.getBean("appSettingService");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		//clearEvents();
		super.tearDown();
	}
	
	@Test
	public void testGetUiFieldsInitState() throws Throwable {
		System.out.println("in testGetUiFieldsInitState()");

        Map<String, UiComponentDTO> uiMap = appSettingService.getUiFieldsInitState();
        
        for(UiComponentDTO uiField:uiMap.values()){
            System.out.println("Field Name: " + uiField.getUiId() + " Required = " + uiField.isRequired());
        }
                    
	}    
	
	@Test
	public void testUpdateRequiredUiFields() throws Throwable {
		System.out.println("in testUpdateRequiredUiFields()");
		
		List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
		ValueIdDTO rec = new ValueIdDTO();
		rec.setLabel("tiClass");
		rec.setData(false);
		recList.add(rec);
		
		rec = new ValueIdDTO();
		rec.setLabel("tiName");
		rec.setData(true);
		recList.add(rec);
		
		rec = new ValueIdDTO();
		rec.setLabel("cmbMake");
		rec.setData(false);
		recList.add(rec);
		
		appSettingService.updateRequiredUiFields(recList);
		
	}  	
}
