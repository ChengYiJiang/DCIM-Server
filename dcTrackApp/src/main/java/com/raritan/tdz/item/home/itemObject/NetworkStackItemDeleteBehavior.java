/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;

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
public class NetworkStackItemDeleteBehavior implements ItemDeleteBehavior {
	
	//TODO: This must be removed and we should be using the DAO.
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ItemDAO itemDao;
	
	private Logger log = Logger.getLogger(ItemDeleteBehavior.class);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		//Set the sibling_item_id	
		//This function return the sibling stacks link to this item
		//List of order by num_ports, item_id
		/*List<Item> stackList = (List<Item>) itemDao.getNetworkStackItems(item.getItemId());
		
		if(stackList.size() > 0){
			Item sibling = stackList.get(0);
			
			if((item.getItemId() == sibling.getCracNwGrpItem().getItemId())){
				switchSiblingItemId(item.getItemId(), sibling.getItemId());
			}
		}*/
		
		itemDao.removePrimaryStackItem(item.getItemId());

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#preDelete(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#postDelete()
	 */
	@Override
	public void postDelete() throws BusinessValidationException {
		// TODO Auto-generated method stub

	}
	
	//Private methods
	private void switchSiblingItemId(long oldSiblingId, long newSiblingId) throws ClassNotFoundException, BusinessValidationException,	Throwable {
		Session session = this.sessionFactory.getCurrentSession();
				
		Query q = session.createSQLQuery("update dct_items set sibling_item_id = :newSiblingId where sibling_item_id = :oldSiblingId");
		q.setLong("newSiblingId", newSiblingId);
		q.setLong("oldSiblingId", oldSiblingId);
		int ret = q.executeUpdate();
		
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated  " + ret + " Sibling Items");
		}
	}

}
