package com.raritan.tdz.item.home;

import static org.junit.Assert.*;
import org.hibernate.criterion.ProjectionList;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import java.math.BigInteger;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Before;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.tests.TestBase;
import org.hibernate.SQLQuery;
import java.util.Date;

/**
 * @author bozana
 * 
 */
public class GenerateItemName extends TestBase {

	private ItemHome itemHome;
	private long max_seq = 99999;
	private long item1_id = -2;
	private long item5_id = -2;

	/** 
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 **/
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemHome = (ItemHome) ctx.getBean("itemHome");
		resetSequenceNumber();
		deleteItem1();
		deleteItem5();
	}

	private long getMaxItemId() {
		Criteria criteria = session
			    .createCriteria(Item.class)
			    .setProjection(Projections.max("itemId"));
			Long maxItemId = (Long)criteria.uniqueResult();
			return maxItemId.longValue(); 
	}
	/**
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 **/
	@AfterMethod
	public void tearDown() throws Throwable {
		resetSequenceNumber();
		deleteItem1();
		deleteItem5();
		super.tearDown();
	}

	private long getCurrentSequenceNumber() {
		long result = 0;
		Query query = session
				.createSQLQuery("select last_value from dct_storage_item_name_seq");

		result = ((BigInteger) query.uniqueResult()).longValue();
		System.out.println("CurrentSequenceNumber = " + result);
		return result;

	}

	private void resetSequenceNumber() {
		long result = 0;
		Query query = session
				.createSQLQuery("select setval ('dct_storage_item_name_seq', 1, false)");
		result = ((BigInteger) query.uniqueResult()).longValue();
		assertEquals(1, result);

	}

	private void addItem1() {
		item1_id = getMaxItemId() + 1;
		Query q1 = session
				.createSQLQuery("INSERT INTO dct_item_details (item_detail_id, description, sys_creation_date, sys_created_by) VALUES (:maxId, 'Bozana Test', '06/12/2012 12:00:00 PM', 'admin')");
		q1.setLong("maxId", item1_id);
		q1.executeUpdate();

		Query q2 = session
				.createSQLQuery("INSERT INTO dct_items (item_id, item_name, class_lks_id, location_id, item_detail_id) VALUES (:maxId,'NoName_00001',1,(select location_id from dct_locations where DC_NAME = 'Site' AND hierarchy_lks_id = (select LKS_ID from dct_lks_data where LKP_VALUE_CODE = 5004 LIMIT 1) LIMIT 1),-1)");
		q2.setLong("maxId", item1_id);
		q2.executeUpdate();
	}

	private void deleteItem1() {
		Query q1 = session
				.createSQLQuery("DELETE from dct_item_details where item_detail_id = :maxId");
		q1.setLong("maxId", item1_id);
		q1.executeUpdate();

		Query q2 = session
				.createSQLQuery("DELETE from dct_items where item_id = :maxId");
		q2.setLong("maxId", item1_id);
		q2.executeUpdate();
	}

	private void addItem5() {
		item5_id = getMaxItemId() + 5;
		Query q1 = session
				.createSQLQuery("INSERT INTO dct_item_details (item_detail_id, description, sys_creation_date, sys_created_by) VALUES (:maxId, 'Bozana Test2', '06/12/2012 12:05:00 PM', 'admin')");
		q1.setLong("maxId", item5_id);
		q1.executeUpdate();

		Query q2 = session
				.createSQLQuery("INSERT INTO dct_items (item_id, item_name, class_lks_id, location_id, item_detail_id) VALUES (:maxId,'NoName_00005',1,(select location_id from dct_locations where DC_NAME = 'Site' AND hierarchy_lks_id = (select LKS_ID from dct_lks_data where LKP_VALUE_CODE = 5004 LIMIT 1) LIMIT 1),-1)");
		q2.setLong("maxId", item5_id);
		q2.executeUpdate();
	}

	private void deleteItem5() {
		
		/*Query q3 = session
				.createSQLQuery("DELETE from dct_items_it where item_id = :maxId");
		q3.setLong("maxId", item5_id);
		q3.executeUpdate();*/
		
		Query q2 = session
				.createSQLQuery("DELETE from dct_items where item_id = :maxId");
		q2.setLong("maxId", item5_id);
		q2.executeUpdate();

		Query q1 = session
				.createSQLQuery("DELETE from dct_item_details where item_detail_id = :maxId");
		q1.setLong("maxId", item5_id);
		q1.executeUpdate();
		
	}

	/**
	 * Test description: This test first adds two storage items that will 
	 * 			have generated names, then checks if next sequence starts from 3
	 * 			and generates few names.
	 * @throws Throwable
	 */
	//@Test
	public void testUniqueStorageNameSkipFirstTwo() throws Throwable {
		System.out.println("in testUniqueStorageNameSkipFirstTwo()");

		// Add item name NoName_00001 and NoName_00005 to the dct_items table
		addItem1();
		addItem5();
		String actualName = null;
		String expectedName = null;

		actualName = itemHome.getGeneratedStorageItemName();
		// Generated name should be NoName_00002 since NoName_00001 already
		// exists
		expectedName = String.format("NoName_%05d", 2);
		System.out.println("exptectedName=" + expectedName);
		System.out.println("actualName=" + actualName);
		assertEquals(expectedName, actualName);
		System.out.println("point1: everything good");

		// Next 2 seq numbers: 00003 and 00004 should be available
		for (int i = 3; i < 5; i++) {
			actualName = itemHome.getGeneratedStorageItemName();
			expectedName = String.format("NoName_%05d", i);
			System.out.println("exptectedName=" + expectedName);
			System.out.println("actualName=" + actualName);
			assertEquals(expectedName, actualName);
		}
		System.out.println("point2: everything good");

		// Next 5th sequence should not be available and we should get 6th
		actualName = itemHome.getGeneratedStorageItemName();
		expectedName = String.format("NoName_%05d", 6);
		System.out.println("exptectedName=" + expectedName);
		System.out.println("actualName=" + actualName);
		assertEquals(expectedName, actualName);
		System.out.println("point3: everything good");

		deleteItem1();
		deleteItem5();

	}

	/**
	 * Test description: This is a long lasting test. It checks all sequence values, 
	 * 				generated names and verifies that sequence wraps around when it
	 * 				reaches max value
	 * @throws Throwable
	 */
	@Test
	public void testUniqueNameLongLasting() throws Throwable {
		System.out.println("in testUniqueNameLongLasting()");
		String actualName = null;
		String expectedName = null;
		// Test all max_seq combinations
		for (int i = 1; i <= max_seq; i++) {
			actualName = itemHome.getGeneratedStorageItemName();
			expectedName = String.format("NoName_%05d", i);
			System.out.printf("Idx = %d, exptectedName = %s, actualName =%s\r", i, expectedName, actualName);
			assertEquals(expectedName, actualName);
		}
		// Now check if counter wraps around
		for (int i = 1; i <= 5; i++) {
			actualName = itemHome.getGeneratedStorageItemName();
			expectedName = String.format("NoName_%05d", i);
			System.out.println("exptectedName=" + expectedName);
			System.out.println("actualName=" + actualName);
			assertEquals(expectedName, actualName);
		}
	}

	/**
	 * Test description: This test has been used to manually test Business validation 
	 * 			exception. I had to change source code in order to run it. 
	 * 			So, it is disabled for auto running.
	 * @throws Throwable
	 */
	@Test (enabled=false) 
	public void simpleTest() throws Throwable {
		System.out.println("in simpleTest()");
		String actualName = null;
		
		for(int i=0; i<10; i++){
			try {
				Date curr_time1 = new Date();
				actualName = itemHome.getGeneratedStorageItemName();
				Date curr_time2 = new Date();
				long diff = curr_time2.getTime() - curr_time1.getTime();
				System.out.println("actualName=" + actualName + "time=" + diff);
			} catch (BusinessValidationException ex) {
				System.out.println("Got BusinessValidationException");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Test description:  Use this test case to quickly test generated names. It just checks 
	 * 			first few sequences	and verifies that names are generated correctly. 
	 * @throws Throwable
	 */
	@Test
	public void testUniqueNameQuick() throws Throwable {
		System.out.println("in testUniqueNameQuick()");
		String actualName = null;
		String expectedName = null;
		for (int i = 0; i < 5; i++) {
			long curr_seq = getCurrentSequenceNumber();
			long expected_seq_num = i+1;
			expectedName = String.format("NoName_%05d", expected_seq_num);
			actualName = itemHome.getGeneratedStorageItemName();
			System.out.println("actualName=" + actualName);
			System.out.println("expectedName=" + expectedName);
			assertEquals(expectedName, actualName);
		}
	}

	/**
	 * Test description: This test case times how long it takes to generate name
	 * 				It has been created to test if performances are good.
	 * 				(When tested measured value was about 18millisec)
	 * @throws Throwable
	 */
	@Test
	public void timeAPI() throws Throwable {
		String actualName = null;
		String expectedName = "NoName_00001";

		Date curr_time1 = new Date();
		actualName = itemHome.getGeneratedStorageItemName();
		Date curr_time2 = new Date();

		System.out.println("exptectedName=" + expectedName);
		System.out.println("actualName=" + actualName);
		assertEquals(expectedName, actualName);
		long diff = curr_time2.getTime() - curr_time1.getTime();
		System.out.println("operation completed in: " + diff + "milliseconds");

	}

}
