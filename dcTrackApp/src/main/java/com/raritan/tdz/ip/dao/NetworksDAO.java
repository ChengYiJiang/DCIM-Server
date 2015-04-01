package com.raritan.tdz.ip.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.domain.Networks;

public interface NetworksDAO extends Dao<Networks> {
	public List<Networks> getNetworkForIpAndLocation(String ipAddress, Long locationId) throws BusinessValidationException;
	public List<Networks> getAllNetworksForLocation(Long locationId) throws BusinessValidationException;
}
