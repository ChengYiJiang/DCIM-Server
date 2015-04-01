package com.raritan.tdz.port.dao;

import java.util.List;

import com.raritan.tdz.domain.SensorPort;

public interface SensorPortDAO extends PortDAO<SensorPort> {

	/**
	 * 
	 * @return Returns list of all sensor ports
	 */
	public List<Long> getAllAssetStripSensorCabinets(String siteCode, List<Long> excludeSensorId);

}
