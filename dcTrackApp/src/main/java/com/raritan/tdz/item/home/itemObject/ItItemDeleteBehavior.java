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

/**
 * @author prasanna
 *
 */
public class ItItemDeleteBehavior implements ItemDeleteBehavior {
	
	//TODO: This must be removed and we should be using the DAO.
	@Autowired
	private SessionFactory sessionFactory;
	
	private Logger log = Logger.getLogger(ItemDeleteBehavior.class);


	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		long itemId = item.getItemId();
		Session session = this.sessionFactory.getCurrentSession();
		
		Query q = session.createSQLQuery("delete from dct_items_it where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " IT Item");
		}
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
		// Nothing to do here

	}

}
