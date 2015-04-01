/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class FloorPDUItemDeleteBehavior implements ItemDeleteBehavior {
	//TODO: This must be removed and we should be using the DAO.
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	private Logger log = Logger.getLogger(ItemDeleteBehavior.class);


	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		long itemId = item.getItemId();
		
		itemDAO.initPowerPortsAndConnectionsProxy(item);
		
		Map<Long, Long> usedFlagUpdateMap = new HashMap<Long, Long>();
		
		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null != powerPorts) {
			for (PowerPort port: powerPorts) {
				Set<PowerConnection> conns = port.getSourcePowerConnections();
				if (null != conns) {
					// collect all the dest port that needs used flags update
					for (PowerConnection conn: conns) {
						usedFlagUpdateMap.put(conn.getDestPort().getPortId(), conn.getSourcePort().getPortId());
					}
					port.getSourcePowerConnections().clear();
				}
				if (null != port.getDestPowerConnections()) {
					port.getDestPowerConnections().clear();
				}
			}
			powerPorts.clear();
		}
		
		// update the used flag of the current destination port
		for (Map.Entry<Long, Long> entry : usedFlagUpdateMap.entrySet()) {
		    Long destId = entry.getKey();
		    Long srcId = entry.getValue();
		    powerPortDAO.changeUsedFlag(destId, srcId);
		}
		
		Session session = this.sessionFactory.getCurrentSession();
		
		//Update items that have this cabinet as the parent
		Query q = session.createSQLQuery("update dct_items set parent_item_id = null where parent_item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " power panels");
		}

		//Update items that have this cabinet as the parent
		q = session.createSQLQuery("update dct_items_me set ups_bank_item_id = null where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " power panels");
		}

		session.flush();
	}

	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postDelete() throws BusinessValidationException {
		// TODO Auto-generated method stub
		
	}
	
}
