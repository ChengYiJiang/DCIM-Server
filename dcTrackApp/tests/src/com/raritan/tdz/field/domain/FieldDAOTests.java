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
import com.raritan.tdz.field.dao.FieldsFinderDAO;
import com.raritan.tdz.field.service.FieldService;
import com.raritan.tdz.lookup.SystemLookup;
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
public class FieldDAOTests extends TestBase {
	
	private FieldsFinderDAO fieldsFinderDAO;
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		fieldsFinderDAO = (FieldsFinderDAO)ctx.getBean("fieldsDAO");
	}
	
	@Test
	public void findFieldsByViewId() throws Throwable {
		List<Fields> fields = fieldsFinderDAO.findFieldsByViewId("itemView");
		assertTrue(fields.size() > 0);
	}
	
	@Test
	public void findFieldIdsByViewId() throws Throwable {
		List<Long> fields = fieldsFinderDAO.findFieldIdsByViewId("itemView");
		assertTrue(fields.size() > 0);
	}
	
	@Test
	public void findFieldDetailsByFieldId() throws Throwable {
		List<Long> fields = fieldsFinderDAO.findFieldIdsByViewId("itemView");
		assertTrue(fields.size() > 0);
		
		for (Long field: fields){
			List<FieldDetails> fieldDetails = fieldsFinderDAO.findFieldDetailsByFieldId(field);
			assertTrue(fieldDetails.size() >= 0);
		}
	}
	
	@Test
	public void findUiFieldName() throws Throwable {
		List<String> uiComponentNameList = fieldsFinderDAO.findUiFieldName("tiName");
		assertTrue(uiComponentNameList.size() == 1);
	}
	
	@Test
	public void findFieldDetailsByClass() throws Throwable {
		List<FieldDetails> fieldDetails = fieldsFinderDAO.findFieldDetailsByClass(SystemLookup.Class.DEVICE);
		assertTrue(fieldDetails.size() > 0);
	}
}
