/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.customfield.dao.CustomFieldsFinderDAO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.reservation.dao.*;
import com.raritan.tdz.domain.Reservations;
import com.raritan.tdz.tests.TestBase;

public class CustomFieldsDaoTest extends TestBase {
	CustomFieldsFinderDAO customFieldsFinderDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		customFieldsFinderDAO = (CustomFieldsFinderDAO)ctx.getBean("customFieldFinderDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesCustomFieldsByClassLkp() throws Throwable {
	 List<LkuData> lkuData = customFieldsFinderDAO.findClassCustomFieldsByClassLkp(SystemLookup.Class.DEVICE);
	 
	 Assert.assertTrue(lkuData.size() > 0);
	}
	
	@Test
	public final void tesCustomFieldsByModelName() throws Throwable {
	 List<LkuData> lkuData = customFieldsFinderDAO.findClassCustomFieldsByModelName("PowerEdge T310");
	 
	 Assert.assertTrue(lkuData.size() > 0);
	}
	
	@Test
	public final void tesCustomFieldLabelByModelName() throws Throwable {
	 List<String> lkuData = customFieldsFinderDAO.findClassCustomFieldLabelsByModelName("PowerEdge T310");
	 
	 Assert.assertTrue(lkuData.size() > 0);
	}
}
