/**
 * 
 */
package com.raritan.tdz.field.domain;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.field.service.FieldService;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.tests.TestBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */
public class FieldTests extends TestBase {
	//private static final String EVENT_SOURCE = "Unit Test";
	
	private FieldService fieldService;
	private FieldHome fieldHome;
	  
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		fieldService = (FieldService)ctx.getBean("fieldService");
		fieldHome = (FieldHome)ctx.getBean("fieldHome");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	

	@Test
	public void testCustomFieldsError() throws Throwable {
		System.out.println("######### UnitTest: testGetFields() ##########");
		try {
			fieldHome.isThisFieldRequiredAtSave("tiCustomField", null, 1200L);
			
		} catch ( Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testGetFields() throws Throwable {
		System.out.println("######### UnitTest: testGetFields() ##########");
		try {
        	Map<String, UiComponentDTO> uiMap = fieldService.getFields(1200); //1200 for device
        	assertTrue((uiMap.size() > 0), "uiMap of fields is <= 0");
        
        	for(UiComponentDTO uiField:uiMap.values()){
            	System.out.println("Field Name: " + uiField.getUiId() + " Required = " + uiField.isRequired());
        	}
		} catch ( ServiceLayerException e) {
			System.out.println("service layer exception");
			e.printStackTrace();
			fail();
		}

	}
	
	@Test
	public void testGetAllFields() throws Throwable {
		System.out.println("######### UnitTest: testGetFields() ##########");
		try {
        	Map<String, UiComponentDTO> uiMap = fieldService.getFields(0); //0 -> all classes
        	assertTrue((uiMap.size() > 0), "uiMap of fields is <= 0");
        
        	for(UiComponentDTO uiField:uiMap.values()){
            	System.out.println("Field Name: " + uiField.getUiId() + " Required = " + uiField.isRequired());
        	}
		} catch ( ServiceLayerException e) {
			System.out.println("service layer exception");
			e.printStackTrace();
			fail();
		}

	} 
	
	@Test
	public void testGetFieldsList() throws Throwable {
		System.out.println("######### UnitTest: testGetFields() ##########");
		Long DEVICE_LKP_VALUE_CODE = 1200L;
		List<UiComponentDTO> fieldList = fieldService.getFieldDetail(DEVICE_LKP_VALUE_CODE);
    	assertTrue((fieldList.size() > 0), "fields list of fields is <= 0");
    
    	Iterator<UiComponentDTO> it = fieldList.iterator();
    	while (it.hasNext()) {
    		UiComponentDTO fd = it.next();
        	System.out.println("Class Name = " + fd.getUiLabel() + "Field Name: " + fd.getUiId() + " Required = " + fd.isRequired() + " Configurable = " + fd.getIsConfigurable());
    	}
	} 
	
	//@Test This is covered by testSaveFieldsAndVerify()
	public void testSaveFields() throws Throwable {
		System.out.println("######### UnitTest: testSaveFields() ###########");

		Long DEVICE_LKP_VALUE_CODE = 1200L;
		Boolean newValue = true;
		List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO rec = new ValueIdDTO();
		rec.setLabel("tiCustomField_1");
		rec.setData(newValue);
		recList.add(rec);
		
		rec = new ValueIdDTO();
		rec.setLabel("tiCustomField_3");
		rec.setData(newValue);
		recList.add(rec);
		
		rec = new ValueIdDTO();
		rec.setLabel("tiCustomField_4");
		rec.setData(newValue);
		recList.add(rec);
		
		fieldService.saveFields(recList, DEVICE_LKP_VALUE_CODE);
	}

	private void verifyData (String uid, Boolean newValue, Map<String, UiComponentDTO> currentUiMap) {
		
		for(UiComponentDTO uiField:currentUiMap.values()) {
			if (uiField.getUiId().equals(uid)) {
				System.out.println ("uIid = " + uiField.getUiId() + ", isRequired = " + uiField.isRequired() + " uid = " + uid + " newValue = " + newValue);
				assertTrue((uiField.isRequired() != newValue), "newValue and OldValue are same");
			}
		}
	}
	 
	private void saveOriginalValues(Long DEVICE_LKP_VALUE_CODE, Map<String, UiComponentDTO> currentUiMap) {
		List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();

		// For each item new new value for isRequiredField;
		for(UiComponentDTO uiField:currentUiMap.values()) {
			ValueIdDTO rec = new ValueIdDTO();
			rec.setLabel(uiField.getUiId());
			rec.setData(uiField.isRequired());
			recList.add(rec);
		}
		try {
			// Save new values to the database
			fieldService.saveFields(recList, DEVICE_LKP_VALUE_CODE);
		} catch ( ServiceLayerException e) {
			System.out.println("service layer exception");
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	public void testSaveFieldsAndVerify() throws Throwable {
		System.out.println("######### UnitTest: testSaveFields() ##########");

		Long DEVICE_LKP_VALUE_CODE = 1200L;
		List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
		
		// Get list of Ui fields for device lkp_value_code
		Map<String, UiComponentDTO> currentUiMap = fieldService.getFields(DEVICE_LKP_VALUE_CODE);


		// For each item new new value for isRequiredField;
		for(UiComponentDTO uiField:currentUiMap.values()) {
			Boolean newVal = !(uiField.isRequired());
			ValueIdDTO rec = new ValueIdDTO();
			System.out.println ("Original Value : uIid =" + uiField.getUiId() + ", isRequired = " + uiField.isRequired() + " , newVal = " + newVal );
			rec.setLabel(uiField.getUiId());
			rec.setData(newVal);
			recList.add(rec);
		}
		try {
			// Save new values to the database
			fieldService.saveFields(recList, DEVICE_LKP_VALUE_CODE);
		} catch ( ServiceLayerException e) {
			System.out.println("service layer exception");
			e.printStackTrace();
			fail();
		}

		// Read the values and verify.
		Map<String, UiComponentDTO> newUiMap = fieldService.getFields(DEVICE_LKP_VALUE_CODE);

		// For each item verify new value for isRequiredField field;
		for(UiComponentDTO newUiField:newUiMap.values()) {
			verifyData (newUiField.getUiId(), newUiField.isRequired(), currentUiMap);
		}
		
		saveOriginalValues(DEVICE_LKP_VALUE_CODE, currentUiMap); 
	}

	//@Test
	public void testSaveAllClassFieldsAndVerify() throws Throwable {
		System.out.println("######### UnitTest: testSaveFields() ##########");

		Long DEVICE_LKP_VALUE_CODE = 0L;
		List<ValueIdDTO> recList = new ArrayList<ValueIdDTO>();
		
		// Get list of Ui fields for device lkp_value_code
		Map<String, UiComponentDTO> currentUiMap = fieldService.getFields(DEVICE_LKP_VALUE_CODE);

		// For each item new new value for isRequiredField;
		for(UiComponentDTO uiField:currentUiMap.values()) {
			Boolean newVal = !(uiField.isRequired());
			ValueIdDTO rec = new ValueIdDTO();
			System.out.println ("Original Value : uIid =" + uiField.getUiId() + ", isRequired = " + uiField.isRequired() + " , newVal = " + newVal );
			rec.setLabel(uiField.getUiId());
			rec.setData(newVal);
			recList.add(rec);
		}
		try {
			// Save new values to the database
			fieldService.saveFields(recList, DEVICE_LKP_VALUE_CODE);
		} catch ( ServiceLayerException e) {
			System.out.println("service layer exception");
			e.printStackTrace();
			fail();
		}

		// Read the values and verify.
		Map<String, UiComponentDTO> newUiMap = fieldService.getFields(DEVICE_LKP_VALUE_CODE);

		// For each item new new value for isRequiredField;
		for(UiComponentDTO newUiField:newUiMap.values()) {
			verifyData (newUiField.getUiId(), newUiField.isRequired(), currentUiMap);
		}
		
		saveOriginalValues(DEVICE_LKP_VALUE_CODE, currentUiMap); 
	}

}
