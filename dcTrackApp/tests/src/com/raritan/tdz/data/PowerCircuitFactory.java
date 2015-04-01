package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

public interface PowerCircuitFactory {

	public abstract PowerCircuit getCircuit(Long circuitId);

	public abstract PowerCircuit createCircuit(
			List<PowerConnection> circuitConnections);

	public abstract List<Long> getCircuitItemIds(PowerCircuit circuit);

	public abstract List<Item> getCircuitItems(PowerCircuit circuit);

	public abstract List<Long> getCircuitPortIds(PowerCircuit circuit);

	public abstract void deleteCircuit(PowerCircuit circuit, boolean doBreaker);

	public abstract PowerCircuit mergeCircuits(PowerCircuit startCircuit,
			PowerCircuit endCircuit);

	public abstract PowerCircuit addPortToStartOfCircuit(PowerPort startPort,
			PowerCircuit endCircuit);

	public abstract PowerCircuit createRPDUToFloorOutlet() throws Throwable;

	public abstract void printCircuit(PowerCircuit circuit);

}