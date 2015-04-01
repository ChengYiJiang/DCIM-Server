package com.raritan.tdz.vbjavabridge.home;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.tests.TestBase;

public class ChassisLayoutSubcriberTest extends TestBase {

		
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
	
	@Test
	public void placeBladeOnChassis() throws RemoteDataAccessException, InterruptedException {
		if (sf != null){
			Session session = sf.getCurrentSession();
			ItItem blade = getBladeItem(session);
			
			Item chassis = blade.getBladeChassis();
			chassis = new Item();
			chassis.setItemId(4981L);
			
			//blade.setBladeChassis(null); //4981
			//session.update(blade);
			System.out.println("Blade Name: " + blade.getItemName());
			
			blade.setBladeChassis(chassis);
			session.update(blade);
			
			Thread.sleep(1 * 60000);
		}
	}
	

	private ItItem getBladeItem(Session session) {
		ItItem item = null;
		//TODO: This is now hard coded to a specific rack pdu and must have an associated IPAddress. This rackpdu is found
		//      in dcTrack demo database.
		//Criteria criteria = session.createCriteria(ItItem.class).add(Restrictions.ge("bladeChassis.itemId", 0L));
		Criteria criteria = session.createCriteria(ItItem.class).add(Restrictions.eq("itemId", 4984L));
		
		List list = criteria.list();
		item = (ItItem)list.get(0);
		
		return item;
	}

}
