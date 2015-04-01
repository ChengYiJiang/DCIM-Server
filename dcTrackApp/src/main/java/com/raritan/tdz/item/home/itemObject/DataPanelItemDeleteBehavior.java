/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemHome;

/**
 * @author prasanna
 *
 */
public class DataPanelItemDeleteBehavior implements ItemDeleteBehavior {
	
		//TODO: This must be removed and we should be using the DAO.
		@Autowired
		private SessionFactory sessionFactory;
		
		private Logger log = Logger.getLogger(ItemDeleteBehavior.class);
		
		@Autowired
		private ItemHome itemHome;
		
		@Autowired
		private DataCircuitHome dataCircuitHome;
		
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		//Delete Circuits for This Item
		List<Long> cirList = new ArrayList<Long>();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setItemId(item.getItemId());
				
		for(DataCircuit rec:dataCircuitHome.viewDataCircuitByCriteria(cCriteria)){
			cirList.add(rec.getDataCircuitId());
		}
		
		if(cirList.size() > 0){
			dataCircuitHome.deleteDataCircuitByIds(cirList, false);
		}

		dataCircuitHome.deleteItemDataConnections(item.getItemId()); //delete internal panel to panel connections
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
