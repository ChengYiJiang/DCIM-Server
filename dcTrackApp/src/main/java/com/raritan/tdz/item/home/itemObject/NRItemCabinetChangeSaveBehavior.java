package com.raritan.tdz.item.home.itemObject;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.util.ValueIdDTOHolder;

public class NRItemCabinetChangeSaveBehavior extends AbstractCabinetChangeSaveBehavior{

	@Override
	protected void clearFields(Item item) {
		Object cmbOrder = ValueIdDTOHolder.getCurrent().getValue("cmbOrder");
		if (cmbOrder == null) item.setShelfPosition(-9);
	}
}
