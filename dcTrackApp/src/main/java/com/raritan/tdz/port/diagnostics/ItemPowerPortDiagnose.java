package com.raritan.tdz.port.diagnostics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.diagnostics.Diagnostics;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.events.dao.EventDAO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.validators.PowerPortValidator;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class ItemPowerPortDiagnose implements Diagnostics {

	@Autowired(required=true)
	PowerPortValidator powerPortValidator; 
	
	@Autowired(required=true)
	ItemDAO itemDAO;

	@Autowired(required=true)
	PowerPortDAO powerPortDAO;

	@Autowired(required=true)
	PowerPortDiagnosticsDAO powerPortDiagnosticsDAO;
	
	@Autowired(required=true)
	private EventDAO eventDAO;
	
	@Override
	public void diagnose(Errors errors) {

		Map<Long, Long> floorPduWithUpsBank = itemDAO.getAllFloorPDUWithUPSBank();
		List<Long> upsBankIds = new ArrayList<Long>();
		Errors allErrors = getItemErrorObject();
		
		// Validate the power ports of the floor pdu 
		for (Map.Entry<Long, Long> entry: floorPduWithUpsBank.entrySet()) {
			Long fpduId = entry.getKey();
			Long upsBankId = entry.getValue();
			if (!upsBankIds.contains(upsBankId)) {
				upsBankIds.add(upsBankId);
			}
			validateItem(fpduId, allErrors);
		}
		
		// Validate the power ports of the ups bank
		for (Long upsBankId: upsBankIds) {
			validateItem(upsBankId, allErrors);
		}
		
		// Do not validate the power panels
		/*List<Long> powerPanels = itemDAO.getAllPowerPanelItemIds();
		for (Long powerPanelItemId: powerPanels) {
			validateItem(powerPanelItemId);
		}*/
		
		eventDAO.generatePowerChainDiagnosticsEvent("Item Power Port Diagnostics", Long.valueOf(allErrors.getErrorCount()), 0L, 0L);
		
	}

	private void validateItem(long itemId, Errors allErrors) {
		
		Item item = itemDAO.loadItem(itemId);
		Errors errors = getItemErrorObject();
		
		if (null == item) return;
		Map<String, Object> targetMap = getValidatorTargetMap(item, null);

		powerPortValidator.validate(targetMap, errors);
		
		powerPortDiagnosticsDAO.reportError(item, errors, "ERROR");
		
		allErrors.addAllErrors(errors);
		
	}

	private Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(item.getClass().getName(), item);
		targetMap.put(UserInfo.class.getName(), userInfo);
		return targetMap;
	}

	private MapBindingResult getItemErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, MeItem.class.getName() );
		return errors;
		
	}

}
