package com.raritan.tdz.lookup.dao;

import java.util.List;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.LkuData;

public interface ConnectorLookupFinderDAO {

	/**
	 * Find the connector lookup by id
	 * @param id
	 * @return List<ConnectorLkuData>
	 */
	public List<ConnectorLkuData> findById(Long id);

	/**
	 * Find the connector lookup using connectorName
	 * @param name
	 * @return
	 */
	public List<ConnectorLkuData> findByName(String name);

	/**
	 * Case inseinsitive serach for connector lookup using connectorName
	 * @param name
	 * @return
	 */
	public List<ConnectorLkuData> findByNameCaseInsensitive(String name);

}
