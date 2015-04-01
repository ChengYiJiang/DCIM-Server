package com.raritan.tdz.item.home.itemObject;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ValueIdDTOHolder;

public class ItemCabinetChangeSaveBehavior extends AbstractCabinetChangeSaveBehavior {

	@Override
	protected void clearFields(Item item) {
		Object cmbUPosition = ValueIdDTOHolder.getCurrent().getValue("cmbUPosition");
		Object cmbOrientation = ValueIdDTOHolder.getCurrent().getValue("cmbOrientation");
		Object radioRailsUsed = ValueIdDTOHolder.getCurrent().getValue("radioRailsUsed");
		if (cmbUPosition == null) item.setUPosition(SystemLookup.SpecialUPositions.NOPOS);
		if (cmbOrientation == null)	item.setFacingLookup(null);
		if (radioRailsUsed == null) item.setMountedRailLookup(null);
	}
}
