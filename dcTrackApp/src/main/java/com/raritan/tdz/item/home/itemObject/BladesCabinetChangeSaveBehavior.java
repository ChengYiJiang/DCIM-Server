package com.raritan.tdz.item.home.itemObject;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.util.ValueIdDTOHolder;

public class BladesCabinetChangeSaveBehavior extends AbstractCabinetChangeSaveBehavior {

	private void clearBladePlacementFields(ItItem itItem) {
		Object cmbChassis = ValueIdDTOHolder.getCurrent().getValue("cmbChassis");
		Object cmbSlotPosition = ValueIdDTOHolder.getCurrent().getValue("cmbSlotPosition");
		if (cmbChassis == null) itItem.setBladeChassis(null);
		if (cmbSlotPosition == null) itItem.setSlotPosition(-9);
	}

	@Override
	protected void clearFields(Item item) {
		ItItem itItem = (ItItem) item;
		clearBladePlacementFields (itItem);
	}

}
