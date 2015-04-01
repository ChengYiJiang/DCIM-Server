package com.raritan.tdz.item.home;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.UiIdLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

public class ChassisFaceResolver implements ItemDataResolver {

	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;

	@Override
	public void resolve(Map<String, Object> data, Errors errors) throws DataAccessException, BusinessValidationException {
		String face = (String)data.get(UiIdLookup.UiId.RADIO_CHASSISFACE);
		LksData chassisFaceLks = null;

		if (face != null ) {
			List<LksData> lksList = systemLookupFinderDAO.findByLkpTypeNameAndLkpValue("FACE", face);
			
			if (lksList == null || lksList.size() ==0) {
				String code = "ItemValidator.InvalidChassisFace"; 
				Object args[]  = {face};
				errors.rejectValue("chassisFace", code, args, "Invalid Chassis face");
				
				// set the value to null so that it will not fail in next step of resolving the slot position
				data.put(UiIdLookup.UiId.RADIO_CHASSISFACE, null);
				
				return;
			}
			
			chassisFaceLks = lksList.get(0);

			if (chassisFaceLks !=null) {
				data.put(UiIdLookup.UiId.RADIO_CHASSISFACE, chassisFaceLks.getLkpValueCode());
			} else {
				data.remove(UiIdLookup.UiId.RADIO_CHASSISFACE);
			}
		}
	}
}
