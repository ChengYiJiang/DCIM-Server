package com.raritan.tdz.piq.json;

import com.raritan.tdz.domain.DataCenterLocationDetails;


public interface SitesJSON {
	
	public String getId();
	
	public String getExternalKey();
	
	public String getSiteId();

	public Boolean isDataCenterInSync(DataCenterLocationDetails location);

}
