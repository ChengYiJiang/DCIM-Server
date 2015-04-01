package com.raritan.tdz.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;

public class ConnectorLkuCacheImpl implements ConnectorLkuCache {

	@Autowired(required=true)
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;

	private Map<Long, ConnectorLkuData> connectorLkuData = new HashMap<Long, ConnectorLkuData>();
	
	private Map<String, ConnectorLkuData> connectorNameLkuData = new HashMap<String, ConnectorLkuData>();
	
	@Override
	public ConnectorLkuData getConnectorLkuData(Long connectorLkuId) {
		ConnectorLkuData connLkuData = connectorLkuData.get(connectorLkuId);
		if (null == connLkuData) {
			List<ConnectorLkuData> connectors = connectorLookupFinderDAO.findById(connectorLkuId);
			if (connectors.size() == 0) return null;
			connLkuData = connectors.get(0);
			connectorLkuData.put(connectorLkuId, connLkuData);
			connectorNameLkuData.put(connLkuData.getConnectorName(), connLkuData);
		}
		
		return connLkuData;
	}

	@Override
	public ConnectorLkuData getConnectorLkuData(String connectorName) {
		ConnectorLkuData connLkuData = connectorNameLkuData.get(connectorName);
		if (null == connLkuData) {
			List<ConnectorLkuData> connectors = connectorLookupFinderDAO.findByNameCaseInsensitive(connectorName);
			if (connectors.size() == 0) return null;
			connLkuData = connectors.get(0);
			connectorLkuData.put(connLkuData.getConnectorId(), connLkuData);
			connectorNameLkuData.put(connLkuData.getConnectorName(), connLkuData);
		}
		
		return connLkuData;
	}
	
}
