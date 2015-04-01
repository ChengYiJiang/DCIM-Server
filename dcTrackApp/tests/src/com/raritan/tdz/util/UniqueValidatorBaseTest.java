/**
 * 
 */
package com.raritan.tdz.util;

import static org.junit.Assert.*;
import org.hibernate.criterion.ProjectionList;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.tests.TestBase;
import org.hibernate.SQLQuery;

/**
 * @author bozana
 *
 */
public class UniqueValidatorBaseTest extends TestBase {

	private ItemHome itemHome;
	private UniqueValidatorBase uniqueValidator;
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemHome = (ItemHome) ctx.getBean("itemHome");
		uniqueValidator = (UniqueValidatorBase) ctx.getBean("uniqueValidator");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	
	private Boolean testCore( String uiId, String value, String siteCode){
		Boolean retval = false;
		try {
			retval = itemHome.isUnique(uiId, value, siteCode, -1L, null, null);
			System.out.println("retval = " + retval.toString());
		}catch( DataAccessException e){
			System.out.println("Data access exception");
			e.printStackTrace();
			fail();
		}catch(ClassNotFoundException e){
			System.out.println("Class not found excpetion");
			e.printStackTrace();
			fail();
		}
		System.out.println("Done: retVal=" + retval);
		return retval;
	}
	
	private String addRaritanTag(String newRaritanTag)
	{
		SessionFactory sessionFactory = uniqueValidator.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		String SQL_QUERY1 = "select raritan_tag from dct_items where item_id = 4688";
		List resp = session.createSQLQuery(SQL_QUERY1).list();
		String oldRaritanTag = (String)resp.get(0);
		System.out.println("before update; raritan_tag=" + oldRaritanTag);
	
		if( oldRaritanTag == null || ! oldRaritanTag.equals(newRaritanTag)){
			String SQL_QUERY2 = "update dct_items set raritan_tag = :raritanVal where item_id=4688";

			session.createSQLQuery("update dct_items set raritan_tag = :raritanVal where item_id=4688").
				setString("raritanVal", newRaritanTag).executeUpdate();
			session.flush();
		}
		resp = session.createSQLQuery(SQL_QUERY1).list();
		String tmpStr = (String)resp.get(0);
		System.out.println("after update; raritan_tag=" + tmpStr);
		return oldRaritanTag;
	}
	
	private void clearRaritanTag(String oldRaritanTag)
	{
		SessionFactory sessionFactory = uniqueValidator.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		 
		session.createSQLQuery("update dct_items set raritan_tag = :raritanVal where item_id=4688").
		setString("raritanVal", oldRaritanTag).executeUpdate();
		session.flush();
		
		String SQL_QUERY1 = "select raritan_tag from dct_items where item_id = 4688";
		List resp = session.createSQLQuery(SQL_QUERY1).list();
		String newRaritanTag = (String)resp.get(0);
		System.out.println("before update; raritan_tag=" + newRaritanTag);		
	}

	private String addAssetNumber(String newAssetNumber)
	{
		SessionFactory sessionFactory = uniqueValidator.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		String SQL_QUERY1 = "select asset_number from dct_item_details where item_detail_id = 4";
		List resp = session.createSQLQuery(SQL_QUERY1).list();
		String oldAssetNumber = (String)resp.get(0);
		System.out.println("before update; asset number=" + oldAssetNumber);
	
		if( oldAssetNumber == null || ! oldAssetNumber.equals(newAssetNumber)){
			String SQL_QUERY2 = "update dct_item_details set asset_number = :assetNumberVal where item_detail_id=4";

			session.createSQLQuery(SQL_QUERY2).setString("assetNumberVal", newAssetNumber).executeUpdate();
			session.flush();
		}
		resp = session.createSQLQuery(SQL_QUERY1).list();
		String tmpStr = (String)resp.get(0);
		System.out.println("after update; asset_number=" + tmpStr);
		return oldAssetNumber;
	}
	
	private void clearAssetNumber(String oldAssetNumber)
	{
		SessionFactory sessionFactory = uniqueValidator.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		 
		session.createSQLQuery("update dct_item_details set asset_number = :assetNumberVal where item_detail_id=4").
		setString("assetNumberVal", oldAssetNumber).executeUpdate();
		session.flush();
		
		String SQL_QUERY1 = "select asset_number from dct_item_details where item_detail_id = 4";
		List resp = session.createSQLQuery(SQL_QUERY1).list();
		String newAssetNumber = (String)resp.get(0);
		System.out.println("before update; asset_number=" + newAssetNumber);		
	}
	
	
	@Test
	public void testUniqueNameTrue2()
	{
		//name should be considered as unique because of site restriction
		//in demo DB it exists on site A
		System.out.println("##### testUniqueName()...");
		String uiId = "tiName";
		String value = "1C";
		String siteCode = "SITE B";
		System.out.println("Testing if itemName: " + value + " exists (should NOT)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", true, retval);
	}
	
	
	@Test
	public void testUniqueNameFalse()
	{
		//name should not be considered as unique because of site restriction
		//in demo DB it exists on site A
		System.out.println("##### testUniqueName()...");
		String uiId = "tiName";
		String value = "1C";
		String siteCode = "SITE A";
		System.out.println("Testing if itemName: " + value + " exists (should)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", false, retval);
	}
	@Test
	public void testUniqueNameTrue()
	{
		System.out.println("##### testUniqueName()...");
		String uiId = "tiName";
		String value = "1CaoBozana";
		String siteCode = "SITE B";
		System.out.println("Testing if itemName: " + value + " exists (should NOT)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", true, retval);
	}

	
	@Test
	public void testUniqueSerialNumberFalse() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueSerialNumber()...");
		
		String uiId = "tiSerialNumber";
		String value = "953421-008";
		String siteCode = "SITE B";
		System.out.println("Testing if serial number: " + value +" exists (should)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", false, retval);	
	}

	@Test
	public void testUniqueSerialNumberTrue() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("##### testUniqueSerialNumber()...");
		
		String uiId = "tiSerialNumber";
		String value = "953421-HelloKitty-008";
		String siteCode = "SITE B";
		System.out.println("Testing if serial number: " + value + " exists (should NOT)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", true, retval);
	}

	//@Test
	public void testUniqueEmailFalse() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueEmail()...");
		
		String uiId = "tiEmail";
		String value = "admin@localhost";
		String siteCode = "SITE B";
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", false, retval);
	}


	
	@Test
	public void testUniqueAssetTagFalse() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueAssetTag()...");
		String assetTagVal="HelloWorld";
		String oldRaritanTag = addRaritanTag(assetTagVal);
		String uiId = "tieAssetTag";
		String value = assetTagVal;
		String siteCode = "SITE B";
		System.out.println("Testing if asset tag: " + value + " exists (should)");
		Boolean retval = false;
		try {
			retval = itemHome.isUnique(uiId, value, siteCode, -1L, null, null);
			System.out.println("retval = " + retval.toString());
		}catch( DataAccessException e){
			System.out.println("Data access exception");
			e.printStackTrace();
			fail();
		}catch(ClassNotFoundException e){
			System.out.println("Class not found excpetion");
			e.printStackTrace();
			fail();
		}
		finally{
			clearRaritanTag(oldRaritanTag);
		}
		System.out.println("Done: retVal=" + retval);
		assertEquals("Unexpected result", false, retval);

	}

	@Test
	public void testUniqueAssetTagTrue() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueAssetTag()...");
		String value="HelloWorld";
		String uiId = "tieAssetTag";
		String siteCode = "SITE B";
		System.out.println("Testing if asset tag: " + value + " exists (should NOT)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", true, retval);

	}

	@Test
	public void testUniqueAssetNumberTrue() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueAssetNumber()...");
		String assetTagVal="HowAreYou";
		String uiId = "tiAssetTag";
		String value = assetTagVal;
		String siteCode = "SITE B";
		System.out.println("Testing if asset number: " + value + " exists (should NOT)");
		Boolean retval = testCore(uiId, value, siteCode);
		assertEquals("Unexpected result", true, retval);

	}

	@Test
	public void testUniqueAssetNumberFalse() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("#####  testUniqueAssetNumber()...");
		String assetNumber="HowAreYouToday";
		String oldAssetNumber = addAssetNumber(assetNumber);
		String uiId = "tiAssetTag";
		String value = assetNumber;
		String siteCode = "SITE B";
		System.out.println("Testing if asset tag: " + value + " exists (should)");
		Boolean retval = false;
		try {
			retval = itemHome.isUnique(uiId, value, siteCode, -1L, null, null);
			System.out.println("retval = " + retval.toString());
		}catch( DataAccessException e){
			System.out.println("Data access exception");
			e.printStackTrace();
			fail();
		}catch(ClassNotFoundException e){
			System.out.println("Class not found exception");
			e.printStackTrace();
			fail();
		}
		finally{
			clearAssetNumber(oldAssetNumber);
		}
		System.out.println("Done: retVal=" + retval);
		assertEquals("Unexpected result", false, retval);

	}


	//@Test
	public void someRandomTests() throws DataAccessException, ClassNotFoundException
	{
		System.out.println("##### running some rundom tests...");
		queryTest1();
		criteriaTest2();
		criteriaTest3();
		criteriaTest4();
		criteriaTest5();
		criteriaTest6();
		criteriaTest7();
		criteriaTest8();
	}
	
	private void queryTest1(){
		System.out.println("-- test1 - running query on serial number");
		SessionFactory sessionFactory = uniqueValidator.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		
		Iterator i = session.createQuery("from Item i, ItemServiceDetails d where i.itemServiceDetails = d.itemServiceDetailId and d.serialNumber = '953421-008'")
				.list().iterator();
		System.out.println("After query");
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			Item item = (Item)pair[0];
			ItemServiceDetails d = (ItemServiceDetails)pair[1];
			System.out.println("id=" + item.getItemId() +", name=" +item.getItemName() + ", serial_number=" + d.getSerialNumber());
			
		}
	}
	
	private void criteriaTest2()
	{
		System.out.println("-- test2 show me all item names, but using criteria and projection:");
		Criteria criteria = session.createCriteria(Item.class);
		criteria.setProjection(Projections.property("itemName"));		
        List<String> list = criteria.list();
      
		for( int id=0; id < list.size(); id++){
			System.out.println("itemName=" + list.get(id));
		}
	}
	
	private void criteriaTest3()
	{			
		System.out.println("-- test3 show me item id and item name using criteria and projection:");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query");
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			System.out.println("id=" + itemId + ", itemName=" + itemName);
			
		}
	}
	private void criteriaTest4()
	{
		System.out.println("-- test4 show me only item id and item name where item name is S44-REB4");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		criteria.add(Restrictions.eq("this.itemName", "S44-REB4"));
		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query");
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			System.out.println("id=" + itemId + ", itemName=" + itemName);
		}

	}
	
	private void criteriaTest5()
	{
		System.out.println("-- test5 show me only item id, item name and serial number where serial number is 953421-008, use aliases");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		criteria.createAlias("itemServiceDetails", "d");
		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		proList.add(Projections.property("d.serialNumber"));
		criteria.add(Restrictions.eq("d.serialNumber", "953421-008"));
		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query");
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			String serialNumber = (String)pair[2];
			System.out.println("id=" + itemId + ", itemName=" + itemName + ", serialNumber=" + serialNumber);
		}
	}
	
	private void criteriaTest6()
	{
		System.out.println("-- test6 show me item id, item name, serial number and email where serial number is 6J37LGP4Z00H, use aliases");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		criteria.createAlias("itemServiceDetails", "d");
		criteria.createAlias("d.itemAdminUser", "users");
		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		proList.add(Projections.property("d.serialNumber"));
		proList.add(Projections.property("users.email"));
		criteria.add(Restrictions.eq("d.serialNumber", "6J37LGP4Z00H"));
		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query, list.size=" + list.size());
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			String serialNumber = (String)pair[2];
			String email=(String)pair[3];
			System.out.println("id=" + itemId + ", itemName=" + itemName + ", serialNumber=" + serialNumber + ", email=" + email);
		}
	}
	
	private void criteriaTest7()
	{
		System.out.println("-- test7 show me item id, item name, serial number and email where email is samernasoura@raritan.com");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		criteria.createAlias("itemServiceDetails", "d", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("d.itemAdminUser", "users", CriteriaSpecification.LEFT_JOIN);
		//No matter if I use inner join or outer left join I get the same (821) entry
//		criteria.createAlias("itemServiceDetails", "d", CriteriaSpecification.INNER_JOIN);
//		criteria.createAlias("d.itemAdminUser", "users", CriteriaSpecification.INNER_JOIN);

		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		proList.add(Projections.property("d.serialNumber"));
		proList.add(Projections.property("users.email"));
		criteria.add(Restrictions.eq("users.email", "samer.nassoura@raritan.com"));
		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query, list.size=" + list.size());
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			String serialNumber = (String)pair[2];
			String email=(String)pair[3];
			System.out.println("id=" + itemId + ", itemName=" + itemName + ", serialNumber=" + serialNumber + ", email=" + email);
		}
	}
	
	private void criteriaTest8()
	{
		System.out.println("-- test8 show me item id, item name, serial number and email where email is samernasoura@raritan.com");
		Criteria criteria = session.createCriteria(Item.class);
		ProjectionList proList = Projections.projectionList();
		criteria.createAlias("itemServiceDetails", "d", CriteriaSpecification.FULL_JOIN);
		criteria.createAlias("d.itemAdminUser", "users", CriteriaSpecification.FULL_JOIN);
		//No matter if I use inner join or outer left join I get the same (821) entry
//		criteria.createAlias("itemServiceDetails", "d", CriteriaSpecification.INNER_JOIN);
//		criteria.createAlias("d.itemAdminUser", "users", CriteriaSpecification.INNER_JOIN);

		proList.add(Projections.property("itemName"));
		proList.add(Projections.property("itemId"));
		proList.add(Projections.property("d.serialNumber"));
		proList.add(Projections.property("users.email"));
//		criteria.add(Restrictions.eq("users.email", "samer.nassoura@raritan.com"));
		criteria.add(Restrictions.eq("users.email", "admin@localhost"));

		criteria.setProjection(proList);		
        List<String> list = criteria.list();
		Iterator i = list.iterator();
		System.out.println("After query, list.size=" + list.size());
		while( i.hasNext()){
			Object[] pair = (Object[]) i.next();
			String itemName = (String)pair[0];
			Long itemId = (Long)pair[1];
			String serialNumber = (String)pair[2];
			String email=(String)pair[3];
			System.out.println("id=" + itemId + ", itemName=" + itemName + ", serialNumber=" + serialNumber + ", email=" + email);
		}
	}
}
