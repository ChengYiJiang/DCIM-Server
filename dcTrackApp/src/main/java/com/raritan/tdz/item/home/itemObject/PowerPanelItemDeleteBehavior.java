/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * @author prasanna
 *
 */
public class PowerPanelItemDeleteBehavior implements ItemDeleteBehavior {

	@Autowired
	private PowerCircuitHome powerCircuitHome;

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private PowerPortDAO powerPortDAO;

	
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

		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null != powerPorts) {
			for (PowerPort port: powerPorts) {
				if (null != port.getSourcePowerConnections()) {
					port.getSourcePowerConnections().clear();
				}
				if (null != port.getDestPowerConnections()) {
					port.getDestPowerConnections().clear();
				}
				powerPortDAO.clearBrkrPortReference(port.getPortId());
			}
			powerPorts.clear();
		}
		
		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal breaker to outlet connections
		powerCircuitHome.deleteItemBuswayConnections(item.getItemId()); //delete internal breaker to outlet connections

		// delete all power outlets association with the panels
		List<Long> panelItemIds = new ArrayList<Long>();
		panelItemIds.add(item.getItemId());
		List<Long> powerOutlets = itemDAO.getPowerPanelConnectedPowerOutlet(panelItemIds);
		itemDAO.clearPowerOutletAssociationWithPanel(powerOutlets);
		
		
		
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
