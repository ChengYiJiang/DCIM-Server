package com.raritan.tdz.move.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.home.CircuitDelete;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.lookup.SystemLookup;

public class ItemDeletePlannedCircuits implements ItemSaveBehavior {

	@Autowired
	private CircuitRequest circuitRequest;
	
	@Autowired
	private CircuitDelete circuitDelete;
	

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
		String requestType = (String) additionalArgs[1];
		
		if (null == requestType || null == itemId || itemId <= 0) return;
		
		deletePlannedCircuits(itemId, requestType);


	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void deletePlannedCircuits(Long itemId, String requestType) throws DataAccessException, BusinessValidationException {
		
		Map<Long, List<Long>> plannedCircuits = circuitRequest.getPlannedCircuits(itemId, requestType);
		
		for (Map.Entry<Long, List<Long>> entry: plannedCircuits.entrySet())  {
			
			Long portClass = entry.getKey();
			List<Long> portClassPlannedCircuits = entry.getValue(); 
			
			if (null == portClassPlannedCircuits || portClassPlannedCircuits.size() == 0) continue;
			
			if (portClass.equals(SystemLookup.PortClass.DATA)) circuitDelete.deleteDataCircuitByIds(portClassPlannedCircuits, false);
			
			if (portClass.equals(SystemLookup.PortClass.POWER)) circuitDelete.deletePowerCircuitByIds(portClassPlannedCircuits, false);
			
		}
	}


}
