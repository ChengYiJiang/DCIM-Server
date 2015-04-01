package com.raritan.tdz.location.rulesengine;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class LocationTypeMethodCallback implements RemoteRefMethodCallback {
	private LocationDAO locationDAO;
	final String UUID_LOCATION_TYPE = "cmbLocationType";
	
	public LocationDAO getLocationDAO() {
		return locationDAO;
	}

	public void setLocationDAO(LocationDAO locationDAO) {
		this.locationDAO = locationDAO;
	}

	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {

		Long locationId = (Long) filterValue;
		DataCenterLocationDetails location = locationDAO.getLocation(locationId);
		if( uiViewComponent.getUiId().equals(UUID_LOCATION_TYPE)){
			if( location.getDcTypeLookup() != null){
				uiViewComponent.getUiValueIdField().setValueId(location.getDcTypeLookup().getLkuId());
				uiViewComponent.getUiValueIdField().setValue(location.getDcTypeLookup().getLkuValue());
			}
		}
		
	}

}
