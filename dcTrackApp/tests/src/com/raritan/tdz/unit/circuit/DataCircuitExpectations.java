package com.raritan.tdz.unit.circuit;

import java.util.HashMap;
import java.util.List;

import org.jmock.Mockery;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.dto.PortInterface;

public interface DataCircuitExpectations {
	public abstract void addExpectations(DataCircuit circuit);
	
	public abstract void createRead(Mockery jmockContext, Long circuitId,
			DataCircuit retValue);

	public abstract void createLoadCircuit(Mockery jmockContext,
			Long circuitId, DataCircuit retValue);

	public abstract void createViewDataCircuitByConnId(Mockery jmockContext,
			Long connectionId, List<DataCircuit> retValue);

	public abstract void createViewDataCircuitByStartPortId(
			Mockery jmockContext, Long portId, List<DataCircuit> retValue);

	public abstract void createViewDataCircuitByCriteria(Mockery jmockContext,
			CircuitCriteriaDTO cCriteria, List<DataCircuit> retValue);

	public abstract void createGetDestinationItemsForItem(Mockery jmockContext,
			Long itemId, HashMap<Long, PortInterface> retValue);

	public abstract void createGetProposedCircuitIdsForItem(
			Mockery jmockContext, Long itemId,
			HashMap<Long, PortInterface> retValue);

}