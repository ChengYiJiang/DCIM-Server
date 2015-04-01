package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UiIdLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

public class CabinetSideResolver implements ItemDataResolver {
	
	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;
	
	public void resolve(Map<String, Object> data, Errors errors) {
		String cabinetSide = (String)data.get(UiIdLookup.UiId.RADIO_CABINET_SIDE);
		List<LksData> lksData = null;
		if (cabinetSide != null) { 
			if (cabinetSide.toLowerCase().equals("left")) {
				lksData = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.RailsUsed.LEFT_REAR);
			}
			else if (cabinetSide.toLowerCase().equals("right")) {
				lksData = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.RailsUsed.RIGHT_REAR);
			} 

			if (lksData != null) {
				data.put(UiIdLookup.UiId.RADIO_CABINET_SIDE, lksData.get(0).getLkpValue());
			}
		}
	}
}
