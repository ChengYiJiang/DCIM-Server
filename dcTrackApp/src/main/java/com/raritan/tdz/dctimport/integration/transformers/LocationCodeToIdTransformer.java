package com.raritan.tdz.dctimport.integration.transformers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.location.dao.LocationDAO;

public class LocationCodeToIdTransformer extends NameToIdTransformerBase {

	@Autowired
	LocationDAO locationDAO;
	
	public LocationCodeToIdTransformer(ImportErrorHandler importErrorHandlerGateway) {
		this. importErrorHandlerGateway = importErrorHandlerGateway;
	}
	
	@Override
	protected Long getId(Map<String, Object> m) {
		String locationCode = (String)m.get("tiLocationCode");
		Long id = locationDAO.getLocationIdByCode(locationCode.toUpperCase());
		if (id == null) errorCode = "Import.locationNotFound";
		return id;
	}

	@Override
	protected Map<String, Object> updateName(Map<String, Object> m) {
		
		String newName = (String)m.get("newLocationCode");
		if (newName != null && newName.length() > 0) 
			m.put("tiLocationCode",  newName);
		
		m.remove("newLocationCode");

		return m;
	}

}
