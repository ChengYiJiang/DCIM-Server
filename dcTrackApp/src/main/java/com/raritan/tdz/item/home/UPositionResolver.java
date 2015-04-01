package com.raritan.tdz.item.home;

import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.UiIdLookup;

public class UPositionResolver implements ItemDataResolver {
	
	public void resolve(Map<String,Object> data, Errors errors) {
		String _uPosition = null;
		Long cmbUPosition = SystemLookup.SpecialUPositions.NOPOS; // when No U-position selected, use default
		Object uPosObj = data.get(UiIdLookup.UiId.CMB_U_POSITION);
		if (uPosObj instanceof Integer) 
			_uPosition = uPosObj.toString();
		else 
			_uPosition = (String)uPosObj;
		
		if (_uPosition != null) { 
			if (_uPosition.toLowerCase().equals("above")) {
				cmbUPosition = SystemLookup.SpecialUPositions.ABOVE;
			}
			else if (_uPosition.toLowerCase().equals("below")) {
				cmbUPosition = SystemLookup.SpecialUPositions.BELOW;
			}
			else if (_uPosition.isEmpty()) {
				cmbUPosition = SystemLookup.SpecialUPositions.NOPOS; // when No U-position selected, use default
			}
			else {
				try {
					cmbUPosition = Long.parseLong(_uPosition);
				} catch (NumberFormatException ne) {
					// We cannot resolve the input data to appropriate uPosition.
					// leaving it to ItemUPositionValidator for further validation
					return;
				}
			}
			data.put(UiIdLookup.UiId.CMB_U_POSITION, cmbUPosition);

		} 
	}
}
