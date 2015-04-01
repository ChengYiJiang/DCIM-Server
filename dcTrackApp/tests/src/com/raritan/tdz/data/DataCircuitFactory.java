package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.Item;

public interface DataCircuitFactory {

	public abstract DataCircuit getCircuit(Long circuitId);

	public abstract DataCircuit createDeviceToNetworkCircuit(
			Long statusValueCode);

	public abstract DataCircuit createDevice2Panel2Network(Long statusValueCode);

	public abstract DataCircuit createDataPanelToNetwork(Long statusValueCode);

	public abstract DataCircuit createCircuitUsingItems(List<Item> itemList,
			Long statusValueCode);

	public abstract DataCircuit createCircuit(
			List<DataConnection> circuitConnections);

	public abstract List<Long> getCircuitItemIds(DataCircuit circuit);

	public abstract List<Item> getCircuitItems(DataCircuit circuit);

	public abstract void deleteCircuit(DataCircuit circuit);

	public abstract DataCircuit mergeCircuits(DataCircuit startCircuit,
			DataCircuit endCircuit);

	public abstract DataCircuit addItemToStartOfCircuit(Item startItem,
			DataCircuit endCircuit);

	public abstract DataCircuit createDeviceToNetworkCircuit(Long statusValueCode, Item cabinet);
	
	public abstract  DataCircuit createFanoutCircuit(Long statusValueCode);

}