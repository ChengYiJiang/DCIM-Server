package com.raritan.tdz.item.home.itemObject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.util.ValueIdDTOHolder;

public class CabinetLocationChangeSaveBehavior extends AbstractLocationChangeSaveBehavior {
	
	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired
	ItemDAO itemDAO;

	@Override
	protected void clearFields(Item item) {
		CabinetItem cabinet = (CabinetItem)item;
		clearCabinetPlacementFields(cabinet);
	}

	private void clearCabinetPlacementFields(CabinetItem cabient) {
		Object cmbRowLabel = ValueIdDTOHolder.getCurrent().getValue("cmbRowLabel");
		Object cmbRowPosition = ValueIdDTOHolder.getCurrent().getValue("cmbRowPosition");
		Object radioFrontFaces = ValueIdDTOHolder.getCurrent().getValue("radioFrontFaces");
		
		if (cmbRowLabel == null) cabient.setRowLabel(null);
		if (cmbRowPosition == null)	cabient.setPositionInRow(0);
		if (radioFrontFaces == null) {
			List<LksData> lksList = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.FrontFaces.NORTH);
			if (lksList != null && lksList.size() > 0) {
				cabient.setFacingLookup(lksList.get(0));
			}
		}
	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(CabinetItem.class.getName())) {
				canSupport = true;
			}
		}
		return canSupport;
	}

}
