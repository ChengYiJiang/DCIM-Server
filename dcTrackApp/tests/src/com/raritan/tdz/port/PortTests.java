package com.raritan.tdz.port;

import org.hibernate.Query;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.tests.TestBase;

public class PortTests extends TestBase {

	public PortTests() {
		// TODO Auto-generated constructor stub
	}
	
	protected ItemDomainAdaptor itemAdaptor;
	protected ModelHome modelHome;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemAdaptor = (ItemDomainAdaptor)ctx.getBean("itemDomainAdaptor");
		modelHome = (ModelHome)  ctx.getBean("modelHome");

		Query deleteQuery = session.createSQLQuery(
			    "DELETE FROM dct_model_ports_power WHERE version = '2.x' and model_id = 1015");
		int updated = deleteQuery.executeUpdate();
		System.out.println("deleted 2.x ports of model_id 1015:" + updated);
		
		deleteQuery = session.createSQLQuery(
			    "DELETE FROM dct_model_ports_data WHERE version = '2.x' and model_id = 1015");
		updated = deleteQuery.executeUpdate();
		System.out.println("deleted 2.x ports of model_id 1015:" + updated);
	}
	
	
	protected void setItemStatus(Long itemId, long status) {
		Session session = sf.getCurrentSession();
		Item item = (Item) session.get(Item.class, itemId);

		item.setStatusLookup( SystemLookup.getLksData(session, status) );
	}

	protected void setItemStatus(Item item, long status) {

		item.setStatusLookup( SystemLookup.getLksData(session, status) );
	}

	

}
