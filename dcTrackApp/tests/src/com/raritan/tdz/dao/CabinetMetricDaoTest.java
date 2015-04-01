/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.component.inspector.dao.CabinetMetricDao;

public class CabinetMetricDaoTest extends TestBase {
	CabinetMetricDao metricDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		metricDAO = (CabinetMetricDao)ctx.getBean("cabinetMetricDao");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(56L);
		itemIds.add(57L);
		
		metricDAO.getCabinetItemCount(3L);
		metricDAO.getCabinetBudgetedPower(3L);
		metricDAO.getCabinetListBudgetedPower("SITE A");
		metricDAO.getItemsEffectivePower(itemIds);
				
	}

}
