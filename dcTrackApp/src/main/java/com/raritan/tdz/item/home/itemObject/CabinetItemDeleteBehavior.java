/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;

/**
 * @author prasanna
 *
 */
public class CabinetItemDeleteBehavior implements ItemDeleteBehavior {
	
	//TODO: This must be removed and we should be using the DAO.
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ItemDAO itemDAO;
	
	private Logger log = Logger.getLogger(getClass());

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		long itemId = item.getItemId();
		
		Session session = this.sessionFactory.getCurrentSession();

		//Update items that have this cabinet as the parent
		Query q = session.createSQLQuery("update dct_items set parent_item_id = null where parent_item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " child items");
		}
		
		q = session.createSQLQuery("delete from dct_items_cabinet where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " Cabinet Item");
		}
		
		//delete reservations if any
		itemDAO.deleteReservations(itemId);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#preDelete(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		// Nothing to do here

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#postDelete()
	 */
	@Override
	public void postDelete() throws BusinessValidationException {
		// Nothing to do here.

	}

}
