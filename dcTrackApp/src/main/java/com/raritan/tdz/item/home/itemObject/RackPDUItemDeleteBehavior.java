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
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 *
 */
public class RackPDUItemDeleteBehavior implements ItemDeleteBehavior {
	
	@Autowired
	private CircuitPDHome circuitPDHome;
	
	@Autowired
	private DataCircuitHome dataCircuitHome;
	
	@Autowired
	private PowerCircuitHome powerCircuitHome;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior#deleteItem(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		//Delete Circuits for This Item
		List<Long> cirDataList = new ArrayList<Long>();
		List<Long> cirPowerList = new ArrayList<Long>();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setItemId(item.getItemId());

		for(CircuitViewData rec:circuitPDHome.viewCircuitPDList(cCriteria)){
			if(rec.isPowerCircuit()){
				cirPowerList.add(rec.getCircuitId());
			}

			if(rec.isDataCircuit()){
				cirDataList.add(rec.getCircuitId());
			}
		}

		if(cirPowerList.size() > 0){
			powerCircuitHome.deletePowerCircuitByIds(cirPowerList, false);
		}

		if(cirDataList.size() > 0){
			dataCircuitHome.deleteDataCircuitByIds(cirDataList, false);
		}
		
		Set<PowerPort> ports = item.getPowerPorts();
		if (null != ports) {
			for (PowerPort port: ports) {
				Set<PowerConnection> connections = port.getSourcePowerConnections();
				if (null != connections) {
					port.getSourcePowerConnections().clear();
				}
			}
			ports.clear();
		}
		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal input to outlet connections


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
