
package com.raritan.tdz.domain;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.audit.domain.AuditTrail;
import com.raritan.tdz.tests.TestBase;
import java.util.List;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */
public class AuditTrailTest extends TestBase {
	//private static final String EVENT_SOURCE = "Unit Test";
	
        
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
	
	@Test
	public void testGetAllAuditTrail() throws Throwable {
		System.out.println("######### UnitTest: testGetAllAuditTrail() ##########");
		
		List<AuditTrail> recList = null;
		
		try {
			session = sf.getCurrentSession();
			Criteria c = session.createCriteria(AuditTrail.class);
			c.createAlias("classLookup", "classLks");
			c.createAlias("item", "item");
			c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			recList = c.list();
			long itemId = -1;
			
			for(AuditTrail r:recList){
				Item item = r.getItem();
				
				if(item != null){
					System.out.print("Item Name: " + item.getItemName() + "\t");
					itemId = item.getItemId();
				}				
				System.out.println(r.getDisplayName());
			}
			
			testGetAuditTrail(itemId);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}		 		
	} 
	
	public void testGetAuditTrail(long itemId) throws Throwable {
		System.out.println("######### UnitTest: testGetAuditTrail() ##########");
		
		List<AuditTrail> recList = null;
		
		try {
			session = sf.getCurrentSession();
			Criteria c = session.createCriteria(AuditTrail.class);
			c.createAlias("classLookup", "classLks");
			c.createAlias("item", "item");
			c.add(Restrictions.eq("item.itemId", itemId));
			c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			recList = c.list();
			
			for(AuditTrail r:recList){
				Item item = r.getItem();
				
				if(item != null){
					System.out.print("Item Name: " + item.getItemName() + "\t");
				}				
				System.out.println(r.getDisplayName());
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
		}		 		
	} 
	
}
