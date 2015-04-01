package com.raritan.tdz.vbjavabridge.home;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.tests.TestBase;

public class ModelSubcriberTest extends TestBase {

		
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		//Make sure that you dont have any events pending
		//that way when you re-run this will not be picked up
		//by the lnevent loop to process them.		
		super.tearDown();
	}

	private void deleteLnEvents() {
		if (sf != null){
			Session session = sf.getCurrentSession();
			
			Query query = session.createQuery("delete from LNEvent");
			
			query.executeUpdate();
		}
		
	}
	
	private void insertLnEvents() {
		if (sf != null){
			Session session = sf.getCurrentSession();
			
			String queryString="insert into dct_lnevents(db_operation_lks_id, table_name, table_row_id, custom_field1) values (1059, 'dct_models',764,'764')";
			SQLQuery query = session.createSQLQuery(queryString);
			query.executeUpdate();
		}
		
	}
	
	@Test
	public void simulateEvents() throws InterruptedException {
		if (sf != null){
			Session session = sf.getCurrentSession();
			
			insertLnEvents();
			
			Thread.sleep(1 * 60000);
		}
	}

}
