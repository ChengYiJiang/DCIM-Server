package com.raritan.tdz.move.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.circuit.home.CircuitDelete;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;

public class CabinetDeletePlannedCircuits implements ItemSaveBehavior {

	@Autowired
	private CircuitDelete circuitDelete;

	/*@Autowired
	private CircuitDAO<PowerCircuit> powerCircuitDAOExt;*/
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;

	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		Long itemId = (Long) additionalArgs[0];
		
		/*List<Long> powerCircuitIds = powerCircuitDAOExt.getCabinetPlannedCircuitIdsNotMoving(itemId);
		
		if (null != powerCircuitIds && powerCircuitIds.size() > 0) { 
			circuitDelete.deletePowerCircuitByIds(powerCircuitIds, false);
		}*/
		
		List<Long> dataCircuitIds = dataCircuitDAOExt.getCabinetPlannedCircuitIdsNotMoving(itemId);
		
		if (null != dataCircuitIds && dataCircuitIds.size() > 0) {
			circuitDelete.deleteDataCircuitByIds(dataCircuitIds, false);
		}
		
	}
	
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
