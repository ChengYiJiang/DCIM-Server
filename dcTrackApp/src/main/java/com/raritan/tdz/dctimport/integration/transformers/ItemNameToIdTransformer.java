package com.raritan.tdz.dctimport.integration.transformers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.item.dao.ItemDAO;

public class ItemNameToIdTransformer extends NameToIdTransformerBase {
	
	@Autowired
	ItemDAO itemDAO;

	public ItemNameToIdTransformer(ImportErrorHandler importErrorHandlerGateway) {
		this. importErrorHandlerGateway = importErrorHandlerGateway;
	}

	@Override
	protected Long getId(Map<String, Object> m) {
		String itemName = (String)m.get("tiName");
		String locationCode = (String)m.get("cmbLocation");
		String cabinetName = (String)m.get("cmbCabinet");
		String uPosition = ((String)m.get("cmbUPosition"));
		String mountedRails = ((String)m.get("radioRailsUsed"));
		//Long id = itemDAO.getItemId(itemName.toUpperCase(), location);
		Long id = itemDAO.getUniqueItemId(locationCode, itemName, cabinetName, uPosition, mountedRails);
		if (id == null) errorCode = "Import.itemNotFound";
		return id;
	}

	@Override
	protected Map<String, Object> updateName(Map<String, Object> m) {
		String newName = (String)m.get("newName");
		if (newName != null && newName.length() > 0) 
			m.put("tiName",  newName);
		
		m.remove("newName");
		return m;		
	}
}
