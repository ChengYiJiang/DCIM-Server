package com.raritan.tdz.item.home.itemObject;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ValueIdDTOHolder;

public class ItemLocationChangeSaveBehavior extends AbstractLocationChangeSaveBehavior {

	@Override
	protected void clearFields(Item item) {
		Object cmbCabinet = ValueIdDTOHolder.getCurrent().getValue("cmbCabinet");
		Object cmbUPosition = ValueIdDTOHolder.getCurrent().getValue("cmbUPosition");
		Object cmbOrientation = ValueIdDTOHolder.getCurrent().getValue("cmbOrientation");
		Object radioRailsUsed = ValueIdDTOHolder.getCurrent().getValue("radioRailsUsed");
		
		if (cmbCabinet == null) item.setParentItem(null);
		if (cmbUPosition == null) item.setUPosition(SystemLookup.SpecialUPositions.NOPOS);
		if (cmbOrientation == null)	item.setFacingLookup(null);
		if (radioRailsUsed == null) item.setMountedRailLookup(null);
	}
}
