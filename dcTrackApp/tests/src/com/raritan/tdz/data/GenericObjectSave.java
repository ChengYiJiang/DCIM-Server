package com.raritan.tdz.data;

import com.raritan.tdz.domain.DataCenterLocationDetails;

public interface GenericObjectSave {
	public abstract Long save(Object dbObject);
	public abstract DataCenterLocationDetails getTestLocation();
	public abstract String getNextRequestNo();
	public abstract void update(Object dbObject);
}
