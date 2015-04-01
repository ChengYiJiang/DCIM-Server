package com.raritan.tdz.unit.circuit;

import java.util.HashMap;
import java.util.List;

import org.jmock.Mockery;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PortInterface;

public interface PowerCircuitExpectations {

	public abstract void createRead(Mockery jmockContext, Long circuitId,
			PowerCircuit retValue);

	public abstract void createGetPowerCircuit(Mockery jmockContext,
			Long circuitId, PowerCircuit retValue);

	public abstract void createLoadCircuit(Mockery jmockContext,
			Long circuitId, PowerCircuit retValue);

	public abstract void createViewPowerCircuitByConnId(Mockery jmockContext,
			Long connectionId, List<PowerCircuit> retValue);

	public abstract void createViewPowerCircuitByStartPortId(
			Mockery jmockContext, Long portId, List<PowerCircuit> retValue);

	public abstract void createViewPowerCircuitByCriteria(Mockery jmockContext,
			CircuitCriteriaDTO cCriteria, List<PowerCircuit> retValue);

	public abstract void createGetDestinationItemsForItem(Mockery jmockContext,
			Long itemId, HashMap<Long, PortInterface> retValue);

	public abstract void createGetNextNodeAmpsForItem(Mockery jmockContext,
			Long itemId, HashMap<Long, PortInterface> retValue);

	public abstract void createGetPowerBankInfo(Mockery jmockContext,
			Long bankId, PowerBankInfo retValue);

	public abstract void createGetPowerUsage(Mockery jmockContext,
			String queryStr, Object[] queryArgs, List<?> retValue);

	public abstract void createGetPowerWattUsedSummary(Mockery jmockContext,
			Long portPowerId, Long portIdToExclude, Long fuseLkuId, Long inputCordToExclude,
			List<PowerWattUsedSummary> retValue);

	public abstract void createGetPowerWattUsedTotal(Mockery jmockContext,
			Long portPowerId, Long fuseLkuId, Long retValue);

	public abstract void createChangeCircuitConnectionChange(
			Mockery jmockContext, PowerPort oldPort, PowerPort newPort);

	public abstract void createGetCircuitsWithTrace(Mockery jmockContext,
			String trace, List<PowerCircuit> retValue);

	public abstract void createGetCircuitsInfoWithTrace(Mockery jmockContext,
			String trace, List<Object[]> retValue);

	public abstract void createGetConnAndDestPort(Mockery jmockContext,
			Long portId, List<Object[]> retValue);

	public abstract void createChangeCircuitTrace(Mockery jmockContext,
			Long circuitId, String circuitTrace, String sharedCircuitTrace,
			Long endConnId);

	public abstract void createGetProposedCircuitIdsForItem(
			Mockery jmockContext, Long itemId,
			HashMap<Long, PortInterface> retValue);

}