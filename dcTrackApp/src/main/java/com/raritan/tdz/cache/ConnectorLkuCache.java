package com.raritan.tdz.cache;

import com.raritan.tdz.domain.ConnectorLkuData;

public interface ConnectorLkuCache {

	public ConnectorLkuData getConnectorLkuData(Long connectorLkuId);

	public ConnectorLkuData getConnectorLkuData(String connectorName);
	
}
