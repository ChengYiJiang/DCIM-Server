package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Santo Rosario
 *
 */
public class FloorPDUItemObject extends MeItemObject {
	protected ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	public FloorPDUItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.FLOOR_PDU);
		return Collections.unmodifiableSet( codes );
	}	
	
	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		
		Session session = this.sessionFactory.getCurrentSession();

		//Update items that have this cabinet as the parent
		Query q = session.createSQLQuery("update dct_items set parent_item_id = null where parent_item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " power panels");
		}
		
		return super.deleteItem();
	}
	
}
