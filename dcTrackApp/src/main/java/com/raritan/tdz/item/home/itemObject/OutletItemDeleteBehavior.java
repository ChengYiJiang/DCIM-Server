/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 *
 */
public class OutletItemDeleteBehavior implements ItemDeleteBehavior {

	@Autowired
	private PowerCircuitHome powerCircuitHome;
	
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
				
		for(PowerCircuit rec:powerCircuitHome.viewPowerCircuitByCriteria(cCriteria)){
			cirList.add(rec.getPowerCircuitId());
		}
		
		if(cirList.size() > 0){   //Power Outlet don't have data circuits
			powerCircuitHome.deletePowerCircuitByIds(cirList, false);
		}

		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal breaker to outlet connections
		powerCircuitHome.deleteItemBuswayConnections(item.getItemId()); //delete internal breaker to outlet connections

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

}
