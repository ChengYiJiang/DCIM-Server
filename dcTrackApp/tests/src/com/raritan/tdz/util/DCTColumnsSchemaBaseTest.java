/**
 * 
 */
package com.raritan.tdz.util;

import static org.junit.Assert.*;
import org.hibernate.criterion.ProjectionList;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.junit.After;
import org.junit.Before;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.tests.TestBase;
import org.hibernate.SQLQuery;

/**
 * @author bozana
 *
 */
public class DCTColumnsSchemaBaseTest extends TestBase {

	private DCTColumnsSchema tblSchema;
	private SessionFactory sessionFactory;
	private RulesProcessor rulesProcessor;
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		tblSchema = (DCTColumnsSchema) ctx.getBean("columnsSchema");
		sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
		rulesProcessor = (RulesProcessor) ctx.getBean("itemRulesProcessor");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public void test1()
	{
		System.out.println("test1!");
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createSQLQuery("select table_name, column_name, data_type, character_maximum_length, numeric_precision, numeric_precision_radix, numeric_scale from INFORMATION_SCHEMA.COLUMNS where table_name like 'dct_%'");
        
        List list = new ArrayList();
        list.addAll(q.list());
    	Iterator i = list.iterator();
        System.out.println("After query");
        while( i.hasNext()){
        	Object[] pair = (Object[]) i.next();
        	String tableName = (String)pair[0];
      		String column_name = (String)pair[1];
      		String data_type = (String)pair[2];
        	Integer char_max_len = (Integer)pair[3];
        	Integer num_precision = (Integer)pair[4];
        	Integer num_radix = (Integer)pair[5];
        	Integer num_scale = (Integer)pair[6];
   			System.out.println("table_name=" + tableName +", column_name=" + column_name + 
   					", data_type=" + data_type + ", char_max_len=" + char_max_len + 
   					", num_precision=" + num_precision + ", num_radix=" + num_radix +
   					", num_scale=" + num_scale);
        }
	}	

	@Test
	public void test2()
	{
		System.out.println("=== test5");
        
        Map x = sessionFactory.getAllClassMetadata();
        Map<String, String> myMap = new HashMap<String,String>();
    
        Iterator it = x.entrySet().iterator();
        while (it.hasNext()) {
        	try{
        		Map.Entry pairs = (Map.Entry)it.next();
        		ClassMetadata classMetadata = (ClassMetadata)pairs.getValue();
        		System.out.println("key: " + pairs.getKey());
        		
        		AbstractEntityPersister y = (AbstractEntityPersister)classMetadata;
        		System.out.println(y.getName() + "------>" + y.getTableName());
        		for (int j = 0; j < y.getPropertyNames().length; j++) {
        			if(y.getPropertyColumnNames(j).length > 0){
            			StringBuffer myKey = new StringBuffer();
            			StringBuffer myVal = new StringBuffer();
            			//hibernate naming
            			myVal.append(pairs.getKey());
            			myVal.append(" ");
            			myVal.append(y.getPropertyNames()[j]);
            			//sql naming contains quotes, have to take them out
            			if(y.getTableName().startsWith("\"") || y.getTableName().endsWith("\"")){
            				myKey.append(y.getTableName().replaceAll("\"", ""));   				
            			}else{
            				myKey.append(y.getTableName());
            			}
            			myKey.append(" ");
            			if(y.getPropertyColumnNames(j)[0].startsWith("\"") ||y.getPropertyColumnNames(j)[0].endsWith("\"")){
            				myKey.append(y.getPropertyColumnNames(j)[0].replaceAll("\"", ""));
            			}else{
            				myKey.append(y.getPropertyColumnNames(j)[0]);	
            			}
    
            			myMap.put(myKey.toString(), myVal.toString());
            		/*	myKey.append(y.getTableName().substring(1, y.getTableName().length()));
            			myKey.append(" ");
            			myKey.append(y.getPropertyColumnNames(j)[0].substring(1, y.getPropertyColumnNames(j)[0].length()));
            			myMap.put(myKey.toString(), myVal.toString());*/
        			}
        			if(y.getPropertyColumnNames(j).length > 1){
        				System.out.println("@@@@@@@@@@@@@ lenth=" +y.getPropertyColumnNames(j).length );
        			}
        			
        			System.out.println(" " + y.getPropertyNames()[j] + " -> " +
        					(y.getPropertyColumnNames(j).length > 0 ? y.getPropertyColumnNames(j)[0]: ""));
        		}
        	}catch(HibernateException e){
        		System.out.println("#### got hibernate exception:");
        		e.printStackTrace();
        	}
        }
        System.out.println("#################################################");
        System.out.println("#### myMap:");
        Iterator it2 = myMap.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pairs = (Map.Entry)it2.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it2.remove(); // avoids a ConcurrentModificationException
        }

        
	}
	
	
	@Test
	public void testUIIdLenthMap()
	{
		System.out.println("testUIIdLenthMap");
		tblSchema.createUiIDPropertyLengthMap();
		Map<String, String> my_map = tblSchema.getUiIdLenthMap();
		if( my_map.size() == 0) fail();
		System.out.println("------------------------");
		Iterator it2 = my_map.entrySet().iterator();
		while (it2.hasNext()) {
		Map.Entry pairs = (Map.Entry)it2.next();
		System.out.println("Map content:");
		System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
	}
	
	
	private String getLength(String entity, String property) throws ClassNotFoundException
	{
		System.out.println("getLength");
		tblSchema.createUiIDPropertyLengthMap();
		Map<String, String> my_map = tblSchema.getUiIdLenthMap();
		if( my_map.size() == 0) fail();
		System.out.println("------------------------");
		
		List<String> uiIds = rulesProcessor.getUiId(entity, property,true);
		
		String str = null;
		if (uiIds.size() > 0){
			str = uiIds.get(0);
			System.out.println("##### uiID=" + str);
		}
		
		String length = tblSchema.getPropertyLength(str);
		
		return length;
	}
	
	private void printMethodName()
	{
		System.out.println("==== in " + (new Exception().getStackTrace()[0].getMethodName()));
	}
	
	
/*
 *  Notes: Currently there is no any uiID for smallint so this test will fail
 *  It was used to test generic functionality of DCTColumnsSchemaBase class
 * 	@Test
	public void testSmallInt()
	{
		System.out.println();
		String entity = "com.raritan.tdz.domain.ItItem";
		String property = "osiLayer";
		String actualLength = getLength(entity, property);
		String expectedLength = "16.2.0";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("smallint is fine");	
	}*/
	
/*
 * Commenting this test out since currently there is no uiID for any bigint
	@Test
	public void testBigInt()
	{
		String entity = "com.raritan.tdz.domain.Item";
		String property = "piqId";
		String actualLength = getLength(entity, property);
		String expectedLength = "64.2.0";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("bigint is fine");	
	}
*/	
	
	@Test
	public void testInteger() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.ModelDetails";
		String property = "ruHeight";
		String actualLength = getLength(entity, property);
		String expectedLength = "32.2.0";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("integer is fine");	
	}
	
	@Test
	public void testNumeric() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.ModelDetails";
		String property = "weight";
		String actualLength = getLength(entity, property);
		String expectedLength = "18.10.0";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("numeric is fine");	
	}
	
	@Test
	public void testVarChar() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.ItemServiceDetails";
		String property = "serialNumber";
		String actualLength = getLength(entity, property);
		String expectedLength = "100";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("numeric is fine");	
	}
	
	@Test
	public void testVarCharItemName() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.Item";
		String property = "itemName";
		String actualLength = getLength(entity, property);
		String expectedLength = "64";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("numeric is fine");	
	}
	
	@Test
	public void testNumericNumPorts() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.Item";
		String property = "numPorts";
		String actualLength = getLength(entity, property);
		String expectedLength = "32.2.0";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("numeric is fine");	
	}
	
	@Test
	public void testNumericWithScale() throws ClassNotFoundException
	{
		String entity = "com.raritan.tdz.domain.ItemServiceDetails";
		String property = "purchasePrice";
		String actualLength = getLength(entity, property);
		String expectedLength = "10.10.2";
		System.out.println("expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if (actualLength == null) fail();
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("numeric is fine");	
	}
	
	@Test
	public void testValidationDouble5dot2(){
		Double d = new Double("12345.67");
		tblSchema.createUiIDPropertyLengthMap();
		Map<String, String> errorMap = new HashMap<String, String>();
		Errors errors = new MapBindingResult(errorMap, "com.raritan.tdz.domain.Item");
		tblSchema.validate("tiLoadCapacity", d, errors);
		assertTrue(errors.getErrorCount() == 0);
	}
	
	@Test
	public void testValidationDouble8dot0(){
		Double d = new Double("12345678.0");
		tblSchema.createUiIDPropertyLengthMap();
		Map<String, String> errorMap = new HashMap<String, String>();
		Errors errors = new MapBindingResult(errorMap, "com.raritan.tdz.domain.Item");
		tblSchema.validate("tiLoadCapacity", d, errors);
		assertTrue(errors.getErrorCount() == 0);
	}
	
	@Test
	public void testValidationDouble8dot5(){
		Double d = new Double("12345678.12345");
		tblSchema.createUiIDPropertyLengthMap();
		Map<String, String> errorMap = new HashMap<String, String>();
		Errors errors = new MapBindingResult(errorMap, "com.raritan.tdz.domain.Item");
		tblSchema.validate("tiLoadCapacity", d, errors);
		assertTrue(errors.getErrorCount() == 1);
	}

/*	
 *  Notes: Currently there is no any uiID for text so this test will fail
 *  It was used to test generic functionality of DCTColumnsSchemaBase class
	@Test
	public void testText()
	{
		//printMethodName();
		String entity = "com.raritan.tdz.domain.Tickets";
		String property = "description";
		String actualLength = getLength(entity, property);
		String expectedLength = " ";
		System.out.println("testText: expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("text is fine");	
	}*/
	
    /**
	//FIXME: This test case does not work because dct_tickets maps to
	 * com.raritan.tdz.Tickes that contains also private inner class
	 * properties of that inner class are not visible...
	 
	@Test
	public void testText2()
	{
		//printMethodName();
		String entity = "com.raritan.tdz.domain.Tickets";
		String property = "ticketStatus";
		String actualLength = getLength(entity, property);
		String expectedLength = " ";
		System.out.println("testText: expectedLength=" +  expectedLength + ", actualLength=" + actualLength);
		if(!actualLength.equals(expectedLength)) fail();
		System.out.println("text is fine");	
	}
	*/
}
